package com.abcd.hrms.timeleave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.abcd.hrms.timeleave",
        "com.abcd.hrms.common.core",
        "com.abcd.hrms.common.security",
        "com.abcd.hrms.common.messaging",
        "com.abcd.hrms.common.observability"
})
@EntityScan(basePackages = {
        "com.abcd.hrms.timeleave.entity",
        "com.abcd.hrms.common.messaging.outbox"
})
@EnableJpaRepositories(basePackages = {
        "com.abcd.hrms.timeleave.repository",
        "com.abcd.hrms.common.messaging.outbox"
})
public class TimeLeaveServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeLeaveServiceApplication.class, args);
    }
}
