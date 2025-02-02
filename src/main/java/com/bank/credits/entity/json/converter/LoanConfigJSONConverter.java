package com.bank.credits.entity.json.converter;

import com.bank.credits.entity.json.LoanConfigJSON;
import jakarta.persistence.Converter;

@Converter
public class LoanConfigJSONConverter extends JSONBConverter<LoanConfigJSON> {
    public LoanConfigJSONConverter() {
        super(LoanConfigJSON.class);
    }
}
