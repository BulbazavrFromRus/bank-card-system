package com.example.effective_mobile.controller;

import com.example.effective_mobile.dto.CardDTO;
import com.example.effective_mobile.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
public class UserCardController {

    @Autowired
    private CardService cardService;

    @GetMapping("/cards")
    @Operation(summary = "Get user's cards", description = "Returns a paginated list of cards for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Page<CardDTO>> getUserCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(cardService.getUserCards(pageable));
    }

    @PostMapping("/cards/{id}/block")
    @Operation(summary = "Request card block", description = "Requests to block a card owned by the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Card blocked"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Card does not belong to user"),
            @ApiResponse(responseCode = "404", description = "Card not found"),
            @ApiResponse(responseCode = "400", description = "Card is not active")
    })
    public ResponseEntity<CardDTO> requestBlockCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.requestBlockCard(id));
    }
}