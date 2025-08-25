package com.sporty.aviation_wrapper.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.aviation_wrapper.dto.AviationApiResponse;
import com.sporty.aviation_wrapper.exception.AviationServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


/**
 * Client for interacting with Aviation API
 */
@Component
public class AviationApiClient {
    
    private static final Logger log = LoggerFactory.getLogger(AviationApiClient.class);
    private static final String CIRCUIT_BREAKER_NAME = "aviation-api";
    private static final String RETRY_NAME = "aviation-api";

    private final String baseUrl;
    private final RestTemplate restTemplate;
    
    public AviationApiClient(RestTemplate restTemplate,
            @Value("${aviation.api.base-url:https://api.aviationapi.com}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
    }
    
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "fallbackGetAirports")
    @Retry(name = RETRY_NAME)
    public AviationApiResponse getAirportsByIcao(String icaoCode) {
        log.debug("Fetching airport data for ICAO code: {}", icaoCode);

        try {
            var response = restTemplate.getForObject(UriComponentsBuilder.fromUriString(baseUrl)
                    .path("/v1/airports")
                    .queryParam("apt", icaoCode)
                    .build().toUriString(), String.class);
            ObjectMapper objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode dataNode = rootNode.get(icaoCode);
            return objectMapper.treeToValue(dataNode.get(0), AviationApiResponse.class);
        } catch (JsonProcessingException e) {
            throw new AviationServiceException("", e);
        }

    }
    
    /**
     * Fallback method when circuit breaker is open
     */
    public AviationApiResponse fallbackGetAirports(String icaoCode, Exception ex) {
        log.warn("Aviation API circuit breaker activated for ICAO: {}. Reason: {}", 
            icaoCode, ex.getMessage());

        throw new AviationServiceException(
                "Aviation service temporarily unavailable. Please try again later.", ex);
    }
}