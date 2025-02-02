package com.bank.credits.dto.request;

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
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer numberOfInstallments;
}