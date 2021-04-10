package com.jtravan.dal;

import com.jtravan.dal.model.RecalculationMetric;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RecalculationMetricServiceImpl implements RecalculationMetricService {

    private final RecalculationMetricRepository recalculationMetricRepository;

    @Autowired
    public RecalculationMetricServiceImpl(@NonNull RecalculationMetricRepository recalculationMetricRepository) {
        this.recalculationMetricRepository = recalculationMetricRepository;
    }

    @Override
    public void addRecalculationMetric(RecalculationMetric recalculationMetric) {
        recalculationMetricRepository.save(recalculationMetric);
    }
}
