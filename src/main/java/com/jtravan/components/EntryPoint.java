package com.jtravan.components;


import lombok.extern.apachecommons.CommonsLog;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@CommonsLog
@Profile("!test")
public class EntryPoint {

    public void run(String... args) {
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
