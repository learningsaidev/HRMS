package com.abcd.hrms.payroll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.abcd.hrms.common",
        "com.abcd.hrms.payroll"
})
@EntityScan(basePackages = {
        "com.abcd.hrms.common",
        "com.abcd.hrms.payroll.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.abcd.hrms.common",
        "com.abcd.hrms.payroll.repository"
})
public class PayrollServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayrollServiceApplication.class, args);
    }
}
