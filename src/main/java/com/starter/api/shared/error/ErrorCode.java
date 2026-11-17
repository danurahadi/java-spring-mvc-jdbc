package com.starter.api.shared.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_FAILED("validation_failed", HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST("malformed_request", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED("unauthenticated", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("forbidden", HttpStatus.FORBIDDEN),
    ACCOUNT_INACTIVE("account_inactive", HttpStatus.FORBIDDEN),
    NOT_FOUND("not_found", HttpStatus.NOT_FOUND),
    CONFLICT("conflict", HttpStatus.CONFLICT),
    RATE_LIMITED("rate_limited", HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL_ERROR("internal_error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final HttpStatus status;

    ErrorCode(String code, HttpStatus status) {
        this.code = code;
        this.status = status;
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }
}
