package com.jtravan.components;

import com.jtravan.dal.ExecutionHistoryServiceImpl;
import com.jtravan.dal.TransactionServiceImpl;
import com.jtravan.dal.UserServiceImpl;
import com.jtravan.dal.model.ExecutionHistory;
import com.jtravan.dal.model.Transaction;
import com.jtravan.dal.model.User;
import com.jtravan.model.DominanceType;
import com.jtravan.model.LockingAction;
import com.jtravan.model.RandomUsernameResponse;
import com.jtravan.model.TransactionOutcome;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Random;

@Component
public class DataAccessManager {

    private final ExecutionHistoryServiceImpl executionHistoryService;
    private final UserServiceImpl userService;
    private final TransactionServiceImpl transactionService;
    private final WebClient webClient;
    private final Random random;

    @Autowired
    public DataAccessManager(@NonNull ExecutionHistoryServiceImpl executionHistoryService,
                             @NonNull UserServiceImpl userService,
                             @NonNull TransactionServiceImpl transactionService,
                             @NonNull WebClient webClient) {
        this.executionHistoryService = executionHistoryService;
        this.userService = userService;
        this.transactionService = transactionService;
        this.webClient = webClient;
        this.random = new Random();
    }

    @Async
    public void addTransaction(String transactionId, Double commit_ranking,
                               Double system_ranking, Double eff_ranking,
                               Integer num_of_operations) {
        Transaction transaction = new Transaction();
        transaction.setTransaction_id(transactionId);
        transaction.setTransaction_commit_ranking(commit_ranking);
        transaction.setTransaction_system_ranking(system_ranking);
        transaction.setTransaction_eff_ranking(eff_ranking);
        transaction.setTransaction_num_of_operations(num_of_operations);
        transactionService.addTransaction(transaction);
    }

    public Transaction getRandomTransaction() {
        return transactionService.getTransactionById(random.nextInt(11144));
    }

    public User getRandomUser() {
        return userService.getUserById(random.nextInt(5856));
    }

    public void updateTransaction(Transaction transaction) {
        transactionService.updateTransaction(transaction);
    }

    @Async
    public void addUser(String userId, Double user_ranking) {
        User user = new User();
        user.setUserid(userId);
        user.setUser_ranking(user_ranking);
        userService.addUser(user);
    }

    public String getRandomUsername() {
        RandomUsernameResponse response = webClient
                .get()
                .uri("https://randomuser.me/api/")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(RandomUsernameResponse.class)
                .block();

        return response.getResults().get(0).getLogin().getUsername();
    }

    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    public List<ExecutionHistory> getAllHistory() {
        return executionHistoryService.getAllHistory();
    }

    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @Async
    public void addExecutionHistory(String userid,
                                    Double user_ranking,
                                    String transaction_id,
                                    Double transaction_commit_ranking,
                                    Double transaction_system_ranking,
                                    Double transaction_eff_ranking,
                                    Integer transaction_num_of_operations,
                                    String reputation_score,
                                    LockingAction action_taken,
                                    DominanceType dominanceType,
                                    Double transaction_execution_time,
                                    Double percentage_affected,
                                    Boolean recalculation_needed,
                                    TransactionOutcome transactionOutcome) {

        ExecutionHistory executionHistory = new ExecutionHistory();

        executionHistory.setUserid(userid);
        executionHistory.setTransaction_id(transaction_id);
        executionHistory.setUser_ranking(user_ranking);
        executionHistory.setTransaction_commit_ranking(transaction_commit_ranking);
        executionHistory.setTransaction_system_ranking(transaction_system_ranking);
        executionHistory.setTransaction_eff_ranking(transaction_eff_ranking);
        executionHistory.setTransaction_num_of_operations(transaction_num_of_operations);
        executionHistory.setReputation_score(reputation_score);
        executionHistory.setAction_taken(action_taken.name());
        executionHistory.setDominance_type(dominanceType.name());
        executionHistory.setTransaction_execution_time(transaction_execution_time);
        executionHistory.setPercentage_affected(percentage_affected);
        executionHistory.setRecalculation_needed(recalculation_needed);
        executionHistory.setTransaction_outcome(transactionOutcome.name());

        executionHistoryService.addExecutionHistory(executionHistory);
    }
}
