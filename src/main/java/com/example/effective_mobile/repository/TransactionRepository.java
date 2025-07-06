package com.example.effective_mobile.repository;

import com.example.effective_mobile.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}
