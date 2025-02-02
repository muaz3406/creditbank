package com.bank.credits.service;

import com.bank.credits.dto.model.LoanDTO;
import com.bank.credits.dto.model.LoanInstallmentDTO;
import com.bank.credits.dto.request.CreateLoanRequest;
import com.bank.credits.dto.request.LoanFilterRequest;
import com.bank.credits.dto.response.CreateLoanResponse;
import com.bank.credits.entity.Customer;
import com.bank.credits.entity.Loan;
import com.bank.credits.entity.LoanConfig;
import com.bank.credits.entity.LoanInstallment;
import com.bank.credits.entity.json.LoanConfigJSON;
import com.bank.credits.entity.mapper.LoanInstallmentMapper;
import com.bank.credits.entity.mapper.LoanMapper;
import com.bank.credits.enums.LoanStatus;
import com.bank.credits.exceptions.ApiErrorCode;
import com.bank.credits.exceptions.ApiException;
import com.bank.credits.repository.CustomerRepository;
import com.bank.credits.repository.LoanConfigRepository;
import com.bank.credits.repository.LoanInstallmentRepository;
import com.bank.credits.repository.LoanRepository;
import com.bank.credits.repository.specifications.LoanSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LoanService {
    private final LoanMapper loanMapper;
    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;
    private final LoanConfigRepository loanConfigRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;
    private final LoanInstallmentMapper loanInstallmentMapper;

    public CreateLoanResponse createLoan(CreateLoanRequest request) {
        log.info("Received loan creation request: {}", request);
        LoanConfig loanConfig = loanConfigRepository.findFirstByDefaultConfigIsTrue()
                .orElseThrow(() -> new ApiException(ApiErrorCode.CONFIG_NOT_FOUND.getCode()));

        try {
            validateRequest(request, loanConfig.getJson());

            Customer customer = getAndValidateCustomer(request.getCustomerUsername());
            validateCustomerLimit(customer, request.getAmount());

            Loan loan = createAndSaveLoan(request, customer);

            updateCustomerLimit(customer, request.getAmount());

            log.info("Successfully created loan with ID: {}", loan.getId());
            return buildSuccessResponse(loan);

        } catch (ApiException e) {
            log.error("Failed to create loan. Error code: {}", e.getErrorCode());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating loan: ", e);
            throw new ApiException(ApiErrorCode.VALIDATION_ERROR.getCode());
        }
    }

    private void validateRequest(CreateLoanRequest request, LoanConfigJSON loanConfig) {
        log.debug("Validating loan request parameters");
        List<Integer> validInstallmentCounts = loanConfig.getValidInstallmentCounts();
        BigDecimal maxInterestRate = loanConfig.getMaxInterestRate();
        BigDecimal minInterestRate = loanConfig.getMinInterestRate();

        if (request == null) {
            log.error("Loan request is null");
            throw new ApiException(ApiErrorCode.INVALID_REQUEST.getCode());
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Invalid loan amount: {}", request.getAmount());
            throw new ApiException(ApiErrorCode.INVALID_LOAN_AMOUNT.getCode());
        }

        if (!validInstallmentCounts.contains(request.getNumberOfInstallments())) {
            log.error("Invalid installment count: {}. Valid values are: {}",
                    request.getNumberOfInstallments(), validInstallmentCounts);
            throw new ApiException(ApiErrorCode.INVALID_INSTALLMENT_COUNT.getCode(),
                    validInstallmentCounts.toString());
        }

        if (request.getInterestRate() == null ||
                request.getInterestRate().compareTo(minInterestRate) < 0 ||
                request.getInterestRate().compareTo(maxInterestRate) > 0) {
            log.error("Invalid interest rate: {}. Must be between {} and {}",
                    request.getInterestRate(), minInterestRate, maxInterestRate);
            throw new ApiException(ApiErrorCode.INVALID_INTEREST_RATE.getCode(),
                    minInterestRate, maxInterestRate);
        }

        log.debug("Loan request validation completed successfully");
    }

    private Customer getAndValidateCustomer(String customerUsername) {
        log.debug("Fetching customer with username: {}", customerUsername);
        return customerRepository.findByUsername(customerUsername)
                .orElseThrow(() -> {
                    log.error("Customer not found with username: {}", customerUsername);
                    return new ApiException(ApiErrorCode.NOT_FOUND_USER.getCode(), customerUsername);
                });
    }

    private void validateCustomerLimit(Customer customer, BigDecimal loanAmount) {
        BigDecimal availableLimit = customer.getCreditLimit().subtract(customer.getUsedCreditLimit());
        log.debug("Checking credit limit for customer {}. Available: {}, Requested: {}",
                customer.getId(), availableLimit, loanAmount);

        if (availableLimit.compareTo(loanAmount) < 0) {
            log.error("Insufficient credit limit for customer {}. Available: {}, Requested: {}",
                    customer.getId(), availableLimit, loanAmount);
            throw new ApiException(ApiErrorCode.INSUFFICIENT_CREDIT_LIMIT.getCode(),
                    availableLimit, loanAmount);
        }
    }

    private Loan createAndSaveLoan(CreateLoanRequest request, Customer customer) {
        log.debug("Creating new loan for customer: {}", customer.getId());
        BigDecimal totalAmount = calculateTotalAmount(request.getAmount(), request.getInterestRate());
        BigDecimal installmentAmount = totalAmount
                .divide(BigDecimal.valueOf(request.getNumberOfInstallments()), 2, RoundingMode.HALF_UP);

        Loan loan = Loan.builder()
                .customer(customer)
                .loanAmount(request.getAmount())
                .totalAmount(totalAmount)
                .numberOfInstallment(request.getNumberOfInstallments())
                .interestRate(request.getInterestRate())
                .status(LoanStatus.ACTIVE)
                .build();

        loan = loanRepository.save(loan);

        createAndSaveInstallments(loan, request.getNumberOfInstallments(), installmentAmount);

        log.debug("Created loan with ID: {}", loan.getId());
        return loan;
    }

    private void createAndSaveInstallments(Loan loan, int numberOfInstallments, BigDecimal installmentAmount) {
        log.debug("Creating installments for loan ID: {}", loan.getId());
        LocalDate firstDueDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        List<LoanInstallment> installments = new ArrayList<>();

        for (int i = 0; i < numberOfInstallments; i++) {
            LoanInstallment installment = LoanInstallment.builder()
                    .loan(loan)
                    .installmentNumber(i + 1)
                    .amount(installmentAmount)
                    .paidAmount(BigDecimal.ZERO)
                    .dueDate(firstDueDate.plusMonths(i))
                    .isPaid(false)
                    .discount(BigDecimal.ZERO)
                    .penalty(BigDecimal.ZERO)
                    .build();

            installments.add(installment);
        }

        loanInstallmentRepository.saveAll(installments);
        log.debug("Saved {} installments for loan ID: {}", installments.size(), loan.getId());
    }

    private void updateCustomerLimit(Customer customer, BigDecimal loanAmount) {
        log.debug("Updating credit limit for customer: {}", customer.getId());
        customer.setUsedCreditLimit(customer.getUsedCreditLimit().add(loanAmount));
        customerRepository.save(customer);
        log.debug("Updated credit limit for customer: {}. New used limit: {}",
                customer.getId(), customer.getUsedCreditLimit());
    }

    private BigDecimal calculateTotalAmount(BigDecimal amount, BigDecimal interestRate) {
        return amount.multiply(BigDecimal.ONE.add(interestRate))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private CreateLoanResponse buildSuccessResponse(Loan loan) {
        BigDecimal installmentAmount = loan.getTotalAmount()
                .divide(BigDecimal.valueOf(loan.getNumberOfInstallment()), 2, RoundingMode.HALF_UP);

        return CreateLoanResponse.builder()
                .success(true)
                .loanId(loan.getId())
                .totalAmount(loan.getTotalAmount())
                .numberOfInstallments(loan.getNumberOfInstallment())
                .installmentAmount(installmentAmount)
                .message("Loan created successfully")
                .build();
    }

    public Page<LoanDTO> getCustomerLoans(LoanFilterRequest request, Pageable pageable) {
        return loanMapper.toDTOs(loanRepository.findAll(
                LoanSpecification.withDynamicQuery(request),
                pageable
        ));
    }

    public LoanDTO getLoanByIdAndCustomer(Long id, String customerUsername) {
        return loanMapper.toDTO(loanRepository.findByIdAndCustomerUsername(id, customerUsername)
                .orElseThrow(() -> new ApiException(ApiErrorCode.ENTITY_NOT_FOUND.getCode())));
    }

    public List<LoanInstallmentDTO> getLoanInstallmentsByIdAndCustomer(Long id, String customerUsername) {
        Loan loan = loanRepository.findByIdAndCustomerUsername(id, customerUsername)
                .orElseThrow(() -> new ApiException(ApiErrorCode.ENTITY_NOT_FOUND.getCode()));

        return loanInstallmentMapper.toDTOs(loanInstallmentRepository.findByLoan(loan));
    }
}