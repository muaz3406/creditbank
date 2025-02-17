package com.bank.credits.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLoanRequest {
    private String customerUsername;
    @NotNull
    private BigDecimal amount;
    @NotNull
    private BigDecimal interestRate;
    @NotNull
    private Integer numberOfInstallments;
}