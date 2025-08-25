package com.sporty.aviation_wrapper.exception;

/**
 * Exception thrown when upstream service is unavailable
 */
public class UpstreamServiceException extends AviationServiceException {

    public UpstreamServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
