package com.bank.credits.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateCustomerRequest {
    private String name;
    private String surname;
}
