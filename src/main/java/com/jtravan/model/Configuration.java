package com.jtravan.model;

import lombok.Data;

@Data
public class Configuration {
    private Integer abortPercentage;
    private Integer conflictingPercentage;
    private Boolean isExecutionLive;
    private Integer recalculationPercentage;
    private Integer totalTransactionsExecuted;
    private Integer totalAffectedTransactions;
    private Integer minimumTransactionsInTheSystem;
}
