package com.jtravan.dal;

import com.jtravan.dal.model.RecalculationMetric;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecalculationMetricRepository extends CrudRepository<RecalculationMetric, Long> {
}
