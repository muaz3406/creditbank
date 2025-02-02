package com.bank.credits.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ApiErrorCode {
    NOT_FOUND_USER("ERR-10001", HttpStatus.NOT_FOUND),
    INVALID_USERNAME_PWD("ERR-10002", HttpStatus.BAD_REQUEST),
    LOGIN_SERVER_ERROR("ERR-10003", HttpStatus.BAD_REQUEST),
    ENTITY_NOT_FOUND("ERR-10004", HttpStatus.NOT_FOUND),
    USERNAME_EXISTS("ERR-10005", HttpStatus.BAD_REQUEST),

    CUSTOMER_NOT_FOUND("ERR-11001", HttpStatus.NOT_FOUND),
    INVALID_CREDIT_LIMIT("ERR-11002", HttpStatus.BAD_REQUEST),
    INVALID_NAME("ERR-11003", HttpStatus.BAD_REQUEST),
    INVALID_SURNAME("ERR-11004", HttpStatus.BAD_REQUEST),
    CREDIT_LIMIT_BELOW_USED("ERR-11005", HttpStatus.BAD_REQUEST),

    INVALID_REQUEST("ERR-12001", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR("ERR-12002", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_CREDIT_LIMIT("ERR-12003", HttpStatus.BAD_REQUEST),
    INVALID_INSTALLMENT_COUNT("ERR-12004", HttpStatus.BAD_REQUEST),
    INVALID_INTEREST_RATE("ERR-12005", HttpStatus.BAD_REQUEST),
    INVALID_LOAN_AMOUNT("ERR-12006", HttpStatus.BAD_REQUEST),

    NOT_FOUND_LOAN("ERR-13001", HttpStatus.NOT_FOUND),
    CONFIG_NOT_FOUND("ERR-13002", HttpStatus.NOT_FOUND),
    INVALID_PAYMENT_AMOUNT("ERR-13003", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_DATE("ERR-13004", HttpStatus.BAD_REQUEST),
    PAYMENT_PROCESSING_ERROR("ERR-13005", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_LOAN_ID("ERR-13006", HttpStatus.BAD_REQUEST),

    INTERNAL_SERVER_ERROR("ERR-99999", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final HttpStatus httpStatus;

    ApiErrorCode(String code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public static ApiErrorCode getByCode(String code) {
        for (ApiErrorCode errorCode : ApiErrorCode.values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }
}
