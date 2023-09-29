package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class AccountsService {

    private final AccountsRepository accountsRepository;
    private final NotificationService notificationService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Autowired
    public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
        this.accountsRepository = accountsRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }

    @Transactional(readOnly = true)
    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }

    public String transferMoney(String accountFromId, String accountToId, BigDecimal amount) {
        // Check if the transfer amount is valid
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount to transfer must be a positive number.");
        }

        Account accountFrom = accountsRepository.getAccount(accountFromId);
        Account accountTo = accountsRepository.getAccount(accountToId);

        // Check if accounts exist
        if (accountFrom == null || accountTo == null) {
            return "One or both accounts do not exist.";
        }

        // Use an asynchronous executor to perform the transfer asynchronously
        executorService.submit(() -> {
            performMoneyTransfer(accountFromId, accountToId, amount);
        });

        return "Transfer initiated";
    }


    @Transactional
    public void performMoneyTransfer(String accountFromId, String accountToId, BigDecimal amount) {
        Account accountFrom = accountsRepository.getAccount(accountFromId);
        Account accountTo = accountsRepository.getAccount(accountToId);

        // Check if accounts exist
        if (accountFrom == null || accountTo == null) {
            throw new IllegalArgumentException("One or both accounts do not exist.");
        }

        // Determine a consistent order for locking accounts
        String firstAccountId = accountFromId.compareTo(accountToId) < 0 ? accountFromId : accountToId;
        String secondAccountId = accountFromId.compareTo(accountToId) < 0 ? accountToId : accountFromId;

        // Lock the first account
        synchronized (firstAccountId.intern()) {
            // Lock the second account
            synchronized (secondAccountId.intern()) {
                // Check if the source account has sufficient funds
                if (accountFrom.getBalance().compareTo(amount) < 0) {
                    throw new IllegalArgumentException("Insufficient funds in the source account.");
                }

                // Deduct the amount from the source account
                accountFrom.setBalance(accountFrom.getBalance().subtract(amount));

                // Add the amount to the target account
                accountTo.setBalance(accountTo.getBalance().add(amount));

                // Notify both account holders asynchronously
                String notificationFrom = "Transferred $" + amount + " to Account " + accountToId;
                String notificationTo = "Received $" + amount + " from Account " + accountFromId;

                notifyAccountsAsync(accountFrom, notificationFrom);
                notifyAccountsAsync(accountTo, notificationTo);
            }
        }
    }

    @Async
    public void notifyAccountsAsync(Account account, String notification) {
        notificationService.notifyAboutTransfer(account, notification);
    }
}
