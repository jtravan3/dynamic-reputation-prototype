package com.jtravan.components;


import lombok.extern.apachecommons.CommonsLog;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@CommonsLog
@Profile("!test")
public class EntryPoint implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        while(true) {
            Thread.sleep(5000);
            log.info("What's up homie?");
        }
    }
}
