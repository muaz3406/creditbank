package com.bank.credits.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "translations")
public class Translation extends BaseEntity implements Serializable {

    @Column(name = "error_code", nullable = false, unique = true)
    private String errorCode;

    @Column(name = "language_code", nullable = false)
    private String languageCode;

    @Column(name = "description", nullable = false)
    private String description;
}

