package com.jtravan.components;

import com.jtravan.dal.model.UseCaseMetric;
import com.jtravan.model.Configuration;
import io.sentry.Sentry;
import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
@CommonsLog
public class EntryPoint {

    private final TransactionOrchestrator transactionOrchestrator;
    private final ConfigurationService configurationService;
    private final DataAccessManager dataAccessManager;
    private List<String> useCases;

    @Autowired
    public EntryPoint(@NonNull TransactionOrchestrator transactionOrchestrator,
                      @NonNull ConfigurationService configurationService,
                      @NonNull DataAccessManager dataAccessManager) {
        this.transactionOrchestrator = transactionOrchestrator;
        this.configurationService = configurationService;
        this.dataAccessManager = dataAccessManager;

        this.useCases = new LinkedList<>();
        this.useCases.add("Use Case 19");
        this.useCases.add("Use Case 20");
        this.useCases.add("Use Case 21");
        this.useCases.add("Use Case 22");
        this.useCases.add("Use Case 23");
        this.useCases.add("Use Case 24");
        this.useCases.add("Use Case 25");
    }

    public UseCaseMetric updateUseCase(String useCase) {
        Configuration configuration = configurationService.getConfiguration();
        UseCaseMetric useCaseMetric = dataAccessManager.getUseCaseMetricByName(useCase);
        configuration.setConflictingPercentage(useCaseMetric.getConflicting_percentage());
        configuration.setAbortPercentage(useCaseMetric.getAbort_percentage());
        configuration.setRecalculationPercentage(useCaseMetric.getRecalculation_percentage());
        configuration.setTransactionThreshold(5000);
        configurationService.setConfiguration(configuration);
        return useCaseMetric;
    }

    public void resetConfiguration() {
        Configuration configuration = configurationService.getConfiguration();
        configuration.setTotalAffectedTransactions(0);
        configuration.setTotalTransactionsExecuted(0);
        configuration.setTransactionThreshold(5000);
        configurationService.setConfiguration(configuration);
    }

    @Async
    public void run() {
        Configuration configuration = configurationService.getConfiguration();
        int transactionThreshold = configuration.getTransactionThreshold();

        for (String useCase : useCases) {
            configuration.setIsExecutionLive(true);
            log.info("Executing use case: " + useCase);
            UseCaseMetric useCaseMetric = updateUseCase(useCase);
            while (configuration.getIsExecutionLive()) {

                if (transactionThreshold > 0 && transactionThreshold > configuration.getTotalTransactionsExecuted()) {
                    try {
                        transactionOrchestrator.beginExecutions(useCase);
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
            log.info("Use Case execution successfully stopped");

            if (useCaseMetric != null) {
                useCaseMetric.setTotal_affected_transactions(configuration.getTotalAffectedTransactions());
                useCaseMetric.setTotal_transactions_executed(configuration.getTotalTransactionsExecuted());
                dataAccessManager.updateUseCaseMetrics(useCaseMetric);
                log.info("Successfully updated Use Case totals");
            }

            resetConfiguration();
        }
    }
}
