package com.jtravan.application;

import com.jtravan.components.EntryPoint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring boot entry point for the Redox Fhir Store Worker
 */
@ComponentScan(value = {"com.jtravan"})
@SpringBootApplication
public class DynamicReputationPrototype {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(DynamicReputationPrototype.class, args);
        applicationContext.getBean(EntryPoint.class).run();
    }
}

