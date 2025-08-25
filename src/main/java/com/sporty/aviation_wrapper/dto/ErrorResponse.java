package com.sporty.aviation_wrapper.dto;

import java.time.LocalDateTime;

/**
 * Standard error response structure
 */
public record ErrorResponse(
    String error,
    String message,
    String path,
    LocalDateTime timestamp,
    int status
) {
    public static ErrorResponse of(String error, String message, String path, int status) {
        return new ErrorResponse(error, message, path, LocalDateTime.now(), status);
    }
}