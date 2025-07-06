package com.example.effective_mobile.dto;


import com.example.effective_mobile.entity.Card.Status;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardDTO {
    private Long id;
    private Long userId;
    private String maskedCardNumber; // Маскированный номер карты (**** **** **** 1234)
    private String holderName;
    private LocalDate expiryDate;
    private Status status;
    private BigDecimal balance;
}
