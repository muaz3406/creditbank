package com.bank.credits.repository;

import com.bank.credits.entity.Loan;
import com.bank.credits.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {
    Optional<Loan> findByIdAndStatus(Long id, LoanStatus loanStatus);

    Optional<Loan> findByIdAndCustomerUsername(Long id, String customerUsername);
}

