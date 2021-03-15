package com.jtravan.components;

import com.jtravan.model.LockingAction;
import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@CommonsLog
public class EntryPoint {

    private final DynamicReputationTransactionManager dynamicReputationTransactionManager;

    @Autowired
    public EntryPoint(@NonNull DynamicReputationTransactionManager dynamicReputationTransactionManager) {
        this.dynamicReputationTransactionManager = dynamicReputationTransactionManager;
    }

    @Async
    public CompletableFuture<String> run() {
        while(true) {
            try {
                Thread.sleep(5000);
                dynamicReputationTransactionManager.addExecutionHistory("jtravan3",
                        0.45,
                        0.36,
                        0.23,
                        0.12,
                        23,
                        LockingAction.ELEVATE,
                        4.23,
                        1234.12,
                        34.1,
                        false );
                log.info("Successfully added execution history!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
