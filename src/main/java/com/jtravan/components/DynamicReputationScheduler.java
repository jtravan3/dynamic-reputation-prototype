package com.jtravan.components;

import com.jtravan.dal.model.Transaction;
import com.jtravan.dal.model.User;
import com.jtravan.model.*;
import io.sentry.Sentry;
import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;


@Component
@CommonsLog
public class DynamicReputationScheduler extends TransactionScheduler {

    private final DataAccessManager dataAccessManager;
    private final ConfigurationService configurationService;
    private final RecalculationService recalculationService;

    @Autowired
    public DynamicReputationScheduler(@NonNull DataAccessManager dataAccessManager,
                                      @NonNull ConfigurationService configurationService,
                                      @NonNull RecalculationService recalculationService) {
        super(configurationService);
        this.dataAccessManager = dataAccessManager;
        this.configurationService = configurationService;
        this.recalculationService = recalculationService;
    }

    @Async
    @Override
    CompletableFuture<Void> beginSchedulerExecution(String useCase,
                                                    User user1,
                                                    User user2,
                                                    Transaction transaction1,
                                                    Transaction transaction2,
                                                    String overallExecutionId,
                                                    int randInt, int randAbortInt) throws InterruptedException {

        Instant startTime = Instant.now();
        Configuration configuration = configurationService.getConfiguration();
        CompletableFuture<Void> recalculationFuture = null;

        boolean recalculationNeeded = false;
        if ((configurationService.getPercentageAffected() >= configuration.getRecalculationPercentage())
                && (configuration.getTotalTransactionsExecuted() > configuration.getMinimumTransactionsInTheSystem())) {
            recalculationFuture = recalculationService.recalculate(useCase);
            configuration.setTotalAffectedTransactions(0);
            configuration.setTotalTransactionsExecuted(0);
            configurationService.setConfiguration(configuration);
            recalculationNeeded = true;
        }

        if (!ObjectUtils.allNotNull(user1, transaction1, user2, transaction2)) {
            Sentry.captureMessage("User or Transaction was null. Gracefully handled it. Nothing to worry about.");
            return CompletableFuture.completedFuture(null);
        }

        Double t1executionTime = getTransactionExecutionTime(transaction1);
        String t1RepScore = getReputationScore(user1, transaction1);

        log.info("U1: " + user1);
        log.info("T1: " + transaction1);
        log.info("T1 Execution Time: " + t1executionTime);
        log.info("T1 Reputation Score: " + t1RepScore);

        Double t2executionTime = getTransactionExecutionTime(transaction2);
        String t2RepScore = getReputationScore(user2, transaction2);
        log.info("U2: " + user2);
        log.info("T2: " + transaction2);
        log.info("T2 Execution Time: " + t2executionTime);
        log.info("T2 Reputation Score: " + t2RepScore);

        // Conflict
        if (randInt < configuration.getConflictingPercentage()) {
            log.info("Conflicting Transactions");
            DominancePair dominancePair = establishDominance(transaction1, user1, transaction2, user2);
            if (dominancePair.getDominanceType() != DominanceType.NOT_COMPARABLE) {
                log.info("Dominating Transaction: " + dominancePair.getDominatingTransaction());
                log.info("Dominance Type: " + dominancePair.getDominanceType());

                User dominatingUser = dominancePair.getDominatingUser();
                Transaction dominatingTransaction = dominancePair.getDominatingTransaction();
                Double dominatingTransactionTime = getTransactionExecutionTime(dominatingTransaction);
                String dominatingRepScore = getReputationScore(dominatingUser, dominatingTransaction);

                User weakUser = dominancePair.getWeakUser();
                Transaction weakTransaction = dominancePair.getWeakTransaction();
                Double weakTransactionTime = getTransactionExecutionTime(weakTransaction);
                String weakRepScore = getReputationScore(weakUser, weakTransaction);

                if (transaction1 == dominatingTransaction) {
                    if (shouldAbort(dominatingUser, dominatingTransaction, randAbortInt, configuration)) { // Abort

                        log.info("Abort Detected!");
                        executeTransaction(dominatingTransactionTime);
                        dataAccessManager.addExecutionHistory(dominatingUser.getUserid(), dominatingUser.getUser_ranking(),
                                dominatingTransaction.getTransaction_id(), dominatingTransaction.getTransaction_commit_ranking(),
                                dominatingTransaction.getTransaction_system_ranking(), dominatingTransaction.getTransaction_eff_ranking(),
                                dominatingTransaction.getTransaction_num_of_operations(), dominatingRepScore, LockingAction.GRANT,
                                dominancePair.getDominanceType(), dominatingTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.ABORT, overallExecutionId, useCase);

                        executeTransaction(weakTransactionTime);
                        dataAccessManager.addExecutionHistory(weakUser.getUserid(), weakUser.getUser_ranking(),
                                weakTransaction.getTransaction_id(), weakTransaction.getTransaction_commit_ranking(),
                                weakTransaction.getTransaction_system_ranking(), weakTransaction.getTransaction_eff_ranking(),
                                weakTransaction.getTransaction_num_of_operations(), weakRepScore, LockingAction.DECLINE,
                                dominancePair.getDominanceType(), weakTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase);

                        // rerun after abort
                        executeTransaction(dominatingTransactionTime);
                        dataAccessManager.addExecutionHistory(dominatingUser.getUserid(), dominatingUser.getUser_ranking(),
                                dominatingTransaction.getTransaction_id(), dominatingTransaction.getTransaction_commit_ranking(),
                                dominatingTransaction.getTransaction_system_ranking(), dominatingTransaction.getTransaction_eff_ranking(),
                                dominatingTransaction.getTransaction_num_of_operations(), dominatingRepScore, LockingAction.GRANT,
                                dominancePair.getDominanceType(), dominatingTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase);
                        configurationService.incrementTotalAffectedTransactions();
                    } else {
                        executeTransaction(dominatingTransactionTime);
                        dataAccessManager.addExecutionHistory(dominatingUser.getUserid(), dominatingUser.getUser_ranking(),
                                dominatingTransaction.getTransaction_id(), dominatingTransaction.getTransaction_commit_ranking(),
                                dominatingTransaction.getTransaction_system_ranking(), dominatingTransaction.getTransaction_eff_ranking(),
                                dominatingTransaction.getTransaction_num_of_operations(), dominatingRepScore, LockingAction.GRANT,
                                dominancePair.getDominanceType(), dominatingTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase);

                        executeTransaction(weakTransactionTime);
                        dataAccessManager.addExecutionHistory(weakUser.getUserid(), weakUser.getUser_ranking(),
                                weakTransaction.getTransaction_id(), weakTransaction.getTransaction_commit_ranking(),
                                weakTransaction.getTransaction_system_ranking(), weakTransaction.getTransaction_eff_ranking(),
                                weakTransaction.getTransaction_num_of_operations(), weakRepScore, LockingAction.DECLINE,
                                dominancePair.getDominanceType(), weakTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase);
                    }
                } else {
                    configurationService.incrementTotalAffectedTransactions();

                    if (shouldAbort(dominatingUser, dominatingTransaction, randAbortInt, configuration)) {
                        log.info("Abort Detected!");
                        log.info("Abort Due To Elevation Detected!");
                        executeTransaction(dominatingTransactionTime);
                        dataAccessManager.addExecutionHistory(dominatingUser.getUserid(), dominatingUser.getUser_ranking(),
                                dominatingTransaction.getTransaction_id(), dominatingTransaction.getTransaction_commit_ranking(),
                                dominatingTransaction.getTransaction_system_ranking(), dominatingTransaction.getTransaction_eff_ranking(),
                                dominatingTransaction.getTransaction_num_of_operations(), dominatingRepScore, LockingAction.ELEVATE,
                                dominancePair.getDominanceType(), dominatingTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.ABORT, overallExecutionId, useCase);

                        executeTransaction(weakTransactionTime);
                        dataAccessManager.addExecutionHistory(weakUser.getUserid(), weakUser.getUser_ranking(),
                                weakTransaction.getTransaction_id(), weakTransaction.getTransaction_commit_ranking(),
                                weakTransaction.getTransaction_system_ranking(), weakTransaction.getTransaction_eff_ranking(),
                                weakTransaction.getTransaction_num_of_operations(), weakRepScore, LockingAction.DECLINE,
                                dominancePair.getDominanceType(), weakTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.ABORTED_DUE_TO_ELEVATE, overallExecutionId, useCase);

                        executeTransaction(weakTransactionTime);
                        dataAccessManager.addExecutionHistory(weakUser.getUserid(), weakUser.getUser_ranking(),
                                weakTransaction.getTransaction_id(), weakTransaction.getTransaction_commit_ranking(),
                                weakTransaction.getTransaction_system_ranking(), weakTransaction.getTransaction_eff_ranking(),
                                weakTransaction.getTransaction_num_of_operations(), weakRepScore, LockingAction.GRANT,
                                dominancePair.getDominanceType(), weakTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase);

                        executeTransaction(dominatingTransactionTime);
                        dataAccessManager.addExecutionHistory(dominatingUser.getUserid(), dominatingUser.getUser_ranking(),
                                dominatingTransaction.getTransaction_id(), dominatingTransaction.getTransaction_commit_ranking(),
                                dominatingTransaction.getTransaction_system_ranking(), dominatingTransaction.getTransaction_eff_ranking(),
                                dominatingTransaction.getTransaction_num_of_operations(), dominatingRepScore, LockingAction.GRANT,
                                dominancePair.getDominanceType(), dominatingTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase);
                        configurationService.incrementTotalAffectedTransactions();
                    } else {
                        executeTransaction(dominatingTransactionTime);
                        dataAccessManager.addExecutionHistory(dominatingUser.getUserid(), dominatingUser.getUser_ranking(),
                                dominatingTransaction.getTransaction_id(), dominatingTransaction.getTransaction_commit_ranking(),
                                dominatingTransaction.getTransaction_system_ranking(), dominatingTransaction.getTransaction_eff_ranking(),
                                dominatingTransaction.getTransaction_num_of_operations(), dominatingRepScore, LockingAction.ELEVATE,
                                dominancePair.getDominanceType(), dominatingTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase);

                        executeTransaction(weakTransactionTime);
                        dataAccessManager.addExecutionHistory(weakUser.getUserid(), weakUser.getUser_ranking(),
                                weakTransaction.getTransaction_id(), weakTransaction.getTransaction_commit_ranking(),
                                weakTransaction.getTransaction_system_ranking(), weakTransaction.getTransaction_eff_ranking(),
                                weakTransaction.getTransaction_num_of_operations(), weakRepScore, LockingAction.DECLINE,
                                dominancePair.getDominanceType(), weakTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.ABORTED_DUE_TO_ELEVATE, overallExecutionId, useCase);

                        executeTransaction(weakTransactionTime);
                        dataAccessManager.addExecutionHistory(weakUser.getUserid(), weakUser.getUser_ranking(),
                                weakTransaction.getTransaction_id(), weakTransaction.getTransaction_commit_ranking(),
                                weakTransaction.getTransaction_system_ranking(), weakTransaction.getTransaction_eff_ranking(),
                                weakTransaction.getTransaction_num_of_operations(), weakRepScore, LockingAction.GRANT,
                                dominancePair.getDominanceType(), weakTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase);
                    }
                }
            } else {
                if (shouldAbort(user1, transaction1, randAbortInt, configuration)) { // Abort

                    log.info("Abort Detected!");
                    executeTransaction(t1executionTime);
                    dataAccessManager.addExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                            transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                            transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                            transaction1.getTransaction_num_of_operations(), t1RepScore, LockingAction.GRANT,
                            DominanceType.NOT_COMPARABLE, t1executionTime, configurationService.getPercentageAffected(),
                            recalculationNeeded, TransactionOutcome.ABORT, overallExecutionId, useCase);

                    executeTransaction(t2executionTime);
                    dataAccessManager.addExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
                            transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
                            transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
                            transaction2.getTransaction_num_of_operations(), t2RepScore, LockingAction.DECLINE,
                            DominanceType.NOT_COMPARABLE, t2executionTime, configurationService.getPercentageAffected(),
                            recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase);

                    // rerun after abort
                    executeTransaction(t1executionTime);
                    dataAccessManager.addExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                            transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                            transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                            transaction1.getTransaction_num_of_operations(), t1RepScore, LockingAction.GRANT,
                            DominanceType.NOT_COMPARABLE, t1executionTime, configurationService.getPercentageAffected(),
                            recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId,useCase);
                    configurationService.incrementTotalAffectedTransactions();
                } else {
                    executeTransaction(t1executionTime);
                    dataAccessManager.addExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                            transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                            transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                            transaction1.getTransaction_num_of_operations(), t1RepScore, LockingAction.GRANT,
                            DominanceType.NOT_COMPARABLE, t1executionTime, configurationService.getPercentageAffected(),
                            recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase);

