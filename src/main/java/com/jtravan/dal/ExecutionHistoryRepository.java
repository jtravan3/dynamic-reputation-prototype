package com.jtravan.dal;

import com.jtravan.dal.model.ExecutionHistory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecutionHistoryRepository extends CrudRepository<ExecutionHistory, Long> {
}
