package com.example.effective_mobile.controller;


import com.example.effective_mobile.dto.TransactionDTO;
import com.example.effective_mobile.service.TransactionService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionDTO> transfer(
            @RequestBody TransferRequest transferRequest) {
        return ResponseEntity.ok(transactionService.transfer(
                transferRequest.getFromCardId(),
                transferRequest.getToCardId(),
                transferRequest.getAmount()));
    }

    @Data
    static class TransferRequest {
        private Long fromCardId;
        private Long toCardId;
        private BigDecimal amount;
    }
}