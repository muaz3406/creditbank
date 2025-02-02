package com.bank.credits.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanFilterRequest {
    private String customerUsername;
    private Integer numberOfInstallment;
    private Boolean isPaid;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minInterestRate;
    private BigDecimal maxInterestRate;
}