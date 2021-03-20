package com.jtravan.components;

import com.jtravan.dal.model.Transaction;
import com.jtravan.dal.model.User;
import com.jtravan.model.DominanceType;
import com.jtravan.model.LockingAction;
import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;

@Component
@CommonsLog
public class TransactionOrchestrator {

    private final DataAccessManager dataAccessManager;
    private final Random random;

    @Autowired
    public TransactionOrchestrator(@NonNull DataAccessManager dataAccessManager) {
        this.dataAccessManager = dataAccessManager;
        this.random = new Random();
    }

    public void executeTransaction() throws InterruptedException {
        User user1 = dataAccessManager.getRandomUser();
        Transaction transaction1 = dataAccessManager.getRandomTransaction();

        Double t1executionTime = Precision.round((random.nextDouble() * random.nextInt(250)) * transaction1.getTransaction_num_of_operations(), 5);
        String t1RepScore = "<"
                + Precision.round(transaction1.getTransaction_commit_ranking(), 5)
                + ","
                + Precision.round(transaction1.getTransaction_eff_ranking(), 5)
                + ","
                + Precision.round(user1.getUser_ranking(), 5)
                + ","
                + Precision.round(transaction1.getTransaction_system_ranking(), 5)
                + ">";

        log.info("U1: " + user1);
        log.info("T1: " + transaction1);
        log.info("T1 Execution Time: " + t1executionTime);
        log.info("T1 Reputation Score: " + t1RepScore);


        User user2 = dataAccessManager.getRandomUser();
        Transaction transaction2 = dataAccessManager.getRandomTransaction();

        Double t2executionTime = Precision.round((random.nextDouble() * random.nextInt(250)) * transaction2.getTransaction_num_of_operations(), 5);
        String t2RepScore = "<"
                + Precision.round(transaction2.getTransaction_commit_ranking(), 5)
                + ","
                + Precision.round(transaction2.getTransaction_eff_ranking(), 5)
                + ","
                + Precision.round(user2.getUser_ranking(), 5)
                + ","
                + Precision.round(transaction2.getTransaction_system_ranking(), 5)
                + ">";
        log.info("U2: " + user2);
        log.info("T2: " + transaction2);
        log.info("T2 Execution Time: " + t2executionTime);
        log.info("T2 Reputation Score: " + t2RepScore);

        Integer randInt = random.nextInt(100);
        // Conflict
        if (randInt <= 10) {
            log.info("Conflicting Transactions");
        } else { // No conflict
            log.info("Non-Conflicting Transactions");
//            Thread.sleep(t1executionTime.intValue());
//            dataAccessManager.addExecutionHistory(user1.getUserid(), user1.getUser_ranking(),
//                    transaction1.getTransaction_id(), transaction1.getTransaction_commit_ranking(),
//                    transaction1.getTransaction_system_ranking(), transaction1.getTransaction_eff_ranking(),
//                    transaction1.getTransaction_num_of_operations(), t1RepScore, LockingAction.GRANT,
//                    DominanceType.NO_CONFLICT, t1executionTime, 0.0, false);
//            Thread.sleep(t2executionTime.intValue());
//            dataAccessManager.addExecutionHistory(user2.getUserid(), user2.getUser_ranking(),
//                    transaction2.getTransaction_id(), transaction2.getTransaction_commit_ranking(),
//                    transaction2.getTransaction_system_ranking(), transaction2.getTransaction_eff_ranking(),
//                    transaction2.getTransaction_num_of_operations(), t2RepScore, LockingAction.GRANT,
//                    DominanceType.NO_CONFLICT,t2executionTime, 0.0, false);
        }

    }
}
