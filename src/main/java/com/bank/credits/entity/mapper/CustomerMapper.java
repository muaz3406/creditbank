package com.bank.credits.entity.mapper;

import com.bank.credits.dto.model.CustomerDTO;
import com.bank.credits.entity.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper extends BaseMapper<Customer, CustomerDTO> {
}
