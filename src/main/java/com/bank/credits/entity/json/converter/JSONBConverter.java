package com.bank.credits.entity.json.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class JSONBConverter<T> implements AttributeConverter<T, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Class<T> clazz;

    public JSONBConverter(Class<T> clazz) {
        this.clazz = clazz;
    }


    @Override
    public String convertToDatabaseColumn(T t) {
        if (t == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(t);
        } catch (JsonProcessingException e) {
            log.error("[convertToDatabaseColumn()] error occurred when try to convert {} (class: {}) as database column!",
                    t, t.getClass().getName());
            throw new IllegalArgumentException("JSON conversion failed: " + e.getMessage());
        }
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, clazz);
        } catch (JsonProcessingException e) {
            log.error("[convertToEntityAttribute()] error occurred when try to convert {} (class: {}) as database column!",
                    dbData, clazz.getName());
            throw new IllegalArgumentException("JSON conversion failed: " + e.getMessage());
        }
    }
}
