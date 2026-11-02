package com.abcd.hrms.reporting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.abcd.hrms.common",
        "com.abcd.hrms.reporting"
})
@EntityScan(basePackages = {
        "com.abcd.hrms.common",
        "com.abcd.hrms.reporting.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.abcd.hrms.common",
        "com.abcd.hrms.reporting.repository"
})
public class NotificationReportingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationReportingServiceApplication.class, args);
    }
}

