package com.abcd.hrms.onboarding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.abcd.hrms.common",
        "com.abcd.hrms.onboarding"
})
@EntityScan(basePackages = {
        "com.abcd.hrms.common",
        "com.abcd.hrms.onboarding.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.abcd.hrms.common",
        "com.abcd.hrms.onboarding.repository"
})
public class OnboardingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnboardingServiceApplication.class, args);
    }
}
