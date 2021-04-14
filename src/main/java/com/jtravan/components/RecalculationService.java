package com.jtravan.components;

import com.jtravan.dal.model.ExecutionHistory;
import com.jtravan.dal.model.Transaction;
import com.jtravan.dal.model.User;
import com.jtravan.model.TransactionOutcome;
import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@CommonsLog
public class RecalculationService {

    private final DataAccessManager dataAccessManager;

    private int num_of_users;
    private int num_of_transactions;

    @Autowired
    public RecalculationService(@NonNull DataAccessManager dataAccessManager) {
        this.dataAccessManager = dataAccessManager;
    }

    @Async
    public void recalculate(String useCase) {
        long startTime = System.nanoTime();
        log.info("Recalculation Initiated...");
        List<Transaction> allTransactions = dataAccessManager.getAllTransactions();
        List<ExecutionHistory> allHistory = dataAccessManager.getAllHistory();
        List<User> allUsers = dataAccessManager.getUsers();

        log.info("Transaction Rankings Started...");
        recalculateTransactionRankings(allTransactions, allHistory);
        log.info("Transaction Rankings Completed");

        log.info("User Ranking Started...");
        recalculateUserRanking(allUsers, allHistory);
        log.info("User Ranking Completed");

        log.info("Recalculation Complete!");
        long endTime = System.nanoTime();

        double duration = (double)((endTime - startTime) / 1000000);
        dataAccessManager.addRecalculationMetric(allHistory.size(), num_of_users, num_of_transactions, duration, useCase);
        log.info("Recalculation Metric Entry Added");

        allTransactions.clear();
        allHistory.clear();
        allUsers.clear();
    }

    private void recalculateUserRanking(List<User> users,
                                        List<ExecutionHistory> executionHistories) {

        Map<User, Long> userToNumOfAbortsMap = new HashMap<>();

        for (User user : users) {
            List<ExecutionHistory> transactionHistory = executionHistories.stream()
                    .filter(h -> h.getUserid().equals(user.getUserid()))
                    .collect(Collectors.toList());

            if (!transactionHistory.isEmpty()) {
                long numofAborts = transactionHistory.stream()
                        .filter(h -> TransactionOutcome.valueOf(h.getTransaction_outcome()) == TransactionOutcome.ABORT)
                        .count();
                userToNumOfAbortsMap.put(user, numofAborts);
            }
        }

        Set<User> allExecutedUsers = new LinkedHashSet<>(userToNumOfAbortsMap.keySet());
        num_of_users = allExecutedUsers.size();

        for (User user : allExecutedUsers) {
            long numOfAborts = userToNumOfAbortsMap.get(user);

            long numOfUsersWithGreaterOrEqualNumOfAborts =
                    userToNumOfAbortsMap.entrySet().stream()
                            .filter(e -> e.getKey() != user && e.getValue() >= numOfAborts)
                            .count();
            double newUserRanking = (double) numOfUsersWithGreaterOrEqualNumOfAborts / allExecutedUsers.size();
            user.setUser_ranking(newUserRanking);
            dataAccessManager.updateUser(user);
        }

    }

    private void recalculateTransactionRankings(List<Transaction> transactions,
                                                List<ExecutionHistory> executionHistories) {

        Map<Transaction, Long> transactionToNumOfCommitMap = new HashMap<>();
        Map<Transaction, Long> transactionToElevateAbortsMap = new HashMap<>();
        Map<Transaction, Double> transactionToExecutionTimeMap = new HashMap<>();

        for (Transaction transaction : transactions) {
            List<ExecutionHistory> transactionHistory = executionHistories.stream()
                    .filter(h -> h.getTransaction_id().equals(transaction.getTransaction_id()))
                    .collect(Collectors.toList());

            if (!transactionHistory.isEmpty()) {
                long numofCommits = transactionHistory.stream()
                .filter(h -> TransactionOutcome.valueOf(h.getTransaction_outcome()) == TransactionOutcome.COMMIT)
                        .count();
                transactionToNumOfCommitMap.put(transaction, numofCommits);
                long numofElevateAborts = transactionHistory.stream()
                        .filter(h -> TransactionOutcome.valueOf(h.getTransaction_outcome()) == TransactionOutcome.ABORTED_DUE_TO_ELEVATE)
                        .count();
                transactionToElevateAbortsMap.put(transaction, numofElevateAborts);
                double executionTimeTotal = transactionHistory.stream()
                        .mapToDouble(ExecutionHistory::getTransaction_execution_time)
                        .sum();
                transactionToExecutionTimeMap.put(transaction, executionTimeTotal);
            }
        }

        Set<Transaction> allExecutedTransactions = new LinkedHashSet<>(transactionToNumOfCommitMap.keySet());

        num_of_transactions = allExecutedTransactions.size();

       for (Transaction transaction : allExecutedTransactions) {
           long numOfCommits = transactionToNumOfCommitMap.get(transaction);
           long numOfElevateAborts = transactionToElevateAbortsMap.get(transaction);
           double executionTime = transactionToExecutionTimeMap.get(transaction);

           long numOfTransactionsWithSmallerOrEqualNumOfCommits =
                   transactionToNumOfCommitMap.entrySet().stream()
                        .filter(e -> e.getKey() != transaction && e.getValue() <= numOfCommits)
                           .count();
           double newCommitRanking = (double) numOfTransactionsWithSmallerOrEqualNumOfCommits / allExecutedTransactions.size();
           transaction.setTransaction_commit_ranking(newCommitRanking);

           long numOfTransactionsWithLargerOrEqualNumOfElevateAborts =
                   transactionToElevateAbortsMap.entrySet().stream()
                           .filter(e -> e.getKey() != transaction && e.getValue() >= numOfElevateAborts)
                           .count();
           double newSystemRanking = (double) numOfTransactionsWithLargerOrEqualNumOfElevateAborts / allExecutedTransactions.size();
           transaction.setTransaction_system_ranking(newSystemRanking);

           long numOfTransactionsWithLargerOrEqualExecutionTime =
                   transactionToExecutionTimeMap.entrySet().stream()
                           .filter(e -> e.getKey() != transaction && e.getValue() >= executionTime)
                           .count();
           double newEfficiencyRanking = (double) numOfTransactionsWithLargerOrEqualExecutionTime / allExecutedTransactions.size();
           transaction.setTransaction_eff_ranking(newEfficiencyRanking);
           dataAccessManager.updateTransaction(transaction);
       }

    }
}
