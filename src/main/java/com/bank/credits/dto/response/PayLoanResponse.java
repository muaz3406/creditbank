package com.bank.credits.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayLoanResponse {
    private int paidInstallments;
    private BigDecimal totalAmountSpent;
    private BigDecimal unusedAmount;
    private boolean isLoanPaidCompletely;
    private boolean success;
    private BigDecimal remainingAmount;
    private String message;
}