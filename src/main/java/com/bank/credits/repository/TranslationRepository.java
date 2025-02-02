package com.bank.credits.repository;

import com.bank.credits.entity.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TranslationRepository extends JpaRepository<Translation, Long> {
    Optional<Translation> findByErrorCodeAndLanguageCode(String errorCode, String languageCode);
}
