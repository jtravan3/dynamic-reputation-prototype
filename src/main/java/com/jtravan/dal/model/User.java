package com.jtravan.dal.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @Column(name="userid")
    private String userid;
    @Column(name="user_ranking")
    private Double user_ranking;
}
