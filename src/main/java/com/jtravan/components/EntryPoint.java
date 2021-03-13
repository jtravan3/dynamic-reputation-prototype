package com.jtravan.components;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@CommonsLog
public class EntryPoint {

    @Async
    public CompletableFuture<String> run() throws InterruptedException {
        while(true) {
            try {
                Thread.sleep(5000);
                log.info("What's up homie?");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
