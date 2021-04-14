package com.jtravan.components;

import com.jtravan.dal.model.UseCaseMetric;
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

        if (useCase != null) {
            UseCaseMetric useCaseMetric = dataAccessManager.getUseCaseMetricByName(useCase);
            configurationService.setConflictingPercentage(useCaseMetric.getConflicting_percentage());
            configurationService.setAbortPercentage(useCaseMetric.getAbort_percentage());
            configurationService.setRecalculationPercentage(useCaseMetric.getRecalculation_percentage());
        }

        while(configurationService.isExecutionLive()) {
            try {
                transactionOrchestrator.beginExecutions(useCase);
                log.info("Successfully executed transactions");
            } catch (Exception e) {
                Sentry.captureException(e);
                e.printStackTrace();
            }
        }
        log.info("Execution successfully stopped");
    }
}
