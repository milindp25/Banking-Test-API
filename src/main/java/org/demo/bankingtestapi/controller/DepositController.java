package org.demo.bankingtestapi.controller;

import jakarta.validation.Valid;
import org.demo.bankingtestapi.DTO.DepositDto;
import org.demo.bankingtestapi.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/deposits")
public class DepositController {

    private final AccountService accountService;

    public DepositController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<?> deposit(@Valid @RequestBody DepositDto depositDto) {
        accountService.deposit(depositDto.getAccountNumber(), depositDto.getAmount());
        return ResponseEntity.ok(Map.of("message", "Deposit successful."));
    }
}
