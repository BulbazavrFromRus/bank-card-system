package com.example.effective_mobile.service;

import com.example.effective_mobile.dto.CardDTO;
import com.example.effective_mobile.entity.Card;
import com.example.effective_mobile.entity.User;
import com.example.effective_mobile.repository.CardRepository;
import com.example.effective_mobile.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<CardDTO> getUserCards(Pageable pageable) {
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return cardRepository.findByUser(user, pageable).map(this::convertToDTO);
    }

    public Page<CardDTO> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable).map(this::convertToDTO);
    }

    public CardDTO createCard(String cardNumber, String holderName, LocalDate expiryDate, Long userId) {
        if (!isValidCardNumber(cardNumber)) {
            throw new IllegalArgumentException("Invalid card number");
        }
        if (expiryDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Card expiry date cannot be in the past");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Card card = Card.builder()
                .cardNumber(cardNumber)
                .holderName(holderName)
                .expiryDate(expiryDate)
                .status(Card.Status.ACTIVE)
                .balance(new java.math.BigDecimal("0.00"))
                .user(user)
                .build();

        Card savedCard = cardRepository.save(card);
        return convertToDTO(savedCard);
    }

    public CardDTO requestBlockCard(Long cardId) {
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Card does not belong to user");
        }
        if (card.getStatus() != Card.Status.ACTIVE) {
            throw new RuntimeException("Card is not active");
        }

        card.setStatus(Card.Status.BLOCKED);
        Card updatedCard = cardRepository.save(card);
        return convertToDTO(updatedCard);
    }

    public CardDTO blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (card.getStatus() != Card.Status.ACTIVE) {
            throw new RuntimeException("Card is not active");
        }

        card.setStatus(Card.Status.BLOCKED);
        Card updatedCard = cardRepository.save(card);
        return convertToDTO(updatedCard);
    }

    public CardDTO activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (card.getStatus() != Card.Status.BLOCKED) {
            throw new RuntimeException("Card is not blocked");
        }

        card.setStatus(Card.Status.ACTIVE);
        Card updatedCard = cardRepository.save(card);
        return convertToDTO(updatedCard);
    }

    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        cardRepository.delete(card);
    }

    @Transactional
    public CardDTO transfer(Long fromCardId, Long toCardId, BigDecimal amount) {
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new RuntimeException("Source card not found"));
        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new RuntimeException("Destination card not found"));

        if (!fromCard.getUser().getId().equals(user.getId()) || !toCard.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Both cards must belong to the user");
        }
        if (fromCard.getStatus() != Card.Status.ACTIVE || toCard.getStatus() != Card.Status.ACTIVE) {
            throw new RuntimeException("Both cards must be active");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be positive");
        }
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        return convertToDTO(fromCard);
    }

    private CardDTO convertToDTO(Card card) {
        CardDTO dto = new CardDTO();
        dto.setId(card.getId());
        dto.setMaskedCardNumber(maskCardNumber(card.getCardNumber()));
        dto.setHolderName(card.getHolderName());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setStatus(card.getStatus().name());
        dto.setBalance(card.getBalance());
        return dto;
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "**** **** **** ****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    private boolean isValidCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.matches("\\d{16}");
    }
}