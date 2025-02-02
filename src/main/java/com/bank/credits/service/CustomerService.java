package com.bank.credits.service;

import com.bank.credits.dto.model.CustomerDTO;
import com.bank.credits.dto.request.CreateCustomerRequest;
import com.bank.credits.entity.Customer;
import com.bank.credits.entity.mapper.CustomerMapper;
import com.bank.credits.exceptions.ApiErrorCode;
import com.bank.credits.exceptions.ApiException;
import com.bank.credits.repository.CustomerRepository;
import com.bank.credits.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public Customer getByUsername(String username) {
        return customerRepository.findByUsername(username).orElseThrow(
                () ->
                        new ApiException(
                                ApiErrorCode.ENTITY_NOT_FOUND.getCode()));
    }

    @Transactional
    public CustomerDTO createCustomer(CreateCustomerRequest request) {
        userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND_USER.getCode()));
        if (customerRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ApiException(ApiErrorCode.USERNAME_EXISTS.getCode());
        }
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setSurname(request.getSurname());
        customer.setUsername(request.getUsername());
        customer.setCreditLimit(request.getCreditLimit());
        customer.setUsedCreditLimit(BigDecimal.ZERO);
        customer.setLoans(new ArrayList<>());
        return customerMapper.toDTO(customerRepository.save(customer));
    }
}