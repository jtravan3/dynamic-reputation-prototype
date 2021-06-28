package com.jtravan.dal;

import com.jtravan.dal.model.OverallExecutionHistory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OverallExecutionHistoryRepository extends CrudRepository<OverallExecutionHistory, Long> {
}