                    executeTransaction(t2executionTime);
                    dataAccessManager.addExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
                            transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
                            transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
                            transaction2.getTransaction_num_of_operations(), t2RepScore, LockingAction.DECLINE,
                            DominanceType.NOT_COMPARABLE, t2executionTime, configurationService.getPercentageAffected(),
                            recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase);
                }
            }
        } else { // No conflict
            log.info("Non-Conflicting Transactions");

            executeTransaction(t1executionTime);
            dataAccessManager.addExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                    transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                    transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                    transaction1.getTransaction_num_of_operations(), t1RepScore, LockingAction.GRANT,
                    DominanceType.NO_CONFLICT, t1executionTime, configurationService.getPercentageAffected(),
                    recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase);

            executeTransaction(t2executionTime);
            dataAccessManager.addExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
                    transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
                    transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
                    transaction2.getTransaction_num_of_operations(), t2RepScore, LockingAction.GRANT,
                    DominanceType.NO_CONFLICT,t2executionTime, configurationService.getPercentageAffected(),
                    recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase);
        }

        if (recalculationNeeded) {
            try {
                recalculationFuture.join();
            } catch (Exception e) {
                log.error("Error while waiting on recalculation");
                Sentry.captureException(e);
            }
        }

        Instant endTime = Instant.now();
        dataAccessManager.addOverallExecutionHistory(overallExecutionId, (double) Duration.between(startTime, endTime).toMillis(), SchedulerType.DRP);
        return CompletableFuture.completedFuture(null);
    }

    public String getReputationScore(User user, Transaction transaction) {
        return "<"
                + Precision.round(transaction.getTransaction_commit_ranking(), 5)
                + ","
                + Precision.round(transaction.getTransaction_eff_ranking(), 5)
                + ","
                + Precision.round(user.getUser_ranking(), 5)
                + ","
                + Precision.round(transaction.getTransaction_system_ranking(), 5)
                + ">";
    }

    private boolean shouldAbort(User user, Transaction transaction, int randAbortInt, Configuration configuration) {
        return (randAbortInt < configuration.getAbortPercentage() ||
                user.getUser_ranking() <= 0.2 || transaction.getTransaction_commit_ranking() <= 0.2);
    }

    public DominancePair establishDominance(Transaction t1, User u1, Transaction t2, User u2) {
        DominancePair dominancePair = new DominancePair();
        if (u1.getUser_ranking() >= u2.getUser_ranking()
                && t1.getTransaction_commit_ranking() >= t2.getTransaction_commit_ranking()
                && t1.getTransaction_eff_ranking() >= t2.getTransaction_eff_ranking()
                && t1.getTransaction_system_ranking() >= t2.getTransaction_system_ranking()) {
            dominancePair.setDominanceType(DominanceType.STRONG);
            dominancePair.setDominatingUser(u1);
            dominancePair.setDominatingTransaction(t1);
            dominancePair.setWeakTransaction(t2);
            dominancePair.setWeakUser(u2);
            return dominancePair;
        } else if (u2.getUser_ranking() >= u1.getUser_ranking()
                && t2.getTransaction_commit_ranking() >= t1.getTransaction_commit_ranking()
                && t2.getTransaction_eff_ranking() >= t1.getTransaction_eff_ranking()
                && t2.getTransaction_system_ranking() >= t1.getTransaction_system_ranking()) {
            dominancePair.setDominanceType(DominanceType.STRONG);
            dominancePair.setDominatingUser(u2);
            dominancePair.setDominatingTransaction(t2);
            dominancePair.setWeakTransaction(t1);
            dominancePair.setWeakUser(u1);
            return dominancePair;
        } else if ((u1.getUser_ranking()
                + t1.getTransaction_commit_ranking()
                + t1.getTransaction_eff_ranking()
                + t1.getTransaction_system_ranking()) >=
                (u2.getUser_ranking()
                        + t2.getTransaction_commit_ranking()
                        + t2.getTransaction_eff_ranking()
                        + t2.getTransaction_system_ranking())) {
            dominancePair.setDominanceType(DominanceType.WEAK);
            dominancePair.setDominatingUser(u1);
            dominancePair.setDominatingTransaction(t1);
            dominancePair.setWeakTransaction(t2);
            dominancePair.setWeakUser(u2);
            return dominancePair;
        } else if ((u2.getUser_ranking()
                + t2.getTransaction_commit_ranking()
                + t2.getTransaction_eff_ranking()
                + t2.getTransaction_system_ranking()) >=
                (u1.getUser_ranking()
                        + t1.getTransaction_commit_ranking()
                        + t1.getTransaction_eff_ranking()
                        + t1.getTransaction_system_ranking())) {
            dominancePair.setDominanceType(DominanceType.WEAK);
            dominancePair.setDominatingUser(u2);
            dominancePair.setDominatingTransaction(t2);
            dominancePair.setWeakTransaction(t1);
            dominancePair.setWeakUser(u1);
            return dominancePair;
        } else {
            dominancePair.setDominanceType(DominanceType.NOT_COMPARABLE);
            return dominancePair;
        }
    }
}
