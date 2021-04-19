package com.jtravan.model;

import com.jtravan.dal.model.Transaction;
import com.jtravan.dal.model.User;
import lombok.Data;

@Data
public class PbsDominancePair {
    private Category dominatingCategory;
    private Transaction dominatingTransaction;
    private User dominatingUser;
    private Category weakCategory;
    private Transaction weakTransaction;
    private User weakUser;
}
