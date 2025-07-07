package com.example.effective_mobile.controller;

import com.example.effective_mobile.dto.CardDTO;
import com.example.effective_mobile.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCardController {

    @Autowired
    private CardService cardService;

    @GetMapping("/cards")
    @Operation(summary = "Get all cards", description = "Returns a paginated list of all cards (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<CardDTO>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(cardService.getAllCards(pageable));
    }

    @PostMapping("/cards")
    @Operation(summary = "Create a card", description = "Creates a new card for a user (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<CardDTO> createCard(@Valid @RequestBody CardRequest cardRequest) {
        CardDTO card = cardService.createCard(
                cardRequest.getCardNumber(),
                cardRequest.getHolderName(),
                cardRequest.getExpiryDate(),
                cardRequest.getUserId()
        );
        return ResponseEntity.ok(card);
    }

    @PutMapping("/cards/{id}/block")
    @Operation(summary = "Block a card", description = "Blocks a card by ID (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card blocked"),
            @ApiResponse(responseCode = "400", description = "Card is not active"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<CardDTO> blockCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.blockCard(id));
    }

    @PutMapping("/cards/{id}/activate")
    @Operation(summary = "Activate a card", description = "Activates a blocked card by ID (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card activated"),
            @ApiResponse(responseCode = "400", description = "Card is not blocked"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<CardDTO> activateCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.activateCard(id));
    }

    @DeleteMapping("/cards/{id}")
    @Operation(summary = "Delete a card", description = "Deletes a card by ID (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Card deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @Data
    static class CardRequest {
        @NotBlank(message = "Card number is required")
        private String cardNumber;
        @NotBlank(message = "Holder name is required")
        private String holderName;
        private LocalDate expiryDate;
        private Long userId;
    }
}