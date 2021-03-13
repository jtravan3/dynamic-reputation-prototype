package com.jtravan.application;

import com.jtravan.components.EntryPoint;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Spring boot entry point for the Redox Fhir Store Worker
 */
@ComponentScan(value = {"com.jtravan"})
@SpringBootApplication
@EnableAsync
@CommonsLog
public class DynamicReputationPrototype {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(DynamicReputationPrototype.class, args);
        try {
            applicationContext.getBean(EntryPoint.class).run();
        } catch (InterruptedException e) {
           log.error("Thread interrupted at Spring Boot Application");
        }
    }
}

