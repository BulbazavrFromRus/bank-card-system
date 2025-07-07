package com.example.effective_mobile.service;

import com.example.effective_mobile.dto.CardDTO;
import com.example.effective_mobile.entity.Card;
import com.example.effective_mobile.entity.User;
import com.example.effective_mobile.repository.CardRepository;
import com.example.effective_mobile.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails userDetails;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CardService cardService;

    private User user;
    private Card card;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("newuser1")
                .role(User.Role.valueOf("USER"))
                .build();

        card = Card.builder()
                .id(1L)
                .cardNumber("1234567890123456")
                .holderName("John Doe")
                .expiryDate(LocalDate.of(2026, 12, 31))
                .status(Card.Status.ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .user(user)
                .build();

        // Mock SecurityContextHolder
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("newuser1");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetUserCards_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(Collections.singletonList(card));
        when(userRepository.findByUsername("newuser1")).thenReturn(Optional.of(user));
        when(cardRepository.findByUser(user, pageable)).thenReturn(cardPage);

        Page<CardDTO> result = cardService.getUserCards(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        CardDTO cardDTO = result.getContent().get(0);
        assertEquals(1L, cardDTO.getId());
        assertEquals("**** **** **** 3456", cardDTO.getMaskedCardNumber());
        assertEquals("John Doe", cardDTO.getHolderName());
        assertEquals("ACTIVE", cardDTO.getStatus());
        assertEquals(new BigDecimal("1000.00"), cardDTO.getBalance());
    }

    @Test
    void testGetUserCards_UserNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findByUsername("newuser1")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> cardService.getUserCards(pageable));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testCreateCard_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        CardDTO result = cardService.createCard("1234567890123456", "John Doe", LocalDate.of(2026, 12, 31), 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("**** **** **** 3456", result.getMaskedCardNumber());
        assertEquals("John Doe", result.getHolderName());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(new BigDecimal("0.00"), result.getBalance());
    }

    @Test
    void testCreateCard_InvalidCardNumber() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cardService.createCard("123", "John Doe", LocalDate.of(2026, 12, 31), 1L));
        assertEquals("Invalid card number", exception.getMessage());
    }

    @Test
    void testCreateCard_PastExpiryDate() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cardService.createCard("1234567890123456", "John Doe", LocalDate.of(2020, 12, 31), 1L));
        assertEquals("Card expiry date cannot be in the past", exception.getMessage());
    }

    @Test
    void testCreateCard_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cardService.createCard("1234567890123456", "John Doe", LocalDate.of(2026, 12, 31), 1L));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testRequestBlockCard_Success() {
        when(userRepository.findByUsername("newuser1")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        CardDTO result = cardService.requestBlockCard(1L);

        assertNotNull(result);
        assertEquals("BLOCKED", result.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void testRequestBlockCard_CardNotFound() {
        when(userRepository.findByUsername("newuser1")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> cardService.requestBlockCard(1L));
        assertEquals("Card not found", exception.getMessage());
    }

    @Test
    void testRequestBlockCard_CardNotBelongToUser() {
        User otherUser = User.builder().id(2L).username("otheruser").build();
        card.setUser(otherUser);
        when(userRepository.findByUsername("newuser1")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> cardService.requestBlockCard(1L));
        assertEquals("Card does not belong to user", exception.getMessage());
    }

    @Test
    void testRequestBlockCard_CardNotActive() {
        card.setStatus(Card.Status.BLOCKED);
        when(userRepository.findByUsername("newuser1")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> cardService.requestBlockCard(1L));
        assertEquals("Card is not active", exception.getMessage());
    }

    @Test
    void testTransfer_Success() {
        Card toCard = Card.builder()
                .id(2L)
                .cardNumber("9876543210987654")
                .holderName("Jane Doe")
                .expiryDate(LocalDate.of(2027, 12, 31))
                .status(Card.Status.ACTIVE)
                .balance(new BigDecimal("500.00"))
                .user(user)
                .build();

        when(userRepository.findByUsername("newuser1")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(cardRepository.save(any(Card.class))).thenReturn(card).thenReturn(toCard);

        CardDTO result = cardService.transfer(1L, 2L, new BigDecimal("200.00"));

        assertNotNull(result);
        assertEquals(new BigDecimal("800.00"), result.getBalance());
        assertEquals(new BigDecimal("700.00"), toCard.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void testTransfer_InsufficientBalance() {
        Card toCard = Card.builder()
                .id(2L)
                .cardNumber("9876543210987654")
                .holderName("Jane Doe")
                .expiryDate(LocalDate.of(2027, 12, 31))
                .status(Card.Status.ACTIVE)
                .balance(new BigDecimal("500.00"))
                .user(user)
                .build();

        when(userRepository.findByUsername("newuser1")).thenReturn(Optional.of(user));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cardService.transfer(1L, 2L, new BigDecimal("2000.00")));
        assertEquals("Insufficient balance", exception.getMessage());
    }

    @Test
    void testDeleteCard_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.deleteCard(1L);

        verify(cardRepository).delete(card);
    }

    @Test
    void testDeleteCard_CardNotFound() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> cardService.deleteCard(1L));
        assertEquals("Card not found", exception.getMessage());
    }
}