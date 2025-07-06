package com.example.effective_mobile.dto;


import com.example.effective_mobile.entity.Transaction.Status;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private Long id;
    private Long fromCardId;
    private Long toCardId;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private Status status;
}