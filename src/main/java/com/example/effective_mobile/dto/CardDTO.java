package com.example.effective_mobile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardDTO {
    @Schema(description = "Card ID", example = "1")
    private Long id;

    @Schema(description = "Masked card number (last 4 digits visible)", example = "**** **** **** 1234")
    private String maskedCardNumber;

    @Schema(description = "Card holder's name", example = "John Doe")
    private String holderName;

    @Schema(description = "Card expiry date", example = "2026-12-31")
    private LocalDate expiryDate;

    @Schema(description = "Card status", example = "ACTIVE")
    private String status;

    @Schema(description = "Card balance", example = "1000.00")
    private BigDecimal balance;
}