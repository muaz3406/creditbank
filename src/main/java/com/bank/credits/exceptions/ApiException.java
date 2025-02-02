package com.bank.credits.exceptions;

public class ApiException extends RuntimeException {
    private final String errorCode;
    private final Object[] params;


    public ApiException(String errorCode, Object... params) {
        super(errorCode);
        this.errorCode = errorCode;
        this.params = params;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object[] getParams() {
        return params;
    }
}