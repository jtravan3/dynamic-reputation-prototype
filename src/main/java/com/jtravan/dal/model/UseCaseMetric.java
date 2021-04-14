package com.jtravan.dal.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "use_case_metrics")
public class UseCaseMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @Column(name="name")
    private String name;
    @Column(name="total_transactions_executed")
    private Integer total_transactions_executed;
    @Column(name="recalculation_percentage")
    private Integer recalculation_percentage;
    @Column(name="total_affected_transactions")
    private Integer total_affected_transactions;
    @Column(name="conflicting_percentage")
    private Integer conflicting_percentage;
    @Column(name="abort_percentage")
    private Integer abort_percentage;
    @Column(name = "time_executed", insertable = false, updatable = false)
    @CreationTimestamp
    private Date time_executed;
}
