package com.nexora.audit.exception;

public class AuditProcessingException extends BusinessException {

    public AuditProcessingException(String message) {
        super(message, "AUDIT_PROCESSING_FAILED", 500);
    }

    public AuditProcessingException(String message, Throwable cause) {
        super(message, "AUDIT_PROCESSING_FAILED", 500);
        initCause(cause);
    }
}