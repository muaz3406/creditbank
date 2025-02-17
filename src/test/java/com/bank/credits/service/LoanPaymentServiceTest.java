package com.bank.credits.service;

import com.bank.credits.dto.request.PayLoanRequest;
import com.bank.credits.dto.response.PayLoanResponse;
import com.bank.credits.entity.Customer;
import com.bank.credits.entity.Loan;
import com.bank.credits.entity.LoanConfig;
import com.bank.credits.entity.LoanInstallment;
import com.bank.credits.entity.json.LoanConfigJSON;
import com.bank.credits.enums.LoanStatus;
import com.bank.credits.repository.CustomerRepository;
import com.bank.credits.repository.LoanConfigRepository;
import com.bank.credits.repository.LoanInstallmentRepository;
import com.bank.credits.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.bank.credits.service.LoanPaymentService.INSUFFICIENT_PAYMENT_AMOUNT_OR_NO_INSTALLMENTS_TO_PAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanPaymentServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanConfigRepository loanConfigRepository;

    @InjectMocks
    private LoanPaymentService loanPaymentService;

    @Test
    void whenValidRequest_thenProcessPaymentSuccessfully() {
        PayLoanRequest request = new PayLoanRequest();
        request.setLoanId(1L);
        request.setAmount(new BigDecimal("1000"));

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUsedCreditLimit(new BigDecimal("5000"));

        LoanConfigJSON configJson = new LoanConfigJSON();
        configJson.setDailyRate(new BigDecimal("0.001"));
        configJson.setMaxFutureMonths(12);

        LoanConfig loanConfig = new LoanConfig();
        loanConfig.setId(1L);
        loanConfig.setDefaultConfig(true);
        loanConfig.setJson(configJson);

        Loan loan = new Loan();
        loan.setId(1L);
        loan.setCustomer(customer);
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setLoanAmount(new BigDecimal("1000"));

        LoanInstallment installment = new LoanInstallment();
        installment.setId(1L);
        installment.setAmount(new BigDecimal("1000"));
        installment.setDueDate(LocalDate.now().plusMonths(1));
        installment.setPaid(false);
        installment.setPaidAmount(BigDecimal.ZERO);

        List<LoanInstallment> installments = new ArrayList<>();
        installments.add(installment);

        when(loanConfigRepository.findFirstByDefaultConfigIsTrue())
                .thenReturn(Optional.of(loanConfig));
        when(loanRepository.findByIdAndStatus(1L, LoanStatus.ACTIVE))
                .thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanAndIsPaidFalse(loan))
                .thenReturn(installments);

        PayLoanResponse response = loanPaymentService.processPayment(request, "testUser", true);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getPaidInstallments());
        assertEquals(LoanPaymentService.PAYMENT_PROCESSED_SUCCESSFULLY, response.getMessage());
    }

    @Test
    void whenInvalidPaymentAmount_thenReturnUnsuccessfulResponse() {
        PayLoanRequest request = new PayLoanRequest();
        request.setLoanId(1L);
        request.setAmount(new BigDecimal("100"));

        LoanConfigJSON configJson = new LoanConfigJSON();
        configJson.setDailyRate(new BigDecimal("0.001"));
        configJson.setMaxFutureMonths(12);

        LoanConfig loanConfig = new LoanConfig();
        loanConfig.setJson(configJson);

        Loan loan = createTestLoan(new BigDecimal("1000"));

        when(loanConfigRepository.findFirstByDefaultConfigIsTrue())
                .thenReturn(Optional.of(loanConfig));
        when(loanRepository.findByIdAndStatus(1L, LoanStatus.ACTIVE))
                .thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanAndIsPaidFalse(loan))
                .thenReturn(loan.getInstallments());

        PayLoanResponse response = loanPaymentService.processPayment(request, "testUser", true);

        assertFalse(response.isSuccess());
        assertEquals(INSUFFICIENT_PAYMENT_AMOUNT_OR_NO_INSTALLMENTS_TO_PAY, response.getMessage());
    }

    private Loan createTestLoan(BigDecimal amount) {
        LoanInstallment installment = new LoanInstallment();
        installment.setAmount(amount);
        installment.setDueDate(LocalDate.now().plusMonths(1));
        installment.setPaid(false);
        installment.setPaidAmount(BigDecimal.ZERO);

        List<LoanInstallment> installments = new ArrayList<>();
        installments.add(installment);

        Loan loan = new Loan();
        loan.setId(1L);
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setLoanAmount(amount);
        loan.setInstallments(installments);
        return loan;
    }

    private LoanConfig createTestLoanConfig() {
        LoanConfigJSON configJson = new LoanConfigJSON();
        configJson.setDailyRate(new BigDecimal("0.001"));
        configJson.setMaxFutureMonths(12);

        LoanConfig loanConfig = new LoanConfig();
        loanConfig.setDefaultConfig(true);
        loanConfig.setJson(configJson);
        return loanConfig;
    }
}