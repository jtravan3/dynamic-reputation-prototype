package com.jtravan.dal;

import com.jtravan.dal.model.OverallExecutionHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OverallExecutionHistoryServiceImpl implements OverallExecutionHistoryService {

    private final OverallExecutionHistoryRepository overallExecutionHistoryRepository;

    @Autowired
    public OverallExecutionHistoryServiceImpl(OverallExecutionHistoryRepository overallExecutionHistoryRepository) {
        this.overallExecutionHistoryRepository = overallExecutionHistoryRepository;
    }

    @Override
    public void addOverallExecutionMetric(OverallExecutionHistory overallExecutionHistory) {
        overallExecutionHistoryRepository.save(overallExecutionHistory);
    }
}
