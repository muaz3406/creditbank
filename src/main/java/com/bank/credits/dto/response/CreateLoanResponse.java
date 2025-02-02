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
public class CreateLoanResponse {
    private boolean success;
    private Long loanId;
    private BigDecimal totalAmount;
    private Integer numberOfInstallments;
    private BigDecimal installmentAmount;
    private String message;
}
