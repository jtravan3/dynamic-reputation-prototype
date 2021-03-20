package com.jtravan.dal.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name="id")
    private Integer id;
    @Column(name="transaction_id")
    private String transaction_id;
    @Column(name="transaction_commit_ranking")
    private Double transaction_commit_ranking;
    @Column(name="transaction_system_ranking")
    private Double transaction_system_ranking;
    @Column(name="transaction_eff_ranking")
    private Double transaction_eff_ranking;
    @Column(name="transaction_num_of_operations")
    private Integer transaction_num_of_operations;
}
