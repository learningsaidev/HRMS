package com.abcd.hrms.common.observability.splunk;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Enterprise Asynchronous Splunk HTTP Event Collector (HEC) Appender.
 * Uses Java 11 native HttpClient to stream structured JSON log events directly to Splunk HEC.
 * Non-blocking, fault-tolerant, and zero external dependency.
 */
public class SplunkHecAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private String url = "http://localhost:8088/services/collector/event";
    private String token = "946654e2-6e83-4ae2-943a-a32ffef9305b";
    private String source = "abcd-hrms";
    private String sourcetype = "_json";
    private String index = "main";
    private boolean enabled = true;

    private BlockingQueue<String> eventQueue;
    private ScheduledExecutorService scheduler;
    private HttpClient httpClient;

    @Override
    public void start() {
        if (!enabled) {
            super.start();
            return;
        }

        this.eventQueue = new LinkedBlockingQueue<>(10000);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "splunk-hec-flusher");
            t.setDaemon(true);
            return t;
        });

        // Flush batch every 2 seconds
        this.scheduler.scheduleWithFixedDelay(this::flushEvents, 2, 2, TimeUnit.SECONDS);

        super.start();
    }

    @Override
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
            flushEvents();
        }
        super.stop();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted() || !enabled || eventQueue == null) {
            return;
        }

        try {
            String jsonEvent = formatSplunkEvent(event);
            eventQueue.offer(jsonEvent);
        } catch (Exception ignored) {
            // Never break application logging if formatting fails
        }
    }

    private void flushEvents() {
        if (eventQueue == null || eventQueue.isEmpty() || token == null || token.isEmpty()) {
            return;
        }

        StringBuilder payloadBuilder = new StringBuilder();
        int drained = 0;
        String line;
        while ((line = eventQueue.poll()) != null && drained < 100) {
            payloadBuilder.append(line).append("\n");
            drained++;
        }

        if (drained == 0) {
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(4))
                    .header("Authorization", "Splunk " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payloadBuilder.toString(), StandardCharsets.UTF_8))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .exceptionally(ex -> null); // Non-blocking async fire-and-forget
        } catch (Exception ignored) {
            // Discard on connection errors to protect core business workflows
        }
    }

    private String formatSplunkEvent(ILoggingEvent event) {
        double timeSec = event.getTimeStamp() / 1000.0;
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"time\":").append(timeSec).append(",");
        sb.append("\"host\":\"").append(escapeJson(getHostName())).append("\",");
        sb.append("\"source\":\"").append(escapeJson(source)).append("\",");
        sb.append("\"sourcetype\":\"").append(escapeJson(sourcetype)).append("\",");
        sb.append("\"index\":\"").append(escapeJson(index)).append("\",");
        sb.append("\"event\":{");
        sb.append("\"timestamp\":\"").append(Instant.ofEpochMilli(event.getTimeStamp()).toString()).append("\",");
        sb.append("\"level\":\"").append(event.getLevel().toString()).append("\",");
        sb.append("\"thread\":\"").append(escapeJson(event.getThreadName())).append("\",");
        sb.append("\"logger\":\"").append(escapeJson(event.getLoggerName())).append("\",");
        sb.append("\"message\":\"").append(escapeJson(event.getFormattedMessage())).append("\"");

        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc != null && !mdc.isEmpty()) {
            sb.append(",\"mdc\":{");
            boolean first = true;
            for (Map.Entry<String, String> entry : mdc.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escapeJson(entry.getKey())).append("\":\"")
                  .append(escapeJson(entry.getValue())).append("\"");
                first = false;
            }
            sb.append("}");
        }

        if (event.getThrowableProxy() != null) {
            sb.append(",\"exception\":\"").append(escapeJson(formatThrowable(event.getThrowableProxy()))).append("\"");
        }

        sb.append("}}");
        return sb.toString();
    }

    private String formatThrowable(IThrowableProxy tp) {
        StringBuilder sb = new StringBuilder();
        sb.append(tp.getClassName()).append(": ").append(tp.getMessage()).append("\n");
        for (StackTraceElementProxy step : tp.getStackTraceElementProxyArray()) {
            sb.append("\tat ").append(step.getSTEAsString()).append("\n");
        }
        return sb.toString();
    }

    private String getHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "localhost";
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Getters and Setters for Logback XML injection
    public void setUrl(String url) { this.url = url; }
    public void setToken(String token) { this.token = token; }
    public void setSource(String source) { this.source = source; }
    public void setSourcetype(String sourcetype) { this.sourcetype = sourcetype; }
    public void setIndex(String index) { this.index = index; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}

