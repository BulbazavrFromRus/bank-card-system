package com.example.effective_mobile.service;


import com.example.effective_mobile.dto.TransactionDTO;
import com.example.effective_mobile.entity.Card;
import com.example.effective_mobile.entity.Transaction;
import com.example.effective_mobile.repository.CardRepository;
import com.example.effective_mobile.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public TransactionDTO transfer(Long fromCardId, Long toCardId, BigDecimal amount) {
        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new RuntimeException("Source card not found"));
        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new RuntimeException("Destination card not found"));

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
        if (!fromCard.getStatus().equals(Card.Status.ACTIVE) || !toCard.getStatus().equals(Card.Status.ACTIVE)) {
            throw new RuntimeException("Card is not active");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        Transaction transaction = Transaction.builder()
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(amount)
                .transactionDate(LocalDateTime.now())
                .status(Transaction.Status.COMPLETED)
                .build();

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
        transactionRepository.save(transaction);

        return convertToDTO(transaction);
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setFromCardId(transaction.getFromCard().getId());
        dto.setToCardId(transaction.getToCard().getId());
        dto.setAmount(transaction.getAmount());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setStatus(transaction.getStatus());
        return dto;
    }
}