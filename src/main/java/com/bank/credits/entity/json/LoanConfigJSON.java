package com.bank.credits.entity.json;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class LoanConfigJSON implements Serializable {
    private int maxFutureMonths;
    private BigDecimal dailyRate;
    private List<Integer> validInstallmentCounts;
    private BigDecimal minInterestRate;
    private BigDecimal maxInterestRate;
}