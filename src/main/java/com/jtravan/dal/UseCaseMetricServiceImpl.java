package com.jtravan.dal;

import com.jtravan.dal.model.UseCaseMetric;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.stream.StreamSupport;

@Component
public class UseCaseMetricServiceImpl implements UseCaseMetricService {

    private final UseCaseMetricsRepository useCaseMetricsRepository;

    @Autowired
    public UseCaseMetricServiceImpl(@NonNull UseCaseMetricsRepository useCaseMetricsRepository) {
        this.useCaseMetricsRepository = useCaseMetricsRepository;
    }

    @Override
    @Cacheable(value="usecases", key="#name")
    public UseCaseMetric getUseCaseMetricByName(@NonNull String name) {
        return StreamSupport
                .stream(useCaseMetricsRepository.findAll().spliterator(), false)
                .filter(useCaseMetric -> name.equals(useCaseMetric.getName()))
                .findFirst().orElse(null);

    }
}
