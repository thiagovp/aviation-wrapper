package com.sporty.aviation_wrapper.service;


import com.sporty.aviation_wrapper.client.AviationApiClient;
import com.sporty.aviation_wrapper.dto.AirportDto;
import com.sporty.aviation_wrapper.exception.AirportNotFoundException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Service layer for airport operations
 */
@Service
public class AirportService {

    private static final Logger log = LoggerFactory.getLogger(AirportService.class);

    private final AviationApiClient aviationApiClient;
    private final Counter requestCounter;
    private final Counter notFoundCounter;

    public AirportService(AviationApiClient aviationApiClient, MeterRegistry meterRegistry) {
        this.aviationApiClient = aviationApiClient;
        this.requestCounter = Counter.builder("airport_requests_total")
                .description("Total number of airport requests")
                .register(meterRegistry);
        this.notFoundCounter = Counter.builder("airport_not_found_total")
                .description("Total number of airport not found responses")
                .register(meterRegistry);
    }

    /**
     * Retrieves airport information by ICAO code
     * Results are cached for 15 minutes to reduce upstream calls
     */
    @Cacheable(value = "airports", key = "#icaoCode", unless = "#result == null")
    public AirportDto getAirportByIcao(String icaoCode) {
        log.info("Retrieving airport information for ICAO code: {}", icaoCode);
        requestCounter.increment();

        var airportsByIcao = aviationApiClient.getAirportsByIcao(icaoCode.toUpperCase());
        if(Objects.isNull(airportsByIcao)){
            notFoundCounter.increment();
            throw new AirportNotFoundException(icaoCode);
        }

        return airportsByIcao.toDto();
    }

}