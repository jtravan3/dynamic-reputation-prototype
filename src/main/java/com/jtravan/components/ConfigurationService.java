package com.jtravan.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationService {

    private int abortPercentage;
    private int conflictingPercentage;
    private boolean isExecutionLive;
    private int recalculationPercentage;

    @Autowired
    public ConfigurationService() {
        this.abortPercentage = 5;
        this.conflictingPercentage = 10;
        this.recalculationPercentage = 50;
        this.isExecutionLive = true;
    }

    public int getAbortPercentage() {
        return abortPercentage;
    }

    public void setAbortPercentage(int abortPercentage) {
        this.abortPercentage = abortPercentage;
    }

    public int getConflictingPercentage() {
        return conflictingPercentage;
    }

    public void setConflictingPercentage(int conflictingPercentage) {
        this.conflictingPercentage = conflictingPercentage;
    }

    public boolean isExecutionLive() {
        return isExecutionLive;
    }

    public void setExecutionLive(boolean executionLive) {
        isExecutionLive = executionLive;
    }

    public int getRecalculationPercentage() {
        return recalculationPercentage;
    }

    public void setRecalculationPercentage(int recalculationPercentage) {
        this.recalculationPercentage = recalculationPercentage;
    }
}