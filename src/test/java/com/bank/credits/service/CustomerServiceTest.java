package com.bank.credits.service;

import com.bank.credits.entity.Customer;
import com.bank.credits.exceptions.ApiErrorCode;
import com.bank.credits.exceptions.ApiException;
import com.bank.credits.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void whenValidUsername_thenReturnCustomer() {
        String username = "testUser";
        Customer expectedCustomer = Customer.builder()
                .name("muaz")
                .surname("tastemel")
                .username(username)
                .build();

        when(customerRepository.findByUsername(username))
                .thenReturn(Optional.of(expectedCustomer));

        Customer actualCustomer = customerService.getByUsername(username);
        assertNotNull(actualCustomer);
        assertEquals(expectedCustomer.getId(), actualCustomer.getId());
        assertEquals(expectedCustomer.getUsername(), actualCustomer.getUsername());
        verify(customerRepository, times(1)).findByUsername(username);
    }

    @Test
    void whenInvalidUsername_thenThrowApiException() {
        String username = "nonExistentUser";
        when(customerRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> customerService.getByUsername(username));
        assertEquals(ApiErrorCode.ENTITY_NOT_FOUND.getCode(), exception.getErrorCode());
        verify(customerRepository, times(1)).findByUsername(username);
    }

    @Test
    void whenNullUsername_thenHandleGracefully() {
        String username = null;
        assertThrows(ApiException.class,
                () -> customerService.getByUsername(username));

        verify(customerRepository, times(1)).findByUsername(null);
    }

    @Test
    void whenRepositoryThrowsException_thenHandleGracefully() {
        String username = "testUser";
        when(customerRepository.findByUsername(username))
                .thenThrow(new RuntimeException("Database error"));
        assertThrows(RuntimeException.class,
                () -> customerService.getByUsername(username));

        verify(customerRepository, times(1)).findByUsername(username);
    }
}