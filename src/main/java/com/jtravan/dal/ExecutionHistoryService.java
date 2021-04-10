package com.jtravan.dal;

import com.jtravan.dal.model.ExecutionHistory;

import java.util.List;

public interface ExecutionHistoryService {
    void addExecutionHistory(ExecutionHistory executionHistory);
    List<ExecutionHistory> getAllHistory();
}
