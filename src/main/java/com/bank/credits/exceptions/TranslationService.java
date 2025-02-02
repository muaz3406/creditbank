package com.bank.credits.exceptions;

import com.bank.credits.entity.Translation;
import com.bank.credits.repository.TranslationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationService {
    private static final String DEFAULT_LANGUAGE = "en-UK";
    private static final String CACHE_KEY_PREFIX = "translation:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private final TranslationRepository translationRepository;
    private final RedisTemplate<String, Translation> redisTemplate;

    @PostConstruct
    public void init() {
        loadTranslationsToCache();
    }

    private void loadTranslationsToCache() {
        log.info("Loading translations to cache...");
        List<Translation> allTranslations = translationRepository.findAll();
        allTranslations.forEach(translation -> {
            String cacheKey = getCacheKey(translation.getErrorCode(), translation.getLanguageCode());
            redisTemplate.opsForValue().set(cacheKey, translation, CACHE_TTL);
        });
        log.info("Loaded {} translations to cache", allTranslations.size());
    }

    private String getCacheKey(String errorCode, String language) {
        return CACHE_KEY_PREFIX + errorCode + ":" + language;
    }

    public String getMessage(String errorCode, String language) {
        Translation translation = getTranslation(errorCode, language);
        if (translation == null) {
            translation = getTranslation(errorCode, DEFAULT_LANGUAGE);
        }
        return translation != null ? translation.getDescription() : "Unknown error";
    }

    public String getMessage(String errorCode, String language, Object... params) {
        String messageTemplate = getMessage(errorCode, language);
        return String.format(messageTemplate, params);
    }

    @Cacheable(value = "translations", key = "#errorCode + ':' + #language")
    public Translation getTranslation(String errorCode, String language) {
        String cacheKey = getCacheKey(errorCode, language);
        Translation cachedTranslation = redisTemplate.opsForValue().get(cacheKey);

        if (cachedTranslation != null) {
            return cachedTranslation;
        }

        return translationRepository.findByErrorCodeAndLanguageCode(errorCode, language)
                .map(translation -> {
                    redisTemplate.opsForValue().set(cacheKey, translation, CACHE_TTL);
                    return translation;
                })
                .orElse(null);
    }

    @CacheEvict(value = "translations", allEntries = true)
    public void refreshCache() {
        log.info("Refreshing translations cache...");
        loadTranslationsToCache();
    }
}

