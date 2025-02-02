package com.bank.credits.controller;

import com.bank.credits.dto.model.LoanDTO;
import com.bank.credits.dto.request.CreateLoanRequest;
import com.bank.credits.dto.request.LoanFilterRequest;
import com.bank.credits.dto.response.CreateLoanResponse;
import com.bank.credits.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/loans")
@Tag(name = "Admin Loan Operations", description = "API endpoints for administrative loan operations")
public class AdminLoanController {
    private final LoanService loanService;

    @Operation(
            summary = "Search loans",
            description = """
                    Search and filter loans with pagination support.
                    Only accessible by admin users.
                    """
    )
    @PostMapping("/search")
    public ResponseEntity<Page<LoanDTO>> search(
            @RequestBody LoanFilterRequest request, @PageableDefault Pageable pageable) {
        log.info("Searching loans");
        return ResponseEntity.ok(loanService.getCustomerLoans(request, pageable));
    }


    @Operation(
            summary = "Create new loan",
            description = """
                    Create a new loan for a customer.
                    Only accessible by admin users.
                    """
    )
    @PostMapping("/create")
    public ResponseEntity<CreateLoanResponse> createLoan(@Valid @RequestBody CreateLoanRequest request) {
        log.info("Creating new loan for customer: {}", request.getCustomerUsername());
        return ResponseEntity.ok(loanService.createLoan(request));
    }
}
