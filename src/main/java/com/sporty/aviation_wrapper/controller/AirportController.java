package com.sporty.aviation_wrapper.controller;

import com.sporty.aviation_wrapper.dto.AirportDto;
import com.sporty.aviation_wrapper.dto.ErrorResponse;
import com.sporty.aviation_wrapper.exception.UpstreamServiceException;
import com.sporty.aviation_wrapper.service.AirportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for airport operations
 */
@RestController
@RequestMapping("/api/v1/airports")
@Validated
@Tag(name = "Airport API", description = "Operations for retrieving airport information")
public class AirportController {
    
    private static final Logger log = LoggerFactory.getLogger(AirportController.class);
    
    private final AirportService airportService;
    
    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }
    
    @GetMapping("/{icaoCode}")
    @Operation(summary = "Get airport by ICAO code", 
               description = "Retrieves detailed airport information using the 4-letter ICAO code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Airport found"),
        @ApiResponse(responseCode = "400", description = "Invalid ICAO code format", content =
                { @Content(mediaType = "application/json", schema =
                @Schema(implementation = ErrorResponse.class)) }),
        @ApiResponse(responseCode = "404", description = "Airport not found", content = { @Content(mediaType = "application/json", schema =
        @Schema(implementation = ErrorResponse.class)) }) ,
        @ApiResponse(responseCode = "503", description = "Service unavailable", content = { @Content(mediaType = "application/json", schema =
        @Schema(implementation = ErrorResponse.class)) })
    })
    public ResponseEntity<AirportDto> getAirportByIcao(
            @PathVariable 
            @Parameter(description = "4-letter ICAO code (e.g., KBAB for London Heathrow)",
                      example = "KBAB")
            @NotBlank(message = "ICAO code cannot be blank")
            @Size(min = 4, max = 4, message = "ICAO code must be exactly 4 characters")
            @Pattern(regexp = "^[A-Za-z]{4}$", message = "ICAO code must contain only letters")
            String icaoCode) {
        
        log.info("Received request for airport with ICAO code: {}", icaoCode);

        try {
            ResponseEntity<AirportDto> response = ResponseEntity.ok(airportService.getAirportByIcao(icaoCode));

            log.info("Successfully processed request for ICAO: {}", icaoCode);

            return response;
        } catch (Exception e) {
            throw new UpstreamServiceException(e.getMessage(),e);
        }
    }
}