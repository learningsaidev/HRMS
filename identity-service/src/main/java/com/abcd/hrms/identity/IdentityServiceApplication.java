package com.abcd.hrms.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.abcd.hrms.identity",
        "com.abcd.hrms.common.core",
        "com.abcd.hrms.common.security",
        "com.abcd.hrms.common.messaging",
        "com.abcd.hrms.common.observability"
})
@EntityScan(basePackages = {
        "com.abcd.hrms.identity.entity",
        "com.abcd.hrms.common.messaging.outbox"
})
@EnableJpaRepositories(basePackages = {
        "com.abcd.hrms.identity.repository",
        "com.abcd.hrms.common.messaging.outbox"
})
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
