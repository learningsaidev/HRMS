package com.abcd.hrms.common.observability.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Common Micrometer Prometheus metrics configuration.
 */
@Configuration
public class MetricsConfig {

    @Value("${spring.application.name:abcd-service}")
    private String applicationName;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags("application", applicationName, "environment", activeProfile);
    }
}
