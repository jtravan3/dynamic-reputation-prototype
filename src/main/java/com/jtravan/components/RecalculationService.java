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
    private List<Transaction> allTransactions;
    private List<ExecutionHistory> allHistory;
    private List<User> allUsers;

    @Autowired
    public RecalculationService(@NonNull DataAccessManager dataAccessManager) {
        this.dataAccessManager = dataAccessManager;
    }

    @Async
    public void recalculate() {
        log.info("Recalculation Initiated...");
        allTransactions = dataAccessManager.getAllTransactions();
        allHistory = dataAccessManager.getAllHistory();
        allUsers = dataAccessManager.getUsers();

        log.info("Commit Ranking Started...");
        recalculateCommitRanking(allTransactions, allHistory);
        log.info("Commit Ranking Completed");

        allTransactions.clear();
        allHistory.clear();
        allUsers.clear();
        log.info("Recalculation Complete!");
    }

    private void recalculateUserRanking(List<User> users,
                                        List<ExecutionHistory> executionHistories) {



    }

    private void recalculateCommitRanking(List<Transaction> transactions,
                                          List<ExecutionHistory> executionHistories) {

        Map<Transaction, Long> transactionToNumOfCommitMap = new HashMap<>();

        for (Transaction transaction : transactions) {
            List<ExecutionHistory> transactionHistory = executionHistories.stream()
                    .filter(h -> h.getTransaction_id().equals(transaction.getTransaction_id()))
                    .collect(Collectors.toList());

            if (!transactionHistory.isEmpty()) {
                long numofCommits = transactionHistory.stream()
                .filter(h -> TransactionOutcome.valueOf(h.getTransaction_outcome()) == TransactionOutcome.COMMIT)
                        .count();
                transactionToNumOfCommitMap.put(transaction, numofCommits);
            }
        }

        Set<Transaction> allExecutedTransactions = new LinkedHashSet<>(transactionToNumOfCommitMap.keySet());

       for (Transaction transaction : allExecutedTransactions) {
           long numOfCommits = transactionToNumOfCommitMap.get(transaction);

           long numOfTransactionsWithSmallerOrEqualNumOfCommits =
                   transactionToNumOfCommitMap.entrySet().stream()
                        .filter(e -> e.getKey() != transaction && e.getValue() <= numOfCommits)
                           .count();
           double newCommitRanking = (double) numOfTransactionsWithSmallerOrEqualNumOfCommits / allExecutedTransactions.size();
           transaction.setTransaction_commit_ranking(newCommitRanking);
           dataAccessManager.updateTransaction(transaction);
       }

    }
}
