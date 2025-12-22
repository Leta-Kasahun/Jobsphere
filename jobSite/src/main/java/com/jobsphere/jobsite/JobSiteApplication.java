package com.jobsphere.jobsite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class JobSiteApplication {
    public static void main(String[] args) {
        SpringApplication.run(JobSiteApplication.class, args);
    }
}