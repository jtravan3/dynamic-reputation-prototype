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

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Component
@CommonsLog
public class TwoPhaseLockingScheduler extends TransactionScheduler {

    private final ConfigurationService configurationService;
    private final DataAccessManager dataAccessManager;

    @Autowired
    public TwoPhaseLockingScheduler(@NonNull ConfigurationService configurationService,
                                    @NonNull DataAccessManager dataAccessManager) {
        super(configurationService);
        this.configurationService = configurationService;
        this.dataAccessManager = dataAccessManager;
    }

    @Override
    @Async
    CompletableFuture<Void> beginSchedulerExecution(String useCase, User user1, User user2, Transaction transaction1, Transaction transaction2, String overallExecutionId,
                                                    int randInt, int randAbortInt) throws InterruptedException {
        Instant startTime = Instant.now();
        Configuration configuration = configurationService.getConfiguration();

        if (!ObjectUtils.allNotNull(user1, transaction1, user2, transaction2)) {
            Sentry.captureMessage("User or Transaction was null. Gracefully handled it. Nothing to worry about.");
            return CompletableFuture.completedFuture(null);
        }

        Double t1executionTime = getTransactionExecutionTime(transaction1);
        Double t1GrowingPhaseTime = getTransactionGrowingPhaseTime(transaction1);
        Double t1ShrinkingPhaseTime = getTransactionShrinkingPhaseTime(transaction1);
        log.info("U1: " + user1);
        log.info("T1: " + transaction1);
        log.info("T1 Execution Time: " + t1executionTime);
        log.info("T1 Growing Phase Time: " + t1GrowingPhaseTime);
        log.info("T1 Shrinking Phase Time: " + t1ShrinkingPhaseTime);

        Double t2executionTime = getTransactionExecutionTime(transaction2);
        Double t2GrowingPhaseTime = getTransactionGrowingPhaseTime(transaction2);
        Double t2ShrinkingPhaseTime = getTransactionShrinkingPhaseTime(transaction2);
        log.info("U2: " + user2);
        log.info("T2: " + transaction2);
        log.info("T2 Execution Time: " + t2executionTime);
        log.info("T2 Growing Phase Time: " + t2GrowingPhaseTime);
        log.info("T2 Shrinking Phase Time: " + t2ShrinkingPhaseTime);

        // Conflict
        if (randInt <= configuration.getConflictingPercentage()) {
            log.info("Conflicting Transactions");
            // Abort
            if (shouldAbort(user1, transaction1, randAbortInt, configuration)) {
                log.info("Abort Detected!");

                // Initial attempt
                executeLockPhase(t1GrowingPhaseTime);
                executeTransaction(t1executionTime);
                dataAccessManager.addTraditionalExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                        transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                        transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                        transaction1.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                        DominanceType.NOT_COMPARABLE, t1executionTime, configurationService.getPercentageAffected(),
                        false, TransactionOutcome.ABORT, overallExecutionId, useCase, TransactionType.NORMAL);

                executeLockPhase(t2GrowingPhaseTime);
                executeTransaction(t2executionTime);
                dataAccessManager.addTraditionalExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
                        transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
                        transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
                        transaction2.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                        DominanceType.NOT_COMPARABLE,t2executionTime, configurationService.getPercentageAffected(),
                        false, TransactionOutcome.ABORT, overallExecutionId, useCase, TransactionType.NORMAL);

                // compensation transactions
                executeLockPhase(t1GrowingPhaseTime);
                executeTransaction(t1executionTime);
                executeLockPhase(t1ShrinkingPhaseTime);
                dataAccessManager.addTraditionalExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                        transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                        transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                        transaction1.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                        DominanceType.NOT_COMPARABLE, t1executionTime, configurationService.getPercentageAffected(),
                        false, TransactionOutcome.COMMIT, overallExecutionId, useCase, TransactionType.COMPENSATION);

