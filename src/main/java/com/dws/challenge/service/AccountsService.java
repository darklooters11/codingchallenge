package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountsService {

    private final AccountsRepository accountsRepository;
    private final NotificationService notificationService;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
        this.accountsRepository = accountsRepository;
        this.notificationService = notificationService;
    }

    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }

    public synchronized String transferMoney(String accountFromId, String accountToId, BigDecimal amount) {
        Account accountFrom = accountsRepository.getAccount(accountFromId);
        Account accountTo = accountsRepository.getAccount(accountToId);

        if (accountFrom == null || accountTo == null) {
            throw new IllegalArgumentException("One or both accounts do not exist.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount to transfer must be a positive number.");
        }

        if (accountFrom.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds in the source account.");
        }

        // Deduct the amount from the source account
        accountFrom.setBalance(accountFrom.getBalance().subtract(amount));

        // Add the amount to the target account
        accountTo.setBalance(accountTo.getBalance().add(amount));
        // Notify both account holders
        String notificationFrom = "Transferred $" + amount + " to Account " + accountToId;
        String notificationTo = "Received $" + amount + " from Account " + accountFromId;

        // Use the notification service to notify account holders
        notificationService.notifyAboutTransfer(accountFrom, notificationFrom);
        notificationService.notifyAboutTransfer(accountTo, notificationTo);
        return notificationFrom;
    }
}
