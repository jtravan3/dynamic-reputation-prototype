package com.jtravan.components;

import com.jtravan.model.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationService {

    private Configuration configuration;

    @Autowired
    public ConfigurationService() {
        configuration = new Configuration();
        configuration.setAbortPercentage(25);
        configuration.setConflictingPercentage(25);
        configuration.setRecalculationPercentage(10);
        configuration.setTotalAffectedTransactions(0);
        configuration.setTotalTransactionsExecuted(0);
        configuration.setMinimumTransactionsInTheSystem(100);
        configuration.setTransactionThreshold(11000);
        configuration.setIsExecutionLive(false);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Double getPercentageAffected() {
        return ((double) configuration.getTotalAffectedTransactions() / (double) configuration.getTotalTransactionsExecuted()) * 100;
    }

    public void incrementTotalTransactionsExecuted() {
        int count = configuration.getTotalTransactionsExecuted();
        count++;
        configuration.setTotalTransactionsExecuted(count);
    }

    public void incrementTotalAffectedTransactions() {
        int count = configuration.getTotalAffectedTransactions();
        count++;
        configuration.setTotalAffectedTransactions(count);
    }
}
