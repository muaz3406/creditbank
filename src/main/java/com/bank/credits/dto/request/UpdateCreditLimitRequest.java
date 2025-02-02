package com.bank.credits.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class UpdateCreditLimitRequest {
    private BigDecimal newLimit;
}
