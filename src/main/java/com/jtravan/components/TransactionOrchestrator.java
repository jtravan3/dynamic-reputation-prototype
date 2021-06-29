package com.jtravan.components;

import com.jtravan.dal.model.Transaction;
import com.jtravan.dal.model.User;
import io.sentry.Sentry;
import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@CommonsLog
public class TransactionOrchestrator {

    private final DataAccessManager dataAccessManager;
    private final DynamicReputationScheduler dynamicReputationScheduler;
    private final TwoPhaseLockingScheduler twoPhaseLockingScheduler;
    private final PredictionBasedScheduler predictionBasedScheduler;
    private final NoLockingScheduler noLockingScheduler;
    private final Random random;

    @Autowired
    public TransactionOrchestrator(@NonNull DataAccessManager dataAccessManager,
                                   @NonNull DynamicReputationScheduler dynamicReputationScheduler,
                                   @NonNull TwoPhaseLockingScheduler twoPhaseLockingScheduler,
                                   @NonNull PredictionBasedScheduler predictionBasedScheduler,
                                   @NonNull NoLockingScheduler noLockingScheduler) {
        this.dataAccessManager = dataAccessManager;
        this.dynamicReputationScheduler = dynamicReputationScheduler;
        this.twoPhaseLockingScheduler = twoPhaseLockingScheduler;
        this.predictionBasedScheduler = predictionBasedScheduler;
        this.noLockingScheduler = noLockingScheduler;
        this.random = new Random();
    }

    public void beginExecutions(String useCase) throws InterruptedException {

        String overallExecutionId = UUID.randomUUID().toString();

        User user1 = dataAccessManager.getRandomUser();
        Transaction transaction1 = dataAccessManager.getRandomTransaction();
        User user2 = dataAccessManager.getRandomUser();
        Transaction transaction2 = dataAccessManager.getRandomTransaction();

        int randInt = random.nextInt(100);
        int randAbortInt = random.nextInt(100);

        if (!ObjectUtils.allNotNull(user1, transaction1, user2, transaction2)) {
            Sentry.captureMessage("User or Transaction was null. Gracefully handled it. Nothing to worry about.");
            return;
        }

        CompletableFuture<Void> noLockingFuture = noLockingScheduler.beginSchedulerExecution(useCase, user1, user2, transaction1,
                transaction2, overallExecutionId, randInt, randAbortInt);
        CompletableFuture<Void> traditionalFuture = twoPhaseLockingScheduler.beginSchedulerExecution(useCase, user1, user2, transaction1,
                transaction2, overallExecutionId, randInt, randAbortInt);
        CompletableFuture<Void> pbsFuture = predictionBasedScheduler.beginSchedulerExecution(useCase, user1, user2, transaction1,
                transaction2, overallExecutionId, randInt, randAbortInt);
        CompletableFuture<Void> drpFuture = dynamicReputationScheduler.beginSchedulerExecution(useCase, user1, user2, transaction1,
                transaction2, overallExecutionId, randInt, randAbortInt);

        CompletableFuture.allOf(traditionalFuture,pbsFuture,drpFuture,noLockingFuture).join();

        log.info("All Schedulers completed");
    }

}
