package com.jtravan.dal;

import com.jtravan.dal.model.UseCaseMetric;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UseCaseMetricsRepository extends CrudRepository<UseCaseMetric, Long> {
}
