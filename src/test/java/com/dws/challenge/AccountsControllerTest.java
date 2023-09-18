package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private AccountsRepository accountsRepository;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @BeforeEach
  void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsRepository.clearAccounts();
  }

  @Test
  void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }

  @Test
  void transferMoneyValidRequest() throws Exception {
    // Create two accounts
    Account account1 = new Account("Id-1", new BigDecimal("1000"));
    Account account2 = new Account("Id-2", new BigDecimal("500"));
    accountsService.createAccount(account1);
    accountsService.createAccount(account2);

    // Transfer money from account1 to account2
    this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"accountFromId\":\"Id-1\",\"accountToId\":\"Id-2\",\"amount\":500}"))
            .andExpect(status().isOk());

    // Verify account balances after transfer
    Account updatedAccount1 = accountsService.getAccount("Id-1");
    Account updatedAccount2 = accountsService.getAccount("Id-2");

    assertThat(updatedAccount1.getBalance()).isEqualByComparingTo("500");
    assertThat(updatedAccount2.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  void transferMoneyInsufficientBalance() throws Exception {
    // Create two accounts with insufficient balance
    Account account1 = new Account("Id-1", new BigDecimal("100"));
    Account account2 = new Account("Id-2", new BigDecimal("500"));
    accountsService.createAccount(account1);
    accountsService.createAccount(account2);

    // Try to transfer money from account1 to account2 (insufficient balance)
    this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"accountFromId\":\"Id-1\",\"accountToId\":\"Id-2\",\"amount\":200}"))
            .andExpect(status().isBadRequest());

    // Verify that the balances remain unchanged
    Account updatedAccount1 = accountsService.getAccount("Id-1");
    Account updatedAccount2 = accountsService.getAccount("Id-2");

    assertThat(updatedAccount1.getBalance()).isEqualByComparingTo("100");
    assertThat(updatedAccount2.getBalance()).isEqualByComparingTo("500");
  }

  @Test
  void transferMoneyInvalidRequest() throws Exception {
    // Try to transfer money with an invalid request (missing fields)
    this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"accountFromId\":\"Id-1\",\"amount\":200}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  void transferMoneyAccountNotFound() throws Exception {
    // Try to transfer money from an account that does not exist
    this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"accountFromId\":\"NonExistent\",\"accountToId\":\"Id-2\",\"amount\":200}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  void transferMoneyNegativeAmount() throws Exception {
    // Try to transfer a negative amount of money
    this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"accountFromId\":\"Id-1\",\"accountToId\":\"Id-2\",\"amount\":-200}"))
            .andExpect(status().isBadRequest());
  }

}
