package com.bank.credits.dto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LoanDTO extends BaseEntityDTO {
    private String customerUsername;
    private BigDecimal loanAmount;
    private Integer numberOfInstallment;
    private BigDecimal interestRate;
    private boolean isPaid;
}