package com.nexora.audit.exception;

public class AuditLogNotFoundException extends BusinessException {

    public AuditLogNotFoundException(String id) {
        super(
                String.format("Audit log not found with id: %s", id),
                "AUDIT_LOG_NOT_FOUND",
                404
        );
    }

    public AuditLogNotFoundException(String field, String value) {
        super(
                String.format("Audit log with %s '%s' not found", field, value),
                "AUDIT_LOG_NOT_FOUND",
                404
        );
    }
}