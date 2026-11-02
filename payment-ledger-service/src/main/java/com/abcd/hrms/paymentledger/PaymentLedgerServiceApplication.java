package com.abcd.hrms.paymentledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.abcd.hrms.common",
        "com.abcd.hrms.paymentledger"
})
@EntityScan(basePackages = {
        "com.abcd.hrms.common",
        "com.abcd.hrms.paymentledger.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.abcd.hrms.common",
        "com.abcd.hrms.paymentledger.repository"
})
public class PaymentLedgerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentLedgerServiceApplication.class, args);
    }
}

