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
public class CreditLimitDTO {
    private BigDecimal creditLimit;
    private BigDecimal usedCreditLimit;
    private BigDecimal availableCreditLimit;
}
