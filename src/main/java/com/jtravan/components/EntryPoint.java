package com.jtravan.components;

import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@CommonsLog
public class EntryPoint {

    private final DataAccessManager dataAccessManager;
    private final TransactionOrchestrator transactionOrchestrator;

    @Autowired
    public EntryPoint(@NonNull DataAccessManager dataAccessManager,
                      @NonNull TransactionOrchestrator transactionOrchestrator) {
        this.dataAccessManager = dataAccessManager;
        this.transactionOrchestrator = transactionOrchestrator;
    }

    @Async
    public CompletableFuture<String> run() {
        while(true) {
            try {
                Thread.sleep(5000);
                transactionOrchestrator.executeTransaction();
                log.info("Successfully executed transactions");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
