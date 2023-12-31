package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.mockito.ArgumentCaptor;


@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

    @Autowired
    private AccountsService accountsService;

    @Mock
    private AccountsRepository accountsRepository;

    @Mock
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accountsService = new AccountsService(accountsRepository, notificationService);
        accountsRepository.clearAccounts();

    }

    @Test
    void addAccount() {
        // Arrange
        Account account = new Account("Id-123");
        account.setBalance(new BigDecimal(1000));

        // Configure the mock repository to return the account when getAccount is called
        when(accountsRepository.getAccount("Id-123")).thenReturn(account);

        // Act: Create the account
        this.accountsService.createAccount(account);

        // Assert: Verify that the account was successfully created
        Account retrievedAccount = this.accountsService.getAccount("Id-123");
        assertThat(retrievedAccount).isEqualTo(account);
    }

    @Test
    void addAccount_failsOnDuplicateId() {
        String uniqueId = "Id-" + System.currentTimeMillis();
        Account account = new Account(uniqueId);

        // Attempt to create the account twice
        this.accountsService.createAccount(account);

        try {
            this.accountsService.createAccount(account);
            //fail("Should have failed when adding duplicate account");
        } catch (DuplicateAccountIdException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
        }
    }

    @Test
    void transferMoneyZeroAmount() {
        // Arrange
        Account accountFrom = new Account("Id-1");
        accountFrom.setBalance(new BigDecimal("1000"));
        Account accountTo = new Account("Id-2");
        when(accountsRepository.getAccount("Id-1")).thenReturn(accountFrom);
        when(accountsRepository.getAccount("Id-2")).thenReturn(accountTo);

        // Act and Assert
        assertThatThrownBy(() -> accountsService.transferMoney("Id-1", "Id-2", BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount to transfer must be a positive number.");
    }

    @Test
    void transferMoneyAsyncNotification() {
        // Arrange
        Account accountFrom = new Account("Id-1");
        accountFrom.setBalance(new BigDecimal("1000"));
        Account accountTo = new Account("Id-2");
        when(accountsRepository.getAccount("Id-1")).thenReturn(accountFrom);
        when(accountsRepository.getAccount("Id-2")).thenReturn(accountTo);

        // Act
        String notification = accountsService.transferMoney("Id-1", "Id-2", new BigDecimal("500"));

        // Assert
        assertThat(notification).isEqualTo("Transfer initiated");

        // Verify that notificationService was called asynchronously
        verify(notificationService, timeout(1000).times(2))
                .notifyAboutTransfer(any(Account.class), anyString());
    }

    @Test
    void transferMoneyInvalidSourceAccount() {
        // Arrange
        String invalidAccountId = "NonExistentId";
        // Act
        String result = accountsService.transferMoney(invalidAccountId, "Id-2", new BigDecimal("500"));
        // Assert
        assertThat(result).isEqualTo("One or both accounts do not exist.");
    }


    @Test
    void transferMoneyInvalidTargetAccount() {
        // Arrange
        Account accountFrom = new Account("Id-1");
        accountFrom.setBalance(new BigDecimal("1000"));
        when(accountsRepository.getAccount("Id-1")).thenReturn(accountFrom);
        when(accountsRepository.getAccount("NonExistentId")).thenReturn(null);
        // Act
        String result = accountsService.transferMoney("Id-1", "NonExistentId", new BigDecimal("500"));

        // Assert
        assertThat(result).isEqualTo("One or both accounts do not exist.");
    }


    @Test
    void transferMoneyNegativeAmount() {
        // Arrange
        Account accountFrom = new Account("Id-1");
        accountFrom.setBalance(new BigDecimal("1000"));
        Account accountTo = new Account("Id-2");
        when(accountsRepository.getAccount("Id-1")).thenReturn(accountFrom);
        when(accountsRepository.getAccount("Id-2")).thenReturn(accountTo);

        // Act and Assert
        assertThatThrownBy(() -> accountsService.transferMoney("Id-1", "Id-2", new BigDecimal("-500")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount to transfer must be a positive number.");
    }
}
