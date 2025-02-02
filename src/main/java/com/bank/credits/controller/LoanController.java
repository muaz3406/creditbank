package com.bank.credits.controller;

import com.bank.credits.dto.model.LoanDTO;
import com.bank.credits.dto.model.LoanInstallmentDTO;
import com.bank.credits.dto.request.PayLoanRequest;
import com.bank.credits.dto.response.PayLoanResponse;
import com.bank.credits.enums.UserRole;
import com.bank.credits.service.CustomerService;
import com.bank.credits.service.LoanPaymentService;
import com.bank.credits.service.LoanService;
import com.bank.credits.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/loans")
@PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
@Tag(name = "Loan Operations", description = "API endpoints for loan operations")
public class LoanController {
    private final LoanService loanService;
    private final CustomerService customerService;
    private final LoanPaymentService paymentService;

    @Operation(
            summary = "Get loan details",
            description = """
                    Returns loan details for the given loan ID.
                    """
    )
    @GetMapping("/{id}")
    public ResponseEntity<LoanDTO> getLoan(@PathVariable Long id) {
        log.info("Fetching loan details - Loan ID: {}", id);
        if (SecurityUtil.hasRole(UserRole.ROLE_ADMIN.name())) {
            return ResponseEntity.ok(loanService.getLoanById(id));

        }
        return ResponseEntity.ok(loanService.getLoanByIdAndCustomer(id, SecurityUtil.getUsername()));
    }

    @Operation(
            summary = "Get loan installments",
            description = """
                    Returns installment details for the given loan ID.
                    """
    )
    @GetMapping("/installments/{loanId}")
    public ResponseEntity<List<LoanInstallmentDTO>> getInstallments(@PathVariable Long loanId) {
        log.info("Fetching loan installments - Loan ID: {}", loanId);
        if (SecurityUtil.hasRole(UserRole.ROLE_ADMIN.name())) {
            return ResponseEntity.ok(loanService.getLoanInstallmentsByLoanId(loanId));

        }
        return ResponseEntity.ok(loanService.getLoanInstallmentsByLoanIdAndCustomer(loanId, SecurityUtil.getUsername()));
    }

    @Operation(
            summary = "Process loan payment",
            description = """
                    Processes payment for loan installments.
                    """
    )
    @PostMapping("/pay")
    public ResponseEntity<PayLoanResponse> payLoan(@Valid @RequestBody PayLoanRequest request) {
        log.info("Processing loan payment - Amount: {}, Loan ID: {}",
                request.getAmount(), request.getLoanId());
        if (SecurityUtil.hasRole(UserRole.ROLE_ADMIN.name())) {
            return ResponseEntity.ok(paymentService.processPayment(request, null, true));

        }
        return ResponseEntity.ok(paymentService.processPayment(request, SecurityUtil.getUsername(), false));
    }
}