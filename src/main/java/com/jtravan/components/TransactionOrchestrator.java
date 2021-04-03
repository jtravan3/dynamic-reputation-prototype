package com.jtravan.components;

import com.jtravan.dal.model.Transaction;
import com.jtravan.dal.model.User;
import com.jtravan.model.DominancePair;
import com.jtravan.model.DominanceType;
import com.jtravan.model.LockingAction;
import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@CommonsLog
public class TransactionOrchestrator {

    private final DataAccessManager dataAccessManager;
    private final Random random;
    private int totalTransactionsExecuted;
    private int totalAffectedTransactions;

    @Autowired
    public TransactionOrchestrator(@NonNull DataAccessManager dataAccessManager) {
        this.dataAccessManager = dataAccessManager;
        this.random = new Random();
    }

    public void executeTransaction() throws InterruptedException {
        User user1 = dataAccessManager.getRandomUser();
        Transaction transaction1 = dataAccessManager.getRandomTransaction();

        Double t1executionTime = getTransactionExecutionTime(transaction1);
        String t1RepScore = getReputationScore(user1, transaction1);

        log.info("U1: " + user1);
        log.info("T1: " + transaction1);
        log.info("T1 Execution Time: " + t1executionTime);
        log.info("T1 Reputation Score: " + t1RepScore);


        User user2 = dataAccessManager.getRandomUser();
        Transaction transaction2 = dataAccessManager.getRandomTransaction();

        Double t2executionTime = getTransactionExecutionTime(transaction2);
        String t2RepScore = getReputationScore(user2, transaction2);
        log.info("U2: " + user2);
        log.info("T2: " + transaction2);
        log.info("T2 Execution Time: " + t2executionTime);
        log.info("T2 Reputation Score: " + t2RepScore);

        Integer randInt = random.nextInt(100);
        // Conflict
        if (randInt <= 10) {
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

                LockingAction lockingAction;
                if (transaction1 == dominatingTransaction) {
                    lockingAction = LockingAction.GRANT;
                } else {
                    lockingAction = LockingAction.ELEVATE;
                    totalAffectedTransactions++;
                }

                executeTransaction(dominatingTransactionTime);
                dataAccessManager.addExecutionHistory(dominatingUser.getUserid(), dominatingUser.getUser_ranking(),
                        dominatingTransaction.getTransaction_id(), dominatingTransaction.getTransaction_commit_ranking(),
                        dominatingTransaction.getTransaction_system_ranking(), dominatingTransaction.getTransaction_eff_ranking(),
                        dominatingTransaction.getTransaction_num_of_operations(), dominatingRepScore, lockingAction,
                        dominancePair.getDominanceType(), dominatingTransactionTime, getPercentageAffected(), false);

                executeTransaction(weakTransactionTime);
                dataAccessManager.addExecutionHistory(weakUser.getUserid(), weakUser.getUser_ranking(),
                        weakTransaction.getTransaction_id(), weakTransaction.getTransaction_commit_ranking(),
                        weakTransaction.getTransaction_system_ranking(), weakTransaction.getTransaction_eff_ranking(),
                        weakTransaction.getTransaction_num_of_operations(), weakRepScore, LockingAction.DECLINE,
                        dominancePair.getDominanceType(), weakTransactionTime, getPercentageAffected(), false);

            } else {

                executeTransaction(t1executionTime);
                dataAccessManager.addExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                        transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                        transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                        transaction1.getTransaction_num_of_operations(), t1RepScore, LockingAction.GRANT,
                        DominanceType.NOT_COMPARABLE, t1executionTime, getPercentageAffected(), false);

                executeTransaction(t2executionTime);
                dataAccessManager.addExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
                        transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
                        transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
                        transaction2.getTransaction_num_of_operations(), t2RepScore, LockingAction.DECLINE,
                        DominanceType.NOT_COMPARABLE, t2executionTime, getPercentageAffected(), false);
            }
        } else { // No conflict
            log.info("Non-Conflicting Transactions");

            executeTransaction(t1executionTime);
            dataAccessManager.addExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
                    transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
                    transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
                    transaction1.getTransaction_num_of_operations(), t1RepScore, LockingAction.GRANT,
                    DominanceType.NO_CONFLICT, t1executionTime, getPercentageAffected(), false);

            executeTransaction(t2executionTime);
            dataAccessManager.addExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
                    transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
                    transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
                    transaction2.getTransaction_num_of_operations(), t2RepScore, LockingAction.GRANT,
                    DominanceType.NO_CONFLICT,t2executionTime, getPercentageAffected(), false);
        }
    }

    public Double getPercentageAffected() {
        return ((double) totalAffectedTransactions/ (double) totalTransactionsExecuted) * 100;
    }

    public void executeTransaction(Double executionTime) throws InterruptedException {
        Thread.sleep(executionTime.intValue());
        totalTransactionsExecuted++;
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

    public Double getTransactionExecutionTime(Transaction transaction) {
        return Precision.round((random.nextDouble() * random.nextInt(250)) * transaction.getTransaction_num_of_operations(), 5);
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
