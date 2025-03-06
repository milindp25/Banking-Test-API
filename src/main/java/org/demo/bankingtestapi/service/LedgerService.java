package org.demo.bankingtestapi.service;

import org.demo.bankingtestapi.entity.Account;
import org.demo.bankingtestapi.entity.LedgerEntry;
import org.demo.bankingtestapi.repository.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public void createLedgerEntry(Account account, BigDecimal amount, String transactionType) {
        LedgerEntry entry = new LedgerEntry();
        entry.setAccount(account);
        entry.setTransactionType(transactionType);
        entry.setAmount(amount);
        // After updating the account, assume account.getBalance() returns the new balance.
        entry.setBalanceAfter(account.getBalance());
        entry.setTransactionDate(Instant.now());
        entry.setCreditDebit("Credit"); // For deposits, it's typically a credit
        ledgerEntryRepository.save(entry);
    }
}
