package com.ai.claim.underwriter.exception;

public class ClaimUnderwriterException extends RuntimeException {

    private String errorCode;
    private Object errorDetails;

    public ClaimUnderwriterException(String message) {
        super(message);
    }

    public ClaimUnderwriterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClaimUnderwriterException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ClaimUnderwriterException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ClaimUnderwriterException(String message, String errorCode, Object errorDetails) {
        super(message);
        this.errorCode = errorCode;
        this.errorDetails = errorDetails;
    }

    public ClaimUnderwriterException(String message, String errorCode, Object errorDetails, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorDetails = errorDetails;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Object getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(Object errorDetails) {
        this.errorDetails = errorDetails;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ClaimUnderwriterException{");
        sb.append("message='").append(getMessage()).append('\'');
        if (errorCode != null) {
            sb.append(", errorCode='").append(errorCode).append('\'');
        }
        if (errorDetails != null) {
            sb.append(", errorDetails=").append(errorDetails);
        }
        sb.append('}');
        return sb.toString();
    }
}
