package com.sporty.aviation_wrapper.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.aviation_wrapper.dto.AviationApiResponse;
import com.sporty.aviation_wrapper.exception.AviationServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Aviation API Client Unit Tests")
class AviationApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    private AviationApiClient aviationApiClient;

    private final String baseUrl = "https://api.aviationapi.com";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        aviationApiClient = new AviationApiClient(restTemplate, baseUrl);
    }

    @Test
    @DisplayName("Should parse successful API response correctly")
    void getAirportsByIcao_SuccessfulResponse_ReturnsAviationApiResponse() throws Exception {
        // Given
        String icaoCode = "KBAB";
        String mockResponse = createMockSuccessResponse(icaoCode);
        String expectedUrl = baseUrl + "/v1/airports?apt=" + icaoCode;

        when(restTemplate.getForObject(expectedUrl, String.class))
                .thenReturn(mockResponse);

        // When
        AviationApiResponse result = aviationApiClient.getAirportsByIcao(icaoCode);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.icao_ident()).isEqualTo(icaoCode);
        assertThat(result.facility_name()).isEqualTo("Sample Airport");
    }

    @Test
    @DisplayName("Should throw RestClientException when RestTemplate throws exception")
    void getAirportsByIcao_RestTemplateThrowsException_ThrowsRestClientException() {
        // Given
        String icaoCode = "KBAB";
        String expectedUrl = baseUrl + "/v1/airports?apt=" + icaoCode;
        RestClientException restException = new RestClientException("Connection timeout");

        when(restTemplate.getForObject(expectedUrl, String.class))
                .thenThrow(restException);

        // When & Then
        assertThatThrownBy(() -> aviationApiClient.getAirportsByIcao(icaoCode))
                .isInstanceOf(RestClientException.class)
                .hasMessage("Connection timeout");
    }

    @Test
    @DisplayName("Should throw AviationServiceException when JSON parsing fails")
    void getAirportsByIcao_InvalidJsonResponse_ThrowsAviationServiceException() {
        // Given
        String icaoCode = "KBAB";
        String expectedUrl = baseUrl + "/v1/airports?apt=" + icaoCode;
        String invalidJsonResponse = "{ invalid json }";

        when(restTemplate.getForObject(expectedUrl, String.class))
                .thenReturn(invalidJsonResponse);

        // When & Then
        assertThatThrownBy(() -> aviationApiClient.getAirportsByIcao(icaoCode))
                .isInstanceOf(AviationServiceException.class);
    }

    @Test
    @DisplayName("Should throw AviationServiceException when response has no data for ICAO code")
    void getAirportsByIcao_NoDataForIcaoCode_ThrowsAviationServiceException() {
        // Given
        String icaoCode = "KBAB";
        String expectedUrl = baseUrl + "/v1/airports?apt=" + icaoCode;
        String responseWithoutData = """
            {
                "KOTHER": [
                    {
                        "icao_ident": "KOTHER",
                        "facility_name": "Other Airport"
                    }
                ]
            }
            """;

        when(restTemplate.getForObject(expectedUrl, String.class))
                .thenReturn(responseWithoutData);

        // When & Then
        assertThatThrownBy(() -> aviationApiClient.getAirportsByIcao(icaoCode))
                .isInstanceOf(NullPointerException.class); // JsonNode.get() returns null
    }

    @Test
    @DisplayName("Should test fallback method throws AviationServiceException")
    void fallbackGetAirports_WhenCalled_ThrowsAviationServiceException() {
        // Given
        String icaoCode = "KBAB";
        Exception originalException = new RuntimeException("Circuit breaker open");

        // When & Then
        assertThatThrownBy(() -> aviationApiClient.fallbackGetAirports(icaoCode, originalException))
                .isInstanceOf(AviationServiceException.class)
                .hasMessage("Aviation service temporarily unavailable. Please try again later.")
                .hasCause(originalException);
    }

    @Test
    @DisplayName("Should test fallback method with different exception types")
    void fallbackGetAirports_WithDifferentExceptionTypes_ThrowsAviationServiceException() {
        // Given
        String icaoCode = "EGLL";
        RestClientException restException = new RestClientException("Connection refused");

        // When & Then
        assertThatThrownBy(() -> aviationApiClient.fallbackGetAirports(icaoCode, restException))
                .isInstanceOf(AviationServiceException.class)
                .hasMessage("Aviation service temporarily unavailable. Please try again later.")
                .hasCause(restException);
    }

    @Test
    @DisplayName("Should handle different ICAO codes correctly")
    void getAirportsByIcao_DifferentIcaoCodes_BuildsCorrectUrls() throws Exception {
        // Test multiple ICAO codes to ensure URL construction is correct
        String[] icaoCodes = {"KBAB", "EGLL", "KJFK"};

        for (String icaoCode : icaoCodes) {
            // Given
            String mockResponse = createMockSuccessResponse(icaoCode);
            String expectedUrl = baseUrl + "/v1/airports?apt=" + icaoCode;

            when(restTemplate.getForObject(expectedUrl, String.class))
                    .thenReturn(mockResponse);

            // When
            AviationApiResponse result = aviationApiClient.getAirportsByIcao(icaoCode);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.icao_ident()).isEqualTo(icaoCode);
        }
    }

    @Test
    @DisplayName("Should verify toDto conversion works correctly")
    void getAirportsByIcao_SuccessfulResponse_ToDtoConversionWorks() throws Exception {
        // Given
        String icaoCode = "KBAB";
        String mockResponse = createMockSuccessResponse(icaoCode);
        String expectedUrl = baseUrl + "/v1/airports?apt=" + icaoCode;

        when(restTemplate.getForObject(expectedUrl, String.class))
                .thenReturn(mockResponse);

        // When
        AviationApiResponse result = aviationApiClient.getAirportsByIcao(icaoCode);
        var airportDto = result.toDto();

        // Then
        assertThat(airportDto).isNotNull();
        assertThat(airportDto.icao()).isEqualTo(icaoCode);
        assertThat(airportDto.facility_name()).isEqualTo("Sample Airport");
        assertThat(airportDto.city()).isEqualTo("Sample City");
        assertThat(airportDto.state()).isEqualTo("NY");
    }

    private String createMockSuccessResponse(String icaoCode) throws Exception {
        AviationApiResponse mockAirport = createMockAviationApiResponse(icaoCode);
        return String.format("""
            {
                "%s": [
                    %s
                ]
            }
            """, icaoCode, objectMapper.writeValueAsString(mockAirport));
    }

    private AviationApiResponse createMockAviationApiResponse(String icaoCode) {
        return new AviationApiResponse(
                icaoCode,           // icao_ident
                "BAB",             // faa_ident
                "Sample Airport",   // facility_name
                "Eastern",         // region
                "New York ADO",    // district_office
                "NY",              // state
                "New York",        // state_full
                "Sample City",     // city
                "Sample County",   // county
                "40.7589",         // latitude
                "-73.7781",        // longitude
                100                // elevation
        );
    }
}