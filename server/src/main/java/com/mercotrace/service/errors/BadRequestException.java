package com.mercotrace.service.errors;

/**
 * Service-layer exception for bad request scenarios (validation, not found, etc.).
 * Used instead of web-layer BadRequestAlertException to satisfy layered architecture.
 * ExceptionTranslator translates this to HTTP 400 with the same JSON structure.
 */
public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String entityName;
    private final String errorKey;

    public BadRequestException(String defaultMessage, String entityName, String errorKey) {
        super(defaultMessage);
        this.entityName = entityName;
        this.errorKey = errorKey;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getErrorKey() {
        return errorKey;
    }
}
