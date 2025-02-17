package com.bank.credits.service;

import com.bank.credits.dto.model.LoanConfigDTO;
import com.bank.credits.entity.mapper.LoanConfigMapper;
import com.bank.credits.exceptions.ApiErrorCode;
import com.bank.credits.exceptions.ApiException;
import com.bank.credits.repository.LoanConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanConfigService {
    private LoanConfigMapper mapper;
    private final LoanConfigRepository repository;

    @Cacheable(value = "loanConfigCache", key = "'defaultConfig'")
    public LoanConfigDTO getDefaultLoanConfig() {
        return mapper.toDTO(repository.findFirstByDefaultConfigIsTrue()
                .orElseThrow(() -> new ApiException(ApiErrorCode.CONFIG_NOT_FOUND.getCode())));
    }
}