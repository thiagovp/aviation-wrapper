package com.sporty.aviation_wrapper.exception;


import com.sporty.aviation_wrapper.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler for the application
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BAD_REQUEST = "Bad Request";

    @ExceptionHandler(AviationServiceException.class)
    public ResponseEntity<ErrorResponse> handleAviationServiceException(
            AviationServiceException ex, HttpServletRequest request) {
        
        log.error("Aviation service error: {}", ex.getMessage(), ex);
        
        HttpStatus status = determineHttpStatus(ex);
        ErrorResponse errorResponse = ErrorResponse.of(
            status.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI(),
            status.value()
        );
        
        return new ResponseEntity<>(errorResponse, status);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ConstraintViolationException ex, HttpServletRequest request) {

        log.warn("Validation error: {}", ex.getMessage());

        String message = ex.getConstraintViolations().stream()
            .findFirst()
            .map(ConstraintViolation::getMessage)
            .orElse("Validation failed");

        ErrorResponse errorResponse = ErrorResponse.of(
                BAD_REQUEST,
            message,
            request.getRequestURI(),
            HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("Method argument validation error: {}", ex.getMessage());

        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getDefaultMessage())
            .orElse("Invalid request parameters");

        ErrorResponse errorResponse = ErrorResponse.of(
                BAD_REQUEST,
            message,
            request.getRequestURI(),
            HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.warn("Type mismatch error: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                BAD_REQUEST,
            "Invalid parameter format: " + ex.getName(),
            request.getRequestURI(),
            HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "Internal Server Error",
            "An unexpected error occurred. Please try again later.",
            request.getRequestURI(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    private HttpStatus determineHttpStatus(AviationServiceException ex) {
        String message = ex.getMessage().toLowerCase();
        
        if (message.contains("not found")) {
            return HttpStatus.NOT_FOUND;
        } else if (message.contains("rate limit") || message.contains("too many requests")) {
            return HttpStatus.TOO_MANY_REQUESTS;
        } else if (message.contains("unavailable") || message.contains("circuit breaker")) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else if (message.contains("timeout")) {
            return HttpStatus.REQUEST_TIMEOUT;
        }
        
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}