package com.bank.credits.entity.json.converter;

import com.bank.credits.entity.json.LoanInstallmentJSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

@Converter
public class LoanInstallmentsJSONConverter implements AttributeConverter<List<LoanInstallmentJSON>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    @Override
    public String convertToDatabaseColumn(List<LoanInstallmentJSON> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("json convert error", e);
        }
    }

    @Override
    public List<LoanInstallmentJSON> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(dbData,
                    new TypeReference<List<LoanInstallmentJSON>>() {
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON parse error", e);
        }
    }
}

