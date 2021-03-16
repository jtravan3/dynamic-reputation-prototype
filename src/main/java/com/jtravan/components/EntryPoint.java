package com.jtravan.components;

import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Component
@CommonsLog
public class EntryPoint {

    private final DataAccessManager dataAccessManager;

    @Autowired
    public EntryPoint(@NonNull DataAccessManager dataAccessManager) {
        this.dataAccessManager = dataAccessManager;
    }

    @Async
    public CompletableFuture<String> run() {
        while(true) {
            try {
                Thread.sleep(5000);
                Random random = new Random();
                String transaction_id = RandomStringUtils.randomAlphabetic(10, 50);
                Double commit_ranking = Math.random();
                Double system_ranking = Math.random();
                Double eff_ranking = Math.random();
                Integer num_of_operations = random.nextInt(200);
                dataAccessManager.addTransaction(transaction_id, commit_ranking,
                        system_ranking, eff_ranking, num_of_operations);
//                dataAccessManager.addExecutionHistory("jtravan3",
//                        0.45,
//                        "potato",
//                        0.36,
//                        0.23,
//                        0.12,
//                        23,
//                        4.23,
//                        LockingAction.ELEVATE,
//                        1234.12,
//                        34.1,
//                        false );
                log.info("Successfully added random transaction: " + transaction_id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
