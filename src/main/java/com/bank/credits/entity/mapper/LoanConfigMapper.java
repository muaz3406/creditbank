package com.bank.credits.entity.mapper;

import com.bank.credits.dto.model.LoanConfigDTO;
import com.bank.credits.entity.LoanConfig;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanConfigMapper extends BaseMapper<LoanConfig, LoanConfigDTO> {
}
