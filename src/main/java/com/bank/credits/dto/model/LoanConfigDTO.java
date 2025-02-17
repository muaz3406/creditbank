package com.bank.credits.dto.model;

import com.bank.credits.entity.json.LoanConfigJSON;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoanConfigDTO {
    private Long id;
    private LoanConfigJSON json;
}