package com.sporty.aviation_wrapper.exception;

/**
 * Exception thrown when airport is not found
 */
public class AirportNotFoundException extends AviationServiceException {

    public AirportNotFoundException(String icaoCode) {
        super("Airport with ICAO code '" + icaoCode + "' not found");
    }
}
