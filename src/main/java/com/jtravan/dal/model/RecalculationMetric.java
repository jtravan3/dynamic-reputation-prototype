package com.jtravan.dal.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "recalculation_metrics")
public class RecalculationMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @Column(name="num_of_execution_history")
    private Integer num_of_execution_history;
    @Column(name="num_of_users")
    private Integer num_of_users;
    @Column(name="num_of_transactions")
    private Integer num_of_transactions;
    @Column(name="time_to_recalculate")
    private Double time_to_recalculate;
    @Column(name = "time_executed", insertable = false, updatable = false)
    @CreationTimestamp
    private Date time_executed;
}