                executeLockPhase(t2GrowingPhaseTime);
                executeTransaction(t2executionTime);
                executeLockPhase(t2ShrinkingPhaseTime);
                dataAccessManager.addTraditionalExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
                        transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
                        transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
                        transaction2.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                        DominanceType.NOT_COMPARABLE,t2executionTime, configurationService.getPercentageAffected(),
                        false, TransactionOutcome.COMMIT, overallExecutionId, useCase, TransactionType.COMPENSATION);

                // Rerun attempt
                executeLockPhase(t1GrowingPhaseTime);
                executeTransaction(t1executionTime);
                executeLockPhase(t1ShrinkingPhaseTime);
                dataAccessManager.addTraditionalExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                        transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                        transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                        transaction1.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                        DominanceType.NOT_COMPARABLE, t1executionTime, configurationService.getPercentageAffected(),
                        false, TransactionOutcome.COMMIT, overallExecutionId, useCase, TransactionType.NORMAL);

                executeLockPhase(t2GrowingPhaseTime);
                executeTransaction(t2executionTime);
                executeLockPhase(t2ShrinkingPhaseTime);
                dataAccessManager.addTraditionalExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
                        transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
                        transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
                        transaction2.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                        DominanceType.NOT_COMPARABLE,t2executionTime, configurationService.getPercentageAffected(),
                        false, TransactionOutcome.COMMIT, overallExecutionId, useCase, TransactionType.NORMAL);
            } else {
                executeLockPhase(t1GrowingPhaseTime);
                executeTransaction(t1executionTime);
                executeLockPhase(t1ShrinkingPhaseTime);
                dataAccessManager.addTraditionalExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                        transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                        transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                        transaction1.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                        DominanceType.NOT_COMPARABLE, t1executionTime, configurationService.getPercentageAffected(),
                        false, TransactionOutcome.COMMIT, overallExecutionId, useCase, TransactionType.NORMAL);

                executeLockPhase(t2GrowingPhaseTime);
                executeTransaction(t2executionTime);
                executeLockPhase(t2ShrinkingPhaseTime);
                dataAccessManager.addTraditionalExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
                        transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
                        transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
                        transaction2.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                        DominanceType.NOT_COMPARABLE,t2executionTime, configurationService.getPercentageAffected(),
                        false, TransactionOutcome.COMMIT, overallExecutionId, useCase, TransactionType.NORMAL);
            }
        } else {
            log.info("Non-Conflicting Transactions");

            executeLockPhase(t1GrowingPhaseTime);
            executeTransaction(t1executionTime);
            executeLockPhase(t1ShrinkingPhaseTime);
            dataAccessManager.addTraditionalExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                    transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                    transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                    transaction1.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                    DominanceType.NOT_COMPARABLE, t1executionTime, configurationService.getPercentageAffected(),
                    false, TransactionOutcome.COMMIT, overallExecutionId, useCase, TransactionType.NORMAL);

            executeLockPhase(t2GrowingPhaseTime);
            executeTransaction(t2executionTime);
            executeLockPhase(t2ShrinkingPhaseTime);
            dataAccessManager.addTraditionalExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
                    transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
                    transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
                    transaction2.getTransaction_num_of_operations(), "N/A", LockingAction.GRANT,
                    DominanceType.NOT_COMPARABLE, t2executionTime, configurationService.getPercentageAffected(),
                    false, TransactionOutcome.COMMIT, overallExecutionId, useCase, TransactionType.NORMAL);
        }

        Instant endTime = Instant.now();
        dataAccessManager.addOverallExecutionHistory(overallExecutionId, (double) Duration.between(startTime, endTime).toMillis(), SchedulerType.TRADITIONAL);
        return CompletableFuture.completedFuture(null);
    }

    private boolean shouldAbort(User user, Transaction transaction, int randAbortInt, Configuration configuration) {
        return (randAbortInt <= configuration.getAbortPercentage() ||
                user.getUser_ranking() <= 0.2 || transaction.getTransaction_commit_ranking() <= 0.2);
    }
}
