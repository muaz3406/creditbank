package com.bank.credits.service;

import com.bank.credits.dto.model.LoanConfigDTO;
import com.bank.credits.dto.model.LoanDTO;
import com.bank.credits.dto.request.CreateLoanRequest;
import com.bank.credits.dto.request.LoanFilterRequest;
import com.bank.credits.entity.Customer;
import com.bank.credits.entity.Loan;
import com.bank.credits.entity.json.LoanConfigJSON;
import com.bank.credits.entity.mapper.LoanMapper;
import com.bank.credits.exceptions.ApiErrorCode;
import com.bank.credits.exceptions.ApiException;
import com.bank.credits.repository.CustomerRepository;
import com.bank.credits.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanConfigService loanConfigService;

    @Mock
    private LoanMapper loanMapper;

    @InjectMocks
    private LoanService loanService;

    @Test
    void whenInsufficientCreditLimit_thenThrowException() {
        CreateLoanRequest request = new CreateLoanRequest();
        request.setAmount(new BigDecimal("60000"));
        request.setNumberOfInstallments(12);
        request.setInterestRate(new BigDecimal("0.10"));
        request.setCustomerUsername("testUser");

        Customer customer = new Customer();
        customer.setUsername("testUser");
        customer.setCreditLimit(new BigDecimal("50000"));
        customer.setUsedCreditLimit(new BigDecimal("0"));

        LoanConfigJSON configJson = new LoanConfigJSON();
        configJson.setValidInstallmentCounts(Arrays.asList(12, 24, 36));
        configJson.setMaxInterestRate(new BigDecimal("0.20"));
        configJson.setMinInterestRate(new BigDecimal("0.05"));

        LoanConfigDTO loanConfig = new LoanConfigDTO();
        loanConfig.setJson(configJson);

        when(loanConfigService.getDefaultLoanConfig())
                .thenReturn(loanConfig);
        when(customerRepository.findByUsername("testUser"))
                .thenReturn(Optional.of(customer));

        ApiException exception = assertThrows(ApiException.class, () -> loanService.createLoan(request));
        assertEquals(ApiErrorCode.INSUFFICIENT_CREDIT_LIMIT.getCode(), exception.getErrorCode());
    }


    @Test
    void whenInvalidInstallmentCount_thenThrowException() {
        CreateLoanRequest request = new CreateLoanRequest();
        request.setAmount(new BigDecimal("10000"));
        request.setNumberOfInstallments(15); // Invalid installment count
        request.setInterestRate(new BigDecimal("0.10"));
        request.setCustomerUsername("testUser");

        LoanConfigJSON configJson = new LoanConfigJSON();
        configJson.setValidInstallmentCounts(Arrays.asList(12, 24, 36));
        configJson.setMaxInterestRate(new BigDecimal("0.20"));
        configJson.setMinInterestRate(new BigDecimal("0.05"));

        LoanConfigDTO loanConfig = new LoanConfigDTO();
        loanConfig.setJson(configJson);

        when(loanConfigService.getDefaultLoanConfig())
                .thenReturn(loanConfig);

        ApiException exception = assertThrows(ApiException.class, () -> loanService.createLoan(request));
        assertEquals(ApiErrorCode.INVALID_INSTALLMENT_COUNT.getCode(), exception.getErrorCode());
    }

    @Test
    void whenGetCustomerLoans_thenReturnPagedResults() {
        LoanFilterRequest filterRequest = new LoanFilterRequest();
        PageRequest pageable = PageRequest.of(0, 10);
        List<Loan> loans = Arrays.asList(new Loan(), new Loan());
        Page<Loan> loanPage = new PageImpl<>(loans);
        List<LoanDTO> loanDTOs = Arrays.asList(new LoanDTO(), new LoanDTO());
        Page<LoanDTO> expectedPage = new PageImpl<>(loanDTOs);

        when(loanRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(loanPage);
        when(loanMapper.toDTOs(loanPage)).thenReturn(expectedPage);

        Page<LoanDTO> result = loanService.getCustomerLoans(filterRequest, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(loanRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void whenGetLoanByIdAndCustomer_thenReturnLoan() {
        // Arrange
        Long loanId = 1L;
        String username = "testUser";
        Loan loan = new Loan();
        LoanDTO expectedDto = new LoanDTO();

        when(loanRepository.findByIdAndCustomerUsername(loanId, username))
                .thenReturn(Optional.of(loan));
        when(loanMapper.toDTO(loan)).thenReturn(expectedDto);

        LoanDTO result = loanService.getLoanByIdAndCustomer(loanId, username);

        assertNotNull(result);
        verify(loanRepository).findByIdAndCustomerUsername(loanId, username);
    }

    @Test
    void whenGetLoanByIdAndCustomerNotFound_thenThrowException() {
        Long loanId = 1L;
        String username = "testUser";

        when(loanRepository.findByIdAndCustomerUsername(loanId, username))
                .thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> loanService.getLoanByIdAndCustomer(loanId, username));
        assertEquals(ApiErrorCode.ENTITY_NOT_FOUND.getCode(), exception.getErrorCode());
    }
}