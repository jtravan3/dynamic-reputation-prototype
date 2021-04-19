package com.jtravan.components;

import com.jtravan.dal.*;
import com.jtravan.dal.model.*;
import com.jtravan.model.*;
import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Random;

@Component
@CommonsLog
public class DataAccessManager {

    private final ExecutionHistoryServiceImpl executionHistoryService;
    private final UserServiceImpl userService;
    private final TransactionServiceImpl transactionService;
    private final RecalculationMetricServiceImpl recalculationMetricService;
    private final UseCaseMetricServiceImpl useCaseMetricService;
    private final WebClient webClient;
    private final Random random;

    @Autowired
    public DataAccessManager(@NonNull ExecutionHistoryServiceImpl executionHistoryService,
                             @NonNull UserServiceImpl userService,
                             @NonNull TransactionServiceImpl transactionService,
                             @NonNull RecalculationMetricServiceImpl recalculationMetricService,
                             @NonNull UseCaseMetricServiceImpl useCaseMetricService,
                             @NonNull WebClient webClient) {
        this.executionHistoryService = executionHistoryService;
        this.userService = userService;
        this.transactionService = transactionService;
        this.recalculationMetricService = recalculationMetricService;
        this.useCaseMetricService = useCaseMetricService;
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

    public UseCaseMetric getUseCaseMetricByName(String name) {
        return useCaseMetricService.getUseCaseMetricByName(name);
    }

    public void updateUseCaseMetrics(UseCaseMetric useCaseMetric) {
        useCaseMetricService.updateUseCaseMetrics(useCaseMetric);
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

    public void updateUser(User user) {
        userService.updateUser(user);
    }

    public void addRecalculationMetric(Integer num_of_execution_history,
                                       Integer num_of_users,
                                       Integer num_of_transactions,
                                       Double time_to_recalculate,
                                       String use_case) {

        RecalculationMetric recalculationMetric = new RecalculationMetric();
        recalculationMetric.setNum_of_execution_history(num_of_execution_history);
        recalculationMetric.setNum_of_users(num_of_users);
        recalculationMetric.setNum_of_transactions(num_of_transactions);
        recalculationMetric.setTime_to_recalculate(time_to_recalculate);
        recalculationMetric.setUse_case(use_case);

        recalculationMetricService.addRecalculationMetric(recalculationMetric);
        log.info("Successfully added recalculation metric");
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
    public void addPbsExecutionHistory(String userid,
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
                                       TransactionOutcome transactionOutcome,
                                       String overall_execution_id,
                                       String use_case, Category category) {

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
        executionHistory.setOverall_execution_id(overall_execution_id);
        executionHistory.setUse_case(use_case);
        executionHistory.setScheduler_type(SchedulerType.PBS.name());
        executionHistory.setCategory(category.name());
        executionHistory.setTransaction_type(TransactionType.NORMAL.name());

        executionHistoryService.addExecutionHistory(executionHistory);
        log.info("Successfully added PBS execution history");
    }

    @Async
    public void addTraditionalExecutionHistory(String userid,
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
                                    TransactionOutcome transactionOutcome,
                                    String overall_execution_id,
                                    String use_case,
                                    TransactionType transactionType) {

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
        executionHistory.setOverall_execution_id(overall_execution_id);
        executionHistory.setUse_case(use_case);
        executionHistory.setScheduler_type(SchedulerType.TRADITIONAL.name());
        executionHistory.setCategory(Category.NOT_APPLICABLE.name());
        executionHistory.setTransaction_type(transactionType.name());

        executionHistoryService.addExecutionHistory(executionHistory);
        log.info("Successfully added traditional execution history");
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
                                    TransactionOutcome transactionOutcome,
                                    String overall_execution_id,
                                    String use_case) {

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
        executionHistory.setOverall_execution_id(overall_execution_id);
        executionHistory.setUse_case(use_case);
        executionHistory.setScheduler_type(SchedulerType.DRP.name());
        executionHistory.setCategory(Category.NOT_APPLICABLE.name());
        executionHistory.setTransaction_type(TransactionType.NORMAL.name());

        executionHistoryService.addExecutionHistory(executionHistory);
        log.info("Successfully added DRP execution history");
    }
}
