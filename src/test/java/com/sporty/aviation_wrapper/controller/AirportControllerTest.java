package com.sporty.aviation_wrapper.controller;

import com.sporty.aviation_wrapper.dto.AirportDto;
import com.sporty.aviation_wrapper.exception.UpstreamServiceException;
import com.sporty.aviation_wrapper.service.AirportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Airport Controller Tests")
class AirportControllerTest {


        @Mock
        private AirportService airportService;

        @InjectMocks
        private AirportController airportController;

        private AirportDto sampleAirportDto;

        @BeforeEach
        void setUp() {
            // Create sample AirportDto record for testing
            sampleAirportDto = new AirportDto(
                    "KBAB",                    // icao
                    "BAB",                     // iata
                    "Sample Airport",          // facility_name
                    "Eastern",                 // region
                    "New York ADO",           // district_office
                    "NY",                     // state
                    "New York",               // state_full
                    "Sample City",            // city
                    "Sample County",          // county
                    "40.7589",                // latitude
                    "-73.7781",               // longitude
                    100                       // elevation
            );
        }

        @Test
        @DisplayName("Should return airport when valid ICAO code is provided")
        void getAirportByIcao_ValidIcaoCode_ReturnsAirport() {
            // Given
            String validIcaoCode = "KBAB";
            when(airportService.getAirportByIcao(validIcaoCode)).thenReturn(sampleAirportDto);

            // When
            ResponseEntity<AirportDto> response = airportController.getAirportByIcao(validIcaoCode);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().icao()).isEqualTo(validIcaoCode);
            verify(airportService, times(1)).getAirportByIcao(validIcaoCode);
        }

        @Test
        @DisplayName("Should throw UpstreamServiceException when service throws exception")
        void getAirportByIcao_ServiceThrowsException_ThrowsUpstreamServiceException() {
            // Given
            String validIcaoCode = "KBAB";
            RuntimeException serviceException = new RuntimeException("Service error");
            when(airportService.getAirportByIcao(validIcaoCode)).thenThrow(serviceException);

            // When & Then
            assertThatThrownBy(() -> airportController.getAirportByIcao(validIcaoCode))
                    .isInstanceOf(UpstreamServiceException.class)
                    .hasMessage("Service error")
                    .hasCause(serviceException);

            verify(airportService, times(1)).getAirportByIcao(validIcaoCode);
        }

        @Test
        @DisplayName("Should accept uppercase ICAO code")
        void getAirportByIcao_UppercaseIcaoCode_ReturnsAirport() {
            // Given
            String uppercaseIcaoCode = "KBAB";
            when(airportService.getAirportByIcao(uppercaseIcaoCode)).thenReturn(sampleAirportDto);

            // When
            ResponseEntity<AirportDto> response = airportController.getAirportByIcao(uppercaseIcaoCode);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(airportService, times(1)).getAirportByIcao(uppercaseIcaoCode);
        }

        @Test
        @DisplayName("Should accept lowercase ICAO code")
        void getAirportByIcao_LowercaseIcaoCode_ReturnsAirport() {
            // Given
            String lowercaseIcaoCode = "kbab";
            when(airportService.getAirportByIcao(lowercaseIcaoCode)).thenReturn(sampleAirportDto);

            // When
            ResponseEntity<AirportDto> response = airportController.getAirportByIcao(lowercaseIcaoCode);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(airportService, times(1)).getAirportByIcao(lowercaseIcaoCode);
        }

        @Test
        @DisplayName("Should call service exactly once for valid request")
        void getAirportByIcao_ValidRequest_CallsServiceOnce() {
            // Given
            String validIcaoCode = "KJFK";
            when(airportService.getAirportByIcao(validIcaoCode)).thenReturn(sampleAirportDto);

            // When
            airportController.getAirportByIcao(validIcaoCode);

            // Then
            verify(airportService, times(1)).getAirportByIcao(validIcaoCode);
            verifyNoMoreInteractions(airportService);
        }
    }
