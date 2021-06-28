package com.jtravan.components;

import com.jtravan.dal.model.Transaction;
import com.jtravan.dal.model.User;
import lombok.NonNull;
import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Component
public abstract class TransactionScheduler {

    private final ConfigurationService configurationService;
    private final Random random;

    @Autowired
    public TransactionScheduler(@NonNull ConfigurationService configurationService) {
        this.configurationService = configurationService;
        this.random = new Random();
    }

    public Double getTransactionExecutionTime(Transaction transaction) {
        return Precision.round((random.nextDouble() * random.nextInt(250)) * transaction.getTransaction_num_of_operations(), 5);
    }

    public Double getTransactionGrowingPhaseTime(Transaction transaction) {
        return Precision.round((random.nextDouble() * random.nextInt(10)) * transaction.getTransaction_num_of_operations(), 5);
    }

    public Double getTransactionShrinkingPhaseTime(Transaction transaction) {
        return Precision.round((random.nextDouble() * random.nextInt(10)) * transaction.getTransaction_num_of_operations(), 5);
    }

    public void executeLockPhase(Double executionTime) throws InterruptedException {
        Thread.sleep(executionTime.intValue());
    }

    public void executeTransaction(Double executionTime) throws InterruptedException {
        Thread.sleep(executionTime.intValue());
        configurationService.incrementTotalTransactionsExecuted();
    }

    abstract CompletableFuture<Void> beginSchedulerExecution(String useCase,
                                                             User user1,
                                                             User user2,
                                                             Transaction transaction1,
                                                             Transaction transaction2,
                                                             String overallExecutionId,
                                                             int randInt, int randAbortInt) throws InterruptedException;
}
