package com.abcd.hrms.taxbenefits;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.abcd.hrms.common",
        "com.abcd.hrms.taxbenefits"
})
@EntityScan(basePackages = {
        "com.abcd.hrms.common",
        "com.abcd.hrms.taxbenefits.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.abcd.hrms.common",
        "com.abcd.hrms.taxbenefits.repository"
})
public class TaxBenefitsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaxBenefitsServiceApplication.class, args);
    }
}

