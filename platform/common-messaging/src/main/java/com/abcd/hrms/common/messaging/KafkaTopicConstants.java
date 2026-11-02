package com.abcd.hrms.common.messaging;

/**
 * Standard Apache Kafka domain topics across ABCD HRMS ecosystem.
 */
public final class KafkaTopicConstants {

    private KafkaTopicConstants() {
    }

    public static final String TOPIC_WORKFORCE_EVENTS = "abcd.hrms.workforce.events";
    public static final String TOPIC_TIME_LEAVE_EVENTS = "abcd.hrms.time-leave.events";
    public static final String TOPIC_ONBOARDING_EVENTS = "abcd.hrms.onboarding.events";
    public static final String TOPIC_PAYROLL_EVENTS = "abcd.hrms.payroll.events";
    public static final String TOPIC_TAX_BENEFITS_EVENTS = "abcd.hrms.tax-benefits.events";
    public static final String TOPIC_PAYMENT_LEDGER_EVENTS = "abcd.hrms.payment-ledger.events";
    public static final String TOPIC_NOTIFICATION_EVENTS = "abcd.hrms.notification.events";
}
