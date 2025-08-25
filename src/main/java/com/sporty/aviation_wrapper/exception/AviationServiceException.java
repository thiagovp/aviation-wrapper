package com.sporty.aviation_wrapper.exception;

/**
 * Custom exception for aviation service operations
 */
public class AviationServiceException extends RuntimeException {

    public AviationServiceException(String message) {
        super(message);
    }

    public AviationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

