package com.jtravan.dal.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "execution_history")
public class ExecutionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userid")
    private User user;
    @Column(name="user_ranking")
    private Double user_ranking;
    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;
    @Column(name="transaction_commit_ranking")
    private Double transaction_commit_ranking;
    @Column(name="transaction_system_ranking")
    private Double transaction_system_ranking;
    @Column(name="transaction_eff_ranking")
    private Double transaction_eff_ranking;
    @Column(name="transaction_num_of_operations")
    private Integer transaction_num_of_operations;
    @Column(name="reputation_score")
    private Double reputation_score;
    @Column(name="action_taken")
    private String action_taken;
    @Column(name="transaction_execution_time")
    private Double transaction_execution_time;
    @Column(name="percentage_affected")
    private Double percentage_affected;
    @Column(name="recalculation_needed")
    private Boolean recalculation_needed;
    @Column(name = "time_executed", insertable = false, updatable = false)
    @CreationTimestamp
    private Date time_executed;
}