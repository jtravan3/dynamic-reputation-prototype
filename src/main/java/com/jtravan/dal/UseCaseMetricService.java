package com.jtravan.dal;

import com.jtravan.dal.model.UseCaseMetric;

public interface UseCaseMetricService {
    UseCaseMetric getUseCaseMetricByName(String name);
    void updateUseCaseMetrics(UseCaseMetric useCaseMetric);
}
