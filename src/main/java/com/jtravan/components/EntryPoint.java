package com.jtravan.components;

import com.jtravan.dal.model.UseCaseMetric;
import com.jtravan.model.Configuration;
import io.sentry.Sentry;
import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@CommonsLog
public class EntryPoint {

    private final TransactionOrchestrator transactionOrchestrator;
    private final ConfigurationService configurationService;
    private final DataAccessManager dataAccessManager;
    private int transactionsExecuted;

    @Value("${drp.use-case}")
    private String useCase;

    @Autowired
    public EntryPoint(@NonNull TransactionOrchestrator transactionOrchestrator,
                      @NonNull ConfigurationService configurationService,
                      @NonNull DataAccessManager dataAccessManager) {
        this.transactionOrchestrator = transactionOrchestrator;
        this.configurationService = configurationService;
        this.dataAccessManager = dataAccessManager;
    }

    @Async
    public void run() {
        Configuration configuration = configurationService.getConfiguration();
        UseCaseMetric useCaseMetric = null;
        if (useCase != null) {
            useCaseMetric = dataAccessManager.getUseCaseMetricByName(useCase);
            configuration.setConflictingPercentage(useCaseMetric.getConflicting_percentage());
            configuration.setAbortPercentage(useCaseMetric.getAbort_percentage());
            configuration.setRecalculationPercentage(useCaseMetric.getRecalculation_percentage());
            configuration.setTransactionThreshold(10000);
            configurationService.setConfiguration(configuration);
        }

        int transactionThreshold = configuration.getTransactionThreshold();

        while(configuration.getIsExecutionLive()) {

            if (transactionThreshold > 0 && transactionThreshold < transactionsExecuted) {
                try {
                    transactionOrchestrator.beginExecutions(useCase);
                    transactionsExecuted+=3;
                    log.info("Successfully executed transactions");
                } catch (Exception e) {
                    Sentry.captureException(e);
                    e.printStackTrace();
                }
            } else {
                configuration.setIsExecutionLive(false);
                log.info("Transaction threshold reached");
            }
        }
        log.info("Execution successfully stopped");

        if (useCaseMetric != null) {
            useCaseMetric.setTotal_affected_transactions(configuration.getTotalAffectedTransactions());
            useCaseMetric.setTotal_transactions_executed(configuration.getTotalTransactionsExecuted());
            dataAccessManager.updateUseCaseMetrics(useCaseMetric);
            log.info("Successfully updated Use Case totals");
        }
    }
}
