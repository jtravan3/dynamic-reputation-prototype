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
public class PredictionBasedScheduler extends TransactionScheduler {

    private final ConfigurationService configurationService;
    private final DataAccessManager dataAccessManager;

    @Autowired
    public PredictionBasedScheduler(@NonNull ConfigurationService configurationService,
                                @NonNull DataAccessManager dataAccessManager) {
        super(configurationService);
        this.configurationService = configurationService;
        this.dataAccessManager = dataAccessManager;
    }

    @Async
    @Override
    CompletableFuture<Void> beginSchedulerExecution(String useCase, User user1, User user2, Transaction transaction1, Transaction transaction2, String overallExecutionId, int randInt, int randAbortInt) throws InterruptedException {

        Instant startTime = Instant.now();
        Configuration configuration = configurationService.getConfiguration();
        boolean recalculationNeeded = false;

        if (!ObjectUtils.allNotNull(user1, transaction1, user2, transaction2)) {
            Sentry.captureMessage("User or Transaction was null. Gracefully handled it. Nothing to worry about.");
            return CompletableFuture.completedFuture(null);
        }

        Double t1executionTime = getTransactionExecutionTime(transaction1);
        String t1RepScore = "N/A";

        log.info("U1: " + user1);
        log.info("T1: " + transaction1);
        log.info("T1 Execution Time: " + t1executionTime);
        log.info("T1 Reputation Score: " + t1RepScore);

        Double t2executionTime = getTransactionExecutionTime(transaction2);
        String t2RepScore = "N/A";
        log.info("U2: " + user2);
        log.info("T2: " + transaction2);
        log.info("T2 Execution Time: " + t2executionTime);
        log.info("T2 Reputation Score: " + t2RepScore);

        // Conflict
        if (randInt < configuration.getConflictingPercentage()) {
            log.info("Conflicting Transactions");
            PbsDominancePair dominancePair = establishPbsDominance(transaction1, user1, transaction2, user2);
            if (dominancePair.getDominatingCategory() != dominancePair.getWeakCategory()) {
                log.info("Dominating Transaction: " + dominancePair.getDominatingTransaction());
                log.info("Dominance Category: " + dominancePair.getDominatingCategory());
                
                Category dominatingCategory = dominancePair.getDominatingCategory();
                User dominatingUser = dominancePair.getDominatingUser();
                Transaction dominatingTransaction = dominancePair.getDominatingTransaction();
                Double dominatingTransactionTime = getTransactionExecutionTime(dominatingTransaction);
                String dominatingRepScore = "N/A";
                
                Category weakCategory = dominancePair.getWeakCategory();
                User weakUser = dominancePair.getWeakUser();
                Transaction weakTransaction = dominancePair.getWeakTransaction();
                Double weakTransactionTime = getTransactionExecutionTime(weakTransaction);
                String weakRepScore = "N/A";

                if (transaction1 == dominatingTransaction) {
                    if (shouldAbort(dominatingCategory, randAbortInt, configuration)) { // Abort

                        log.info("Abort Detected!");
                        executeTransaction(dominatingTransactionTime);
                        dataAccessManager.addPbsExecutionHistory(dominatingUser.getUserid(), dominatingUser.getUser_ranking(),
                                dominatingTransaction.getTransaction_id(), dominatingTransaction.getTransaction_commit_ranking(),
                                dominatingTransaction.getTransaction_system_ranking(), dominatingTransaction.getTransaction_eff_ranking(),
                                dominatingTransaction.getTransaction_num_of_operations(), dominatingRepScore, LockingAction.GRANT,
                                DominanceType.NOT_COMPARABLE, dominatingTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.ABORT, overallExecutionId, useCase, dominatingCategory);

                        executeTransaction(weakTransactionTime);
                        dataAccessManager.addPbsExecutionHistory(weakUser.getUserid(), weakUser.getUser_ranking(),
                                weakTransaction.getTransaction_id(), weakTransaction.getTransaction_commit_ranking(),
                                weakTransaction.getTransaction_system_ranking(), weakTransaction.getTransaction_eff_ranking(),
                                weakTransaction.getTransaction_num_of_operations(), weakRepScore, LockingAction.DECLINE,
                                DominanceType.NOT_COMPARABLE, weakTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase, weakCategory);

                        // rerun after abort
                        executeTransaction(dominatingTransactionTime);
                        dataAccessManager.addPbsExecutionHistory(dominatingUser.getUserid(), dominatingUser.getUser_ranking(),
                                dominatingTransaction.getTransaction_id(), dominatingTransaction.getTransaction_commit_ranking(),
                                dominatingTransaction.getTransaction_system_ranking(), dominatingTransaction.getTransaction_eff_ranking(),
                                dominatingTransaction.getTransaction_num_of_operations(), dominatingRepScore, LockingAction.GRANT,
                                DominanceType.NOT_COMPARABLE, dominatingTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase, dominatingCategory);
                        configurationService.incrementTotalAffectedTransactions();
                    } else {
                        executeTransaction(dominatingTransactionTime);
                        dataAccessManager.addPbsExecutionHistory(dominatingUser.getUserid(), dominatingUser.getUser_ranking(),
                                dominatingTransaction.getTransaction_id(), dominatingTransaction.getTransaction_commit_ranking(),
                                dominatingTransaction.getTransaction_system_ranking(), dominatingTransaction.getTransaction_eff_ranking(),
                                dominatingTransaction.getTransaction_num_of_operations(), dominatingRepScore, LockingAction.GRANT,
                                DominanceType.NOT_COMPARABLE, dominatingTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase, dominatingCategory);

                        executeTransaction(weakTransactionTime);
                        dataAccessManager.addPbsExecutionHistory(weakUser.getUserid(), weakUser.getUser_ranking(),
                                weakTransaction.getTransaction_id(), weakTransaction.getTransaction_commit_ranking(),
                                weakTransaction.getTransaction_system_ranking(), weakTransaction.getTransaction_eff_ranking(),
                                weakTransaction.getTransaction_num_of_operations(), weakRepScore, LockingAction.DECLINE,
                                DominanceType.NOT_COMPARABLE, weakTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase, weakCategory);
                    }
                } else {
                    configurationService.incrementTotalAffectedTransactions();

                    if (shouldAbort(dominatingCategory, randAbortInt, configuration)) {
                        log.info("Abort Detected!");
                        log.info("Abort Due To Elevation Detected!");
                        executeTransaction(dominatingTransactionTime);
                        dataAccessManager.addPbsExecutionHistory(dominatingUser.getUserid(), dominatingUser.getUser_ranking(),
                                dominatingTransaction.getTransaction_id(), dominatingTransaction.getTransaction_commit_ranking(),
                                dominatingTransaction.getTransaction_system_ranking(), dominatingTransaction.getTransaction_eff_ranking(),
                                dominatingTransaction.getTransaction_num_of_operations(), dominatingRepScore, LockingAction.ELEVATE,
                                DominanceType.NOT_COMPARABLE, dominatingTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.ABORT, overallExecutionId, useCase, dominatingCategory);

                        executeTransaction(weakTransactionTime);
                        dataAccessManager.addPbsExecutionHistory(weakUser.getUserid(), weakUser.getUser_ranking(),
                                weakTransaction.getTransaction_id(), weakTransaction.getTransaction_commit_ranking(),
                                weakTransaction.getTransaction_system_ranking(), weakTransaction.getTransaction_eff_ranking(),
                                weakTransaction.getTransaction_num_of_operations(), weakRepScore, LockingAction.DECLINE,
                                DominanceType.NOT_COMPARABLE, weakTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.ABORTED_DUE_TO_ELEVATE, overallExecutionId, useCase, weakCategory);

                        executeTransaction(weakTransactionTime);
                        dataAccessManager.addPbsExecutionHistory(weakUser.getUserid(), weakUser.getUser_ranking(),
                                weakTransaction.getTransaction_id(), weakTransaction.getTransaction_commit_ranking(),
                                weakTransaction.getTransaction_system_ranking(), weakTransaction.getTransaction_eff_ranking(),
                                weakTransaction.getTransaction_num_of_operations(), weakRepScore, LockingAction.GRANT,
                                DominanceType.NOT_COMPARABLE, weakTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase, weakCategory);

                        executeTransaction(dominatingTransactionTime);
                        dataAccessManager.addPbsExecutionHistory(dominatingUser.getUserid(), dominatingUser.getUser_ranking(),
                                dominatingTransaction.getTransaction_id(), dominatingTransaction.getTransaction_commit_ranking(),
                                dominatingTransaction.getTransaction_system_ranking(), dominatingTransaction.getTransaction_eff_ranking(),
                                dominatingTransaction.getTransaction_num_of_operations(), dominatingRepScore, LockingAction.GRANT,
                                DominanceType.NOT_COMPARABLE, dominatingTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase, dominatingCategory);
                        configurationService.incrementTotalAffectedTransactions();
                    } else {
                        executeTransaction(dominatingTransactionTime);
                        dataAccessManager.addPbsExecutionHistory(dominatingUser.getUserid(), dominatingUser.getUser_ranking(),
                                dominatingTransaction.getTransaction_id(), dominatingTransaction.getTransaction_commit_ranking(),
                                dominatingTransaction.getTransaction_system_ranking(), dominatingTransaction.getTransaction_eff_ranking(),
                                dominatingTransaction.getTransaction_num_of_operations(), dominatingRepScore, LockingAction.ELEVATE,
                                DominanceType.NOT_COMPARABLE, dominatingTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase, dominatingCategory);

                        executeTransaction(weakTransactionTime);
                        dataAccessManager.addPbsExecutionHistory(weakUser.getUserid(), weakUser.getUser_ranking(),
                                weakTransaction.getTransaction_id(), weakTransaction.getTransaction_commit_ranking(),
                                weakTransaction.getTransaction_system_ranking(), weakTransaction.getTransaction_eff_ranking(),
                                weakTransaction.getTransaction_num_of_operations(), weakRepScore, LockingAction.DECLINE,
                                DominanceType.NOT_COMPARABLE, weakTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.ABORTED_DUE_TO_ELEVATE, overallExecutionId, useCase, weakCategory);

                        executeTransaction(weakTransactionTime);
                        dataAccessManager.addPbsExecutionHistory(weakUser.getUserid(), weakUser.getUser_ranking(),
                                weakTransaction.getTransaction_id(), weakTransaction.getTransaction_commit_ranking(),
                                weakTransaction.getTransaction_system_ranking(), weakTransaction.getTransaction_eff_ranking(),
                                weakTransaction.getTransaction_num_of_operations(), weakRepScore, LockingAction.GRANT,
                                DominanceType.NOT_COMPARABLE, weakTransactionTime, configurationService.getPercentageAffected(),
                                recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase, weakCategory);
                    }
                }
            } else {
                if (shouldAbort(getCategory(transaction1), randAbortInt, configuration)) { // Abort

                    log.info("Abort Detected!");
                    executeTransaction(t1executionTime);
                    dataAccessManager.addPbsExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                            transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                            transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                            transaction1.getTransaction_num_of_operations(), t1RepScore, LockingAction.GRANT,
                            DominanceType.NOT_COMPARABLE, t1executionTime, configurationService.getPercentageAffected(),
                            recalculationNeeded, TransactionOutcome.ABORT, overallExecutionId, useCase, getCategory(transaction1));

                    executeTransaction(t2executionTime);
                    dataAccessManager.addPbsExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
                            transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
                            transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
                            transaction2.getTransaction_num_of_operations(), t2RepScore, LockingAction.DECLINE,
                            DominanceType.NOT_COMPARABLE, t2executionTime, configurationService.getPercentageAffected(),
                            recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase, getCategory(transaction2));

                    // rerun after abort
                    executeTransaction(t1executionTime);
                    dataAccessManager.addPbsExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                            transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                            transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                            transaction1.getTransaction_num_of_operations(), t1RepScore, LockingAction.GRANT,
                            DominanceType.NOT_COMPARABLE, t1executionTime, configurationService.getPercentageAffected(),
                            recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId,useCase, getCategory(transaction1));
                    configurationService.incrementTotalAffectedTransactions();
                } else {
                    executeTransaction(t1executionTime);
                    dataAccessManager.addPbsExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                            transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                            transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                            transaction1.getTransaction_num_of_operations(), t1RepScore, LockingAction.GRANT,
                            DominanceType.NOT_COMPARABLE, t1executionTime, configurationService.getPercentageAffected(),
                            recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase, getCategory(transaction1));

