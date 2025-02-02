package com.bank.credits.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final String TRACE_ID = "TRACE_ID";
    public static final String N_A = "N/A";
    public static final String ACCEPT_LANGUAGE = "Accept-Language";
    public static final String EN_UK = "en-UK";
    private final TranslationService translationService;

    public GlobalExceptionHandler(TranslationService translationService) {
        this.translationService = translationService;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        String language = request.getHeader(ACCEPT_LANGUAGE);
        language = (language == null || language.isEmpty()) ? EN_UK : language;
        String message = translationService.getMessage(ex.getErrorCode(), language, ex.getParams());
        ApiErrorCode errorCode;
        try {
            errorCode = ApiErrorCode.getByCode(ex.getErrorCode());
        } catch (IllegalArgumentException e) {
            errorCode = ApiErrorCode.INTERNAL_SERVER_ERROR;
            message = "An unknown error occurred.";
        }
        String traceId = MDC.get(TRACE_ID) != null ? MDC.get(TRACE_ID) : N_A;
        ErrorResponse errorResponse = new ErrorResponse(
                errorCode.getCode(),
                message,
                errorCode.getHttpStatus(),
                traceId
        );
        return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
    }
}