package com.jtravan.components;

import com.jtravan.dal.model.Transaction;
import com.jtravan.dal.model.User;
import com.jtravan.model.*;
import io.sentry.Sentry;
import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@CommonsLog
public class TraditionalScheduler extends TransactionScheduler {

    private final ConfigurationService configurationService;
    private final DataAccessManager dataAccessManager;

    @Autowired
    public TraditionalScheduler(@NonNull ConfigurationService configurationService,
                                @NonNull DataAccessManager dataAccessManager) {
        super(configurationService);
        this.configurationService = configurationService;
        this.dataAccessManager = dataAccessManager;
    }

    @Override
    @Async
    CompletableFuture<Void> beginSchedulerExecution(String useCase, User user1, User user2, Transaction transaction1, Transaction transaction2, String overallExecutionId,
                                                    int randInt, int randAbortInt) throws InterruptedException {

        Configuration configuration = configurationService.getConfiguration();

        if (!ObjectUtils.allNotNull(user1, transaction1, user2, transaction2)) {
            Sentry.captureMessage("User or Transaction was null. Gracefully handled it. Nothing to worry about.");
            return CompletableFuture.completedFuture(null);
        }

        Double t1executionTime = getTransactionExecutionTime(transaction1);

        log.info("U1: " + user1);
        log.info("T1: " + transaction1);
        log.info("T1 Execution Time: " + t1executionTime);

        Double t2executionTime = getTransactionExecutionTime(transaction2);
        log.info("U2: " + user2);
        log.info("T2: " + transaction2);
        log.info("T2 Execution Time: " + t2executionTime);

        // Conflict
        if (randInt <= configuration.getConflictingPercentage()) {
            log.info("Conflicting Transactions");
            // Abort
            if (shouldAbort(user1, transaction1, randAbortInt, configuration)) {
                log.info("Abort Detected!");

                // Initial attempt
                executeTransaction(t1executionTime);
                dataAccessManager.addTraditionalExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                        transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                        transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                        transaction1.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                        DominanceType.NOT_COMPARABLE, t1executionTime, configurationService.getPercentageAffected(),
                        false, TransactionOutcome.ABORT, overallExecutionId, useCase, TransactionType.NORMAL);

                executeTransaction(t2executionTime);
                dataAccessManager.addTraditionalExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
                        transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
                        transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
                        transaction2.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                        DominanceType.NOT_COMPARABLE,t2executionTime, configurationService.getPercentageAffected(),
                        false, TransactionOutcome.ABORT, overallExecutionId, useCase, TransactionType.NORMAL);

                // compensation transactions
                executeTransaction(t1executionTime);
                dataAccessManager.addTraditionalExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                        transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                        transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                        transaction1.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                        DominanceType.NOT_COMPARABLE, t1executionTime, configurationService.getPercentageAffected(),
                        false, TransactionOutcome.COMMIT, overallExecutionId, useCase, TransactionType.COMPENSATION);

                executeTransaction(t2executionTime);
                dataAccessManager.addTraditionalExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
                        transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
                        transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
                        transaction2.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                        DominanceType.NOT_COMPARABLE,t2executionTime, configurationService.getPercentageAffected(),
                        false, TransactionOutcome.COMMIT, overallExecutionId, useCase, TransactionType.COMPENSATION);

                // Rerun attempt
                executeTransaction(t1executionTime);
                dataAccessManager.addTraditionalExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                        transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                        transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                        transaction1.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                        DominanceType.NOT_COMPARABLE, t1executionTime, configurationService.getPercentageAffected(),
                        false, TransactionOutcome.COMMIT, overallExecutionId, useCase, TransactionType.NORMAL);

                executeTransaction(t2executionTime);
                dataAccessManager.addTraditionalExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
                        transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
                        transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
                        transaction2.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                        DominanceType.NOT_COMPARABLE,t2executionTime, configurationService.getPercentageAffected(),
                        false, TransactionOutcome.COMMIT, overallExecutionId, useCase, TransactionType.NORMAL);
            } else {
                executeTransaction(t1executionTime);
                dataAccessManager.addTraditionalExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                        transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                        transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                        transaction1.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                        DominanceType.NOT_COMPARABLE, t1executionTime, configurationService.getPercentageAffected(),
                        false, TransactionOutcome.COMMIT, overallExecutionId, useCase, TransactionType.NORMAL);

                executeTransaction(t2executionTime);
                dataAccessManager.addTraditionalExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
                        transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
                        transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
                        transaction2.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                        DominanceType.NOT_COMPARABLE,t2executionTime, configurationService.getPercentageAffected(),
                        false, TransactionOutcome.COMMIT, overallExecutionId, useCase, TransactionType.NORMAL);
            }
        } else {
            log.info("Non-Conflicting Transactions");

            executeTransaction(t1executionTime);
            dataAccessManager.addTraditionalExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                    transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                    transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                    transaction1.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                    DominanceType.NOT_COMPARABLE, t1executionTime, configurationService.getPercentageAffected(),
                    false, TransactionOutcome.COMMIT, overallExecutionId, useCase, TransactionType.NORMAL);

            executeTransaction(t2executionTime);
            dataAccessManager.addTraditionalExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
                    transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
                    transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
                    transaction2.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                    DominanceType.NOT_COMPARABLE, t2executionTime, configurationService.getPercentageAffected(),
                    false, TransactionOutcome.COMMIT, overallExecutionId, useCase, TransactionType.NORMAL);
        }

        return CompletableFuture.completedFuture(null);
    }

    private boolean shouldAbort(User user, Transaction transaction, int randAbortInt, Configuration configuration) {
        return (randAbortInt <= configuration.getAbortPercentage() ||
                user.getUser_ranking() <= 0.2 || transaction.getTransaction_commit_ranking() <= 0.2);
    }
}