                    executeTransaction(t2executionTime);
                    dataAccessManager.addPbsExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
                            transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
                            transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
                            transaction2.getTransaction_num_of_operations(), t2RepScore, LockingAction.DECLINE,
                            DominanceType.NOT_COMPARABLE, t2executionTime, configurationService.getPercentageAffected(),
                            recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase, getCategory(transaction2));
                }
            }
        } else { // No conflict
            log.info("Non-Conflicting Transactions");

            executeTransaction(t1executionTime);
            dataAccessManager.addPbsExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                    transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                    transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                    transaction1.getTransaction_num_of_operations(), t1RepScore, LockingAction.GRANT,
                    DominanceType.NO_CONFLICT, t1executionTime, configurationService.getPercentageAffected(),
                    recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase, getCategory(transaction1));

            executeTransaction(t2executionTime);
            dataAccessManager.addPbsExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
                    transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
                    transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
                    transaction2.getTransaction_num_of_operations(), t2RepScore, LockingAction.GRANT,
                    DominanceType.NO_CONFLICT,t2executionTime, configurationService.getPercentageAffected(),
                    recalculationNeeded, TransactionOutcome.COMMIT, overallExecutionId, useCase, getCategory(transaction2));
        }

        Instant endTime = Instant.now();
        dataAccessManager.addOverallExecutionHistory(overallExecutionId, (double) Duration.between(startTime, endTime).toMillis(), SchedulerType.PBS);
        return CompletableFuture.completedFuture(null);
    }
    
    private PbsDominancePair establishPbsDominance(Transaction t1, User user1, Transaction t2, User user2) {
        PbsDominancePair pbsDominancePair = new PbsDominancePair();
        
        Category t1Category = getCategory(t1);
        Category t2Category = getCategory(t2);
        
        if (Category.isCategory1HigherThanOrEqualCategory2(t1Category, t2Category)) {
            pbsDominancePair.setDominatingCategory(t1Category);
            pbsDominancePair.setDominatingUser(user1);
            pbsDominancePair.setDominatingTransaction(t1);
            pbsDominancePair.setWeakCategory(t2Category);
            pbsDominancePair.setWeakTransaction(t2);
            pbsDominancePair.setWeakUser(user2);
        } else {
            pbsDominancePair.setDominatingCategory(t2Category);
            pbsDominancePair.setDominatingUser(user2);
            pbsDominancePair.setDominatingTransaction(t2);
            pbsDominancePair.setWeakCategory(t1Category);
            pbsDominancePair.setWeakTransaction(t1);
            pbsDominancePair.setWeakUser(user1);
        }
        
        return pbsDominancePair;
    }

    private boolean shouldAbort(Category category, int randAbortInt, Configuration configuration) {
        return (randAbortInt < configuration.getAbortPercentage() || category == Category.LCHE || category == Category.LCLE);
    }
    
    private Category getCategory(Transaction transaction) {
        if (transaction.getTransaction_eff_ranking() >= .5 && transaction.getTransaction_commit_ranking() >= .5) {
            return Category.HCHE;
        } else if (transaction.getTransaction_eff_ranking() < .5 && transaction.getTransaction_commit_ranking() >= .5) {
            return Category.HCLE;
        } else if (transaction.getTransaction_eff_ranking() >= .5 && transaction.getTransaction_commit_ranking() < .5) {
            return Category.LCHE;
        } else {
            return Category.LCLE;
        }
    }
    
}
