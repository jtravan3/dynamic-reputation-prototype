package com.jtravan.components;

import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@CommonsLog
public class EntryPoint {

    private final DataAccessManager dataAccessManager;
    private final TransactionOrchestrator transactionOrchestrator;
    private boolean isExecutionLive;

    @Autowired
    public EntryPoint(@NonNull DataAccessManager dataAccessManager,
                      @NonNull TransactionOrchestrator transactionOrchestrator) {
        this.dataAccessManager = dataAccessManager;
        this.transactionOrchestrator = transactionOrchestrator;
        this.isExecutionLive = true;
    }

    public void setExecutionLive(boolean isExecutionLive) {
        this.isExecutionLive = isExecutionLive;
    }

    @Async
    public void run() {
        while(isExecutionLive) {
            try {
                Thread.sleep(5000);
                transactionOrchestrator.executeTransaction();
                log.info("Successfully executed transactions");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log.info("Execution successfully stopped");
    }
}
