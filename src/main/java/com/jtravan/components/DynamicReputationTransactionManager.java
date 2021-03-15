package com.jtravan.components;

import com.jtravan.dal.ExecutionHistoryServiceImpl;
import com.jtravan.dal.model.ExecutionHistory;
import com.jtravan.model.LockingAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class DynamicReputationTransactionManager {

    private final ExecutionHistoryServiceImpl executionHistoryService;

    @Autowired
    public DynamicReputationTransactionManager(ExecutionHistoryServiceImpl executionHistoryService) {
        this.executionHistoryService = executionHistoryService;
    }

    @Async
    public void addExecutionHistory(String userid,
                                    Double user_ranking,
                                    Double transaction_commit_ranking,
                                    Double transaction_system_ranking,
                                    Double transaction_eff_ranking,
                                    Integer transaction_num_of_operations,
                                    LockingAction action_taken,
                                    Double reputation_score,
                                    Double transaction_execution_time,
                                    Double percentage_affected,
                                    Boolean recalculation_needed) {

        ExecutionHistory executionHistory = new ExecutionHistory();
        executionHistory.setUserid(userid);
        executionHistory.setUser_ranking(user_ranking);
        executionHistory.setTransaction_commit_ranking(transaction_commit_ranking);
        executionHistory.setTransaction_system_ranking(transaction_system_ranking);
        executionHistory.setTransaction_eff_ranking(transaction_eff_ranking);
        executionHistory.setTransaction_num_of_operations(transaction_num_of_operations);
        executionHistory.setAction_taken(action_taken.name());
        executionHistory.setReputation_score(reputation_score);
        executionHistory.setTransaction_execution_time(transaction_execution_time);
        executionHistory.setPercentage_affected(percentage_affected);
        executionHistory.setRecalculation_needed(recalculation_needed);

        executionHistoryService.addExecutionHistory(executionHistory);
    }
}
