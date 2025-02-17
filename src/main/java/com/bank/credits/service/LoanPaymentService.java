package com.bank.credits.service;

import com.bank.credits.dto.request.PayLoanRequest;
import com.bank.credits.dto.response.PayLoanResponse;
import com.bank.credits.entity.Customer;
import com.bank.credits.entity.Loan;
import com.bank.credits.entity.LoanConfig;
import com.bank.credits.entity.LoanInstallment;
import com.bank.credits.enums.LoanStatus;
import com.bank.credits.exceptions.ApiErrorCode;
import com.bank.credits.exceptions.ApiException;
import com.bank.credits.repository.CustomerRepository;
import com.bank.credits.repository.LoanConfigRepository;
import com.bank.credits.repository.LoanInstallmentRepository;
import com.bank.credits.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanPaymentService {
    public static final String PAYMENT_PROCESSED_SUCCESSFULLY = "Payment processed successfully";
    public static final String INSUFFICIENT_PAYMENT_AMOUNT_OR_NO_INSTALLMENTS_TO_PAY = "Insufficient payment amount or no installments to pay";
    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;
    private final CustomerRepository customerRepository;
    private final LoanConfigRepository loanConfigRepository;

    @Transactional
    public PayLoanResponse processPayment(PayLoanRequest request, String username, boolean isAdmin) {
        log.info("Starting loan payment process: {}", request);
        LoanConfig loanConfig = loanConfigRepository.findFirstByDefaultConfigIsTrue()
                .orElseThrow(() -> new ApiException(ApiErrorCode.CONFIG_NOT_FOUND.getCode()));
        BigDecimal dailyRate = loanConfig.getJson().getDailyRate();
        int maxFutureMonths = loanConfig.getJson().getMaxFutureMonths();

        try {
            validateRequest(request);

            Loan loan = validateAndGetLoan(request.getLoanId(), username, isAdmin);
            BigDecimal remainingPaymentAmount = request.getAmount();
            int paidInstallmentsCount = 0;
            BigDecimal totalAmountSpent = BigDecimal.ZERO;

            LocalDate maxAllowedDate = LocalDate.now().plusMonths(maxFutureMonths);

            List<LoanInstallment> payableInstallments = loanInstallmentRepository.findByLoanAndIsPaidFalse(loan).stream()
                    .filter(i -> !i.getDueDate().isAfter(maxAllowedDate))
                    .sorted(Comparator.comparing(LoanInstallment::getDueDate))
                    .toList();

            BigDecimal totalRequiredAmount = BigDecimal.ZERO;
            for (LoanInstallment installment : payableInstallments) {
                BigDecimal adjustedAmount = calculateAdjustedAmount(installment, dailyRate);
                totalRequiredAmount = totalRequiredAmount.add(adjustedAmount);

                if (remainingPaymentAmount.compareTo(totalRequiredAmount) < 0) {
                    totalRequiredAmount = totalRequiredAmount.subtract(adjustedAmount);
                    break;
                }
            }

            if (totalRequiredAmount.compareTo(BigDecimal.ZERO) == 0 ||
                    remainingPaymentAmount.compareTo(totalRequiredAmount) < 0) {
                return PayLoanResponse.builder()
                        .success(false)
                        .message(INSUFFICIENT_PAYMENT_AMOUNT_OR_NO_INSTALLMENTS_TO_PAY)
                        .remainingAmount(calculateRemainingDebt(loan))
                        .build();
            }

            for (LoanInstallment installment : payableInstallments) {
                BigDecimal adjustedAmount = calculateAdjustedAmount(installment, dailyRate);

                if (remainingPaymentAmount.compareTo(adjustedAmount) < 0) {
                    break;
                }

                processInstallmentPayment(installment, adjustedAmount);
                remainingPaymentAmount = remainingPaymentAmount.subtract(adjustedAmount);
                paidInstallmentsCount++;
                totalAmountSpent = totalAmountSpent.add(adjustedAmount);
            }

            boolean isFullyPaid = updateLoanStatus(loan);

            return buildSuccessResponse(loan, isFullyPaid, paidInstallmentsCount,
                    totalAmountSpent, remainingPaymentAmount);

        } catch (Exception e) {
            log.error("Error during payment process: ", e);
            throw new ApiException(ApiErrorCode.PAYMENT_PROCESSING_ERROR.getCode());
        }
    }

    private void validateRequest(PayLoanRequest request) {
        log.debug("Validating payment request");
        if (request == null) {
            throw new ApiException(ApiErrorCode.INVALID_REQUEST.getCode());
        }
        if (request.getLoanId() == null) {
            throw new ApiException(ApiErrorCode.INVALID_LOAN_ID.getCode());
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(ApiErrorCode.INVALID_PAYMENT_AMOUNT.getCode());
        }
    }

    private Loan validateAndGetLoan(Long loanId, String username, boolean isAdmin) {
        log.debug("Fetching and validating loan with ID: {}", loanId);
        if (isAdmin) {
            return loanRepository.findByIdAndStatus(loanId, LoanStatus.ACTIVE)
                    .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND_LOAN.getCode()));
        }
        return loanRepository.findByIdAndStatusAndCustomerUsername(loanId, LoanStatus.ACTIVE, username)
                .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND_LOAN.getCode()));
    }

    private BigDecimal calculateAdjustedAmount(LoanInstallment installment, BigDecimal dailyRate) {
        LocalDateTime now = LocalDateTime.now();
        long daysDifference = ChronoUnit.DAYS.between(now.toLocalDate(), installment.getDueDate());

        BigDecimal originalAmount = installment.getAmount();
        BigDecimal adjustedAmount;

        if (daysDifference > 0) {
            BigDecimal discount = originalAmount
                    .multiply(dailyRate)
                    .multiply(BigDecimal.valueOf(daysDifference));
            adjustedAmount = originalAmount.subtract(discount);
        } else if (daysDifference < 0) {
            BigDecimal penalty = originalAmount
                    .multiply(dailyRate)
                    .multiply(BigDecimal.valueOf(Math.abs(daysDifference)));
            adjustedAmount = originalAmount.add(penalty);
        } else {
            adjustedAmount = originalAmount;
        }

        log.debug("Calculated adjusted amount. Original: {}, Adjusted: {}, Days difference: {}",
                originalAmount, adjustedAmount, daysDifference);

        return adjustedAmount;
    }

    private void processInstallmentPayment(LoanInstallment installment, BigDecimal adjustedAmount) {
        log.debug("Processing installment payment. Amount: {}, Due date: {}",
                adjustedAmount, installment.getDueDate());
        installment.setPaidAmount(installment.getPaidAmount().add(adjustedAmount));
        installment.setPaid(true);
        installment.setPaymentDate(LocalDateTime.now());
        loanInstallmentRepository.save(installment);
    }

    private boolean updateLoanStatus(Loan loan) {
        log.debug("Checking if loan is fully paid: {}", loan.getId());
        boolean allPaid = loanInstallmentRepository.findByLoanAndIsPaidFalse(loan).isEmpty();

        if (allPaid) {
            log.info("Loan fully paid, updating status to CLOSED: {}", loan.getId());
            loan.setStatus(LoanStatus.CLOSED);
            loan.setClosedDate(LocalDateTime.now());
            loanRepository.save(loan);

            updateCustomerCreditLimit(loan);
        }

        return allPaid;
    }

    private void updateCustomerCreditLimit(Loan loan) {
        log.debug("Updating customer credit limit for loan: {}", loan.getId());
        Customer customer = loan.getCustomer();
        BigDecimal newUsedLimit = customer.getUsedCreditLimit()
                .subtract(loan.getLoanAmount());

        customer.setUsedCreditLimit(newUsedLimit);
        customerRepository.save(customer);
        log.debug("Updated customer credit limit. New used limit: {}", newUsedLimit);
    }

    private BigDecimal calculateRemainingDebt(Loan loan) {
        log.debug("Calculating remaining debt for loan: {}", loan.getId());
        BigDecimal remainingDebt = loanInstallmentRepository.findByLoanAndIsPaidFalse(loan).stream()
                .map(i -> i.getAmount().subtract(i.getPaidAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("Calculated remaining debt: {}", remainingDebt);
        return remainingDebt;
    }

    private PayLoanResponse buildSuccessResponse(Loan loan, boolean isFullyPaid,
                                                 int paidInstallments, BigDecimal totalAmountSpent,
                                                 BigDecimal remainingPaymentAmount) {
        return PayLoanResponse.builder()
                .success(true)
                .paidInstallments(paidInstallments)
                .totalAmountSpent(totalAmountSpent)
                .isLoanPaidCompletely(isFullyPaid)
                .remainingAmount(calculateRemainingDebt(loan))
                .unusedAmount(remainingPaymentAmount)
                .message(PAYMENT_PROCESSED_SUCCESSFULLY)
                .build();
    }
}