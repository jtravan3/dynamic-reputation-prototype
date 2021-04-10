package com.jtravan.dal;

import com.jtravan.dal.model.ExecutionHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class ExecutionHistoryServiceImpl implements ExecutionHistoryService {

    private final ExecutionHistoryRepository executionHistoryRepository;

    @Autowired
    public ExecutionHistoryServiceImpl(ExecutionHistoryRepository executionHistoryRepository) {
        this.executionHistoryRepository = executionHistoryRepository;
    }

    @Override
    public void addExecutionHistory(ExecutionHistory executionHistory) {
        executionHistoryRepository.save(executionHistory);
    }

    @Override
    public List<ExecutionHistory> getAllHistory() {
        List<ExecutionHistory> rtnList = new LinkedList<>();
        executionHistoryRepository.findAll().forEach(rtnList::add);
        return rtnList;
    }
}
