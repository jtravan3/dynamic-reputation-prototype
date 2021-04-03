package com.jtravan.model;

import com.jtravan.dal.model.Transaction;
import com.jtravan.dal.model.User;
import lombok.Data;

@Data
public class DominancePair {
    private DominanceType dominanceType;
    private User dominatingUser;
    private Transaction dominatingTransaction;
    private User weakUser;
    private Transaction weakTransaction;
}
