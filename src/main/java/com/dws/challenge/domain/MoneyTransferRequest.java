package com.dws.challenge.domain;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class MoneyTransferRequest {
    @NotEmpty
    private String accountFromId;

    @NotEmpty
    private String accountToId;

    @Positive
    private BigDecimal amount;
}

