package com.jtravan.model;

import lombok.Data;

import java.util.List;

@Data
public class RandomUsernameResponse {
    private List<RandomUsernameEntry> results;
}
