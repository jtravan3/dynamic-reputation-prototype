package com.jtravan.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring boot entry point for the Redox Fhir Store Worker
 */
@ComponentScan(value = {"com.jtravan"})
@SpringBootApplication
public class DynamicReputationPrototype {
    public static void main(String[] args) {
        SpringApplication.run(DynamicReputationPrototype.class, args);
    }
}

