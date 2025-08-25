package com.sporty.aviation_wrapper.dto;

/**
 * Response structure from Aviation API
 */

public record AviationApiResponse(
        String icao_ident,
        String faa_ident,
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
) {
    


        public AirportDto toDto() {
            return new AirportDto(
                icao_ident, faa_ident,facility_name, region, district_office,state, state_full,  city, county,
                   latitude, longitude, elevation
            );
        }

}