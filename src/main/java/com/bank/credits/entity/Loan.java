package com.bank.credits.entity;

import com.bank.credits.entity.json.LoanInstallmentJSON;
import com.bank.credits.entity.json.converter.LoanInstallmentsJSONConverter;
import com.bank.credits.enums.LoanStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnTransformer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "loans")
@ToString(callSuper = true, exclude = {"customer"})
@EqualsAndHashCode(callSuper = true)
public class Loan extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private BigDecimal loanAmount;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private Integer numberOfInstallment;

    @Column(nullable = false)
    private BigDecimal interestRate;

    @Column(nullable = false)
    private boolean isPaid;

    private LoanStatus status;

    private LocalDateTime closedDate;

    @ColumnTransformer(write = "?::jsonb")
    @Column(name = "installments", columnDefinition = "jsonb")
    @Convert(converter = LoanInstallmentsJSONConverter.class)
    private List<LoanInstallmentJSON> installments;
}