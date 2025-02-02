package com.bank.credits.entity;

import com.bank.credits.entity.json.LoanConfigJSON;
import com.bank.credits.entity.json.converter.LoanConfigJSONConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnTransformer;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue(value = BusinessConfig.LOAN_CONFIG)
public class LoanConfig extends BusinessConfig {
    @ColumnTransformer(write = "?::jsonb")
    @Column(name = "json", columnDefinition = "jsonb")
    @Convert(converter = LoanConfigJSONConverter.class)
    private LoanConfigJSON json;
}
