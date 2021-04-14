package com.jtravan.application;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Spring boot entry point for the Redox Fhir Store Worker
 */
@ComponentScan(value = {"com.jtravan"})
@EnableJpaRepositories("com.jtravan.dal")
@EntityScan("com.jtravan.dal.model")
@SpringBootApplication
@EnableAsync
@EnableCaching
@CommonsLog
public class DynamicReputationPrototype {
    public static void main(String[] args) {
        SpringApplication.run(DynamicReputationPrototype.class, args);
    }
}

