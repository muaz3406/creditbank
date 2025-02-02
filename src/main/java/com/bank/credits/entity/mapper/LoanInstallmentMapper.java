package com.bank.credits.entity.mapper;

import com.bank.credits.dto.model.LoanInstallmentDTO;
import com.bank.credits.entity.LoanInstallment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanInstallmentMapper extends BaseMapper<LoanInstallment, LoanInstallmentDTO> {
}