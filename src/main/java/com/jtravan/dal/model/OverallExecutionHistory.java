package com.jtravan.dal.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "overall_execution_history")
public class OverallExecutionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @Column(name="overall_execution_id")
    private String overall_execution_id;
    @Column(name="overall_execution_time")
    private Double overall_execution_time;
    @Column(name="scheduler_type")
    private String scheduler_type;
    @Column(name = "time_executed", insertable = false, updatable = false)
    @CreationTimestamp
    private Date time_executed;
}
