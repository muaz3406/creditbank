package com.bank.credits.service;

import com.bank.credits.enums.UserRole;
import com.bank.credits.exceptions.ApiErrorCode;
import com.bank.credits.exceptions.ApiException;
import com.bank.credits.repository.CustomerRepository;
import com.bank.credits.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Service
@RequiredArgsConstructor
public class ValidationService {
    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;
    public void validateUser(Long loanId, String customerUsername) {
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals(UserRole.ROLE_CUSTOMER.name()))) {
            loanRepository.findByIdAndCustomerUsername(loanId, customerUsername)
                    .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND_LOAN.getCode()));
        } else if (!StringUtils.hasText(customerUsername)) {
            throw new ApiException(ApiErrorCode.CUSTOMER_NOT_FOUND.getCode());
        } else if (StringUtils.hasText(customerUsername)) {
            customerRepository.findByUsername(customerUsername)
                    .orElseThrow(() -> new ApiException(ApiErrorCode.CUSTOMER_NOT_FOUND.getCode()));
        }
    }
}