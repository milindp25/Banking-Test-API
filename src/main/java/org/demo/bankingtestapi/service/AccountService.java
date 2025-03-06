package org.demo.bankingtestapi.service;

import org.demo.bankingtestapi.entity.Account;
import org.demo.bankingtestapi.exception.BadRequestException;
import org.demo.bankingtestapi.repository.AccountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final LedgerService ledgerService;
    private final AuditService auditService;

    // Maximum allowed deposit amount (example: $10,000)
    private static final BigDecimal MAX_DEPOSIT_AMOUNT = new BigDecimal("10000");

    public AccountService(AccountRepository accountRepository, LedgerService ledgerService, AuditService auditService) {
        this.accountRepository = accountRepository;
        this.ledgerService = ledgerService;
        this.auditService = auditService;
    }

    @Transactional
    public void deposit(String accountNumber, BigDecimal amount) {
        // Retrieve the account
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BadRequestException("Account not found"));

        // Check if account status is active
        if (!"Active".equalsIgnoreCase(account.getStatus())) {
            throw new BadRequestException("Account is not active");
        }

        // Validate deposit amount is within limits
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Deposit amount must be greater than zero");
        }
        if (amount.compareTo(MAX_DEPOSIT_AMOUNT) > 0) {
            throw new BadRequestException("Deposit amount exceeds the maximum allowed limit of $" + MAX_DEPOSIT_AMOUNT);
        }

        // Verify the account belongs to the authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String currentUsername = auth.getName();
//        if (!account.getUser().getEmail().equalsIgnoreCase(currentUsername)) {
//            throw new BadRequestException("This account does not belong to the authenticated user");
//        }

        // Update the account balance
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        // Create a ledger entry for this deposit
        ledgerService.createLedgerEntry(account, amount, "Deposit");

        // Log the deposit in the audit table using the authenticated user from the account
        auditService.logAction(account.getUser(), "Deposit of $" + amount + " successful. New balance: $" + account.getBalance());
    }
}
