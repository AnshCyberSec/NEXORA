package com.nexora.fraud.exception;

public class FraudAssessmentNotFoundException extends BusinessException {

    public FraudAssessmentNotFoundException(String id) {
        super(
                String.format("Fraud assessment not found with id: %s", id),
                "FRAUD_ASSESSMENT_NOT_FOUND",
                404
        );
    }

    public FraudAssessmentNotFoundException(String field, String value) {
        super(
                String.format("Fraud assessment with %s '%s' not found", field, value),
                "FRAUD_ASSESSMENT_NOT_FOUND",
                404
        );
    }
}