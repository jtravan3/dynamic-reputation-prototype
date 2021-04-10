package com.jtravan.dal;

import com.jtravan.dal.model.Transaction;

import java.util.List;

public interface TransactionService {
    void addTransaction(Transaction transaction);
    Transaction getTransactionById(Integer id);
    List<Transaction> getAllTransactions();
    void updateTransaction(Transaction transaction);
}
