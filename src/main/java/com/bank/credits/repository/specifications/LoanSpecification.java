package com.bank.credits.repository.specifications;

import com.bank.credits.dto.request.LoanFilterRequest;
import com.bank.credits.entity.Loan;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LoanSpecification {

    public static Specification<Loan> withDynamicQuery(LoanFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getCustomerUsername() != null) {
                predicates.add(cb.equal(root.get("customer").get("username"),
                        filter.getCustomerUsername()));
            }

            if (filter.getNumberOfInstallment() != null) {
                predicates.add(cb.equal(root.get("numberOfInstallment"),
                        filter.getNumberOfInstallment()));
            }

            if (filter.getIsPaid() != null) {
                predicates.add(cb.equal(root.get("isPaid"),
                        filter.getIsPaid()));
            }

            if (filter.getMinAmount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("loanAmount"),
                        filter.getMinAmount()));
            }
            if (filter.getMaxAmount() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("loanAmount"),
                        filter.getMaxAmount()));
            }

            if (filter.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createDate"),
                        filter.getStartDate()));
            }
            if (filter.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createDate"),
                        filter.getEndDate()));
            }

            if (filter.getMinInterestRate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("interestRate"),
                        filter.getMinInterestRate()));
            }
            if (filter.getMaxInterestRate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("interestRate"),
                        filter.getMaxInterestRate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

