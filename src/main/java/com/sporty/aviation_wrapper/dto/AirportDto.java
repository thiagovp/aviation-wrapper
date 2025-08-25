package com.sporty.aviation_wrapper.dto;

import java.io.Serializable;

/**
 * Data Transfer Object for Airport information
 */
public record AirportDto(
        String icao,
        String iata,
        String facility_name,
        String region,
        String district_office,
        String state,
        String state_full,
        String city,
        String county,
        String latitude,
        String longitude,
        Integer elevation
) implements Serializable {
}