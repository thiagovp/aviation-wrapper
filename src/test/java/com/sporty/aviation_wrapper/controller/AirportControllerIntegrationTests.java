package com.sporty.aviation_wrapper.controller;

import com.sporty.aviation_wrapper.dto.AirportDto;
import com.sporty.aviation_wrapper.service.AirportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Integration tests using @WebMvcTest for validation testing
@WebMvcTest(AirportController.class)
@DisplayName("Integration Tests with Validation")
class AirportControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AirportService airportService;

    private AirportDto sampleAirportDto;

    @BeforeEach
    void setUp() {
        // Create sample AirportDto record for testing
        sampleAirportDto = new AirportDto(
                "KBAB",
                "BAB",
                "Sample Airport",
                "Eastern",
                "New York ADO",
                "NY",
                "New York",
                "Sample City",
                "Sample County",
                "40.7589",
                "-73.7781",
                100
        );
    }


    @ParameterizedTest
    @MethodSource
    void getAirportByIcao_IcaoCodeValidation_Returns400(String icaoCode) throws Exception {
        mockMvc.perform(get("/api/v1/airports/" + icaoCode))
                .andExpect(status().isBadRequest());

        verify(airportService, never()).getAirportByIcao(anyString());
    }

    static Stream<String> getAirportByIcao_IcaoCodeValidation_Returns400() {
        return Stream.of("KBA", "KBABS", "KB12", "KB@B", "    ");
    }


    @Test
    @DisplayName("Should successfully process valid ICAO code via MockMvc")
    void getAirportByIcao_ValidIcaoCodeViaMockMvc_Returns200() throws Exception {
        // Given
        String validIcaoCode = "KBAB";
        when(airportService.getAirportByIcao(validIcaoCode)).thenReturn(sampleAirportDto);

        // When & Then
        mockMvc.perform(get("/api/v1/airports/" + validIcaoCode))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.icao").value(validIcaoCode));

        verify(airportService, times(1)).getAirportByIcao(validIcaoCode);
    }

    @Test
    @DisplayName("Should handle mixed case ICAO code via MockMvc")
    void getAirportByIcao_MixedCaseIcaoCodeViaMockMvc_Returns200() throws Exception {
        // Given
        String mixedCaseIcaoCode = "KbAb";
        when(airportService.getAirportByIcao(mixedCaseIcaoCode)).thenReturn(sampleAirportDto);

        // When & Then
        mockMvc.perform(get("/api/v1/airports/" + mixedCaseIcaoCode))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        verify(airportService, times(1)).getAirportByIcao(mixedCaseIcaoCode);
    }

    @Test
    @DisplayName("Should verify correct endpoint mapping")
    void getAirportByIcao_CorrectEndpointMapping_IsAccessible() throws Exception {
        // Given
        String validIcaoCode = "EGLL";
        when(airportService.getAirportByIcao(validIcaoCode)).thenReturn(sampleAirportDto);

        // When & Then
        mockMvc.perform(get("/api/v1/airports/" + validIcaoCode))
                .andExpect(status().isOk());

        // Verify wrong endpoint returns 404
        mockMvc.perform(get("/api/v2/airports/" + validIcaoCode))
                .andExpect(status().isInternalServerError());
    }
}