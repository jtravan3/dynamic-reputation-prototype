package com.jtravan.dal;

import com.jtravan.dal.model.Transaction;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionServiceImpl(@NonNull TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void addTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    @Override
    public Transaction getTransactionById(Integer id) {
        return transactionRepository.findById(id).orElse(null);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        List<Transaction> rtnList = new LinkedList<>();
        transactionRepository.findAll().forEach(rtnList::add);
        return rtnList;
    }

    @Override
    public void updateTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }
}
