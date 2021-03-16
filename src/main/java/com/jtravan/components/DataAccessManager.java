package com.jtravan.components;

import com.jtravan.dal.ExecutionHistoryServiceImpl;
import com.jtravan.dal.UserServiceImpl;
import com.jtravan.dal.model.ExecutionHistory;
import com.jtravan.dal.model.Transaction;
import com.jtravan.dal.model.User;
import com.jtravan.model.LockingAction;
import com.jtravan.model.RandomUsernameResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class DataAccessManager {

    private final ExecutionHistoryServiceImpl executionHistoryService;
    private final UserServiceImpl userService;
    private final WebClient webClient;

    @Autowired
    public DataAccessManager(@NonNull ExecutionHistoryServiceImpl executionHistoryService,
                             @NonNull UserServiceImpl userService,
                             @NonNull WebClient webClient) {
        this.executionHistoryService = executionHistoryService;
        this.userService = userService;
        this.webClient = webClient;
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

    @Async
    public void addExecutionHistory(String userid,
                                    Double user_ranking,
                                    String transaction_id,
                                    Double transaction_commit_ranking,
                                    Double transaction_system_ranking,
                                    Double transaction_eff_ranking,
                                    Integer transaction_num_of_operations,
                                    Double reputation_score,
                                    LockingAction action_taken,
                                    Double transaction_execution_time,
                                    Double percentage_affected,
                                    Boolean recalculation_needed) {

        User user = new User();
        Transaction transaction = new Transaction();
        ExecutionHistory executionHistory = new ExecutionHistory();

        user.setUserid(userid);
        user.setUser_ranking(user_ranking);
        transaction.setTransaction_id(transaction_id);
        transaction.setTransaction_commit_ranking(transaction_commit_ranking);
        transaction.setTransaction_system_ranking(transaction_system_ranking);
        transaction.setTransaction_eff_ranking(transaction_eff_ranking);
        transaction.setTransaction_num_of_operations(transaction_num_of_operations);

        executionHistory.setUser(user);
        executionHistory.setTransaction(transaction);
        executionHistory.setUser_ranking(user_ranking);
        executionHistory.setTransaction_commit_ranking(transaction_commit_ranking);
        executionHistory.setTransaction_system_ranking(transaction_system_ranking);
        executionHistory.setTransaction_eff_ranking(transaction_eff_ranking);
        executionHistory.setTransaction_num_of_operations(transaction_num_of_operations);
        executionHistory.setReputation_score(reputation_score);
        executionHistory.setAction_taken(action_taken.name());
        executionHistory.setTransaction_execution_time(transaction_execution_time);
        executionHistory.setPercentage_affected(percentage_affected);
        executionHistory.setRecalculation_needed(recalculation_needed);

        executionHistoryService.addExecutionHistory(executionHistory);
    }
}
