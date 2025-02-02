package com.bank.credits.repository;

import com.bank.credits.entity.LoanConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanConfigRepository extends JpaRepository<LoanConfig, Long> {
    Optional<LoanConfig> findFirstByDefaultConfigIsTrue();
}
