package com.jtravan.components;

import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@CommonsLog
public class EntryPoint {

    private final TransactionOrchestrator transactionOrchestrator;
    private final ConfigurationService configurationService;

    @Autowired
    public EntryPoint(@NonNull TransactionOrchestrator transactionOrchestrator,
                      @NonNull ConfigurationService configurationService) {
        this.transactionOrchestrator = transactionOrchestrator;
        this.configurationService = configurationService;
    }

    @Async
    public void run() {
        while(configurationService.isExecutionLive()) {
            try {
                transactionOrchestrator.beginExecutions();
                log.info("Successfully executed transactions");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log.info("Execution successfully stopped");
    }
}
