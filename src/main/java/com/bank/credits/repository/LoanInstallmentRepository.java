package com.bank.credits.repository;

import com.bank.credits.entity.Loan;
import com.bank.credits.entity.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long> {
    List<LoanInstallment> findByLoan(Loan loan);

    List<LoanInstallment> findByLoanAndIsPaidFalse(Loan loan);
}
