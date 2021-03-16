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

    @Autowired
    public EntryPoint(@NonNull DataAccessManager dataAccessManager) {
        this.dataAccessManager = dataAccessManager;
    }

    @Async
    public CompletableFuture<String> run() {
        while(true) {
            try {
                Thread.sleep(5000);
                String userId = dataAccessManager.getRandomUsername();
                Double user_ranking = Math.random();
                dataAccessManager.addUser(userId, user_ranking);
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
                log.info("Successfully added random user: " + userId + ", " + user_ranking);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
