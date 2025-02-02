package com.bank.credits.entity.mapper;

import com.bank.credits.dto.model.LoanDTO;
import com.bank.credits.entity.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanMapper extends BaseMapper<Loan, LoanDTO> {
    @Override
    @Mapping(target = "customerUsername", source = "customer.username")
    LoanDTO toDTO(Loan loan);
}