package com.sporty.aviation_wrapper.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.sporty.aviation_wrapper.dto.AviationApiResponse;
import com.sporty.aviation_wrapper.exception.AviationServiceException;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "aviation.api.base-url=http://localhost:8089",
    "resilience4j.circuitbreaker.instances.aviation-api.enabled=false",
    "resilience4j.retry.instances.aviation-api.enabled=false"
})
@DisplayName("Aviation API Client Integration Tests")
class AviationApiClientIntegrationTest {

    private static WireMockServer wireMockServer;
    private AviationApiClient aviationApiClient;
    private RestTemplate restTemplate;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8089));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
        restTemplate = new RestTemplate();
        aviationApiClient = new AviationApiClient(restTemplate, "http://localhost:8089");
    }

    @Test
    @DisplayName("Should successfully call Aviation API and parse response")
    void getAirportsByIcao_SuccessfulApiCall_ReturnsCorrectData() {
        // Given
        String icaoCode = "KBAB";
        String mockResponseJson = """
            {
                "KBAB": [
                    {
                        "icao_ident": "KBAB",
                        "faa_ident": "BAB",
                        "facility_name": "Beale Air Force Base",
                        "region": "Western",
                        "district_office": "Los Angeles ADO",
                        "state": "CA",
                        "state_full": "California",
                        "city": "Marysville",
                        "county": "Yuba County",
                        "latitude": "39.1361",
                        "longitude": "-121.4367",
                        "elevation": 113
                    }
                ]
            }
            """;

        wireMockServer.stubFor(get(urlPathEqualTo("/v1/airports"))
                .withQueryParam("apt", equalTo(icaoCode))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponseJson)));

        // When
        AviationApiResponse result = aviationApiClient.getAirportsByIcao(icaoCode);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.icao_ident()).isEqualTo("KBAB");
        assertThat(result.faa_ident()).isEqualTo("BAB");
        assertThat(result.facility_name()).isEqualTo("Beale Air Force Base");
        assertThat(result.city()).isEqualTo("Marysville");
        assertThat(result.state()).isEqualTo("CA");
        assertThat(result.state_full()).isEqualTo("California");
        assertThat(result.elevation()).isEqualTo(113);

        // Verify the request was made correctly
        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/v1/airports"))
                .withQueryParam("apt", equalTo(icaoCode)));
    }

    @Test
    @DisplayName("Should handle API returning 404 Not Found")
    void getAirportsByIcao_ApiReturns404_ThrowsException() {
        // Given
        String icaoCode = "XXXX";

        wireMockServer.stubFor(get(urlPathEqualTo("/v1/airports"))
                .withQueryParam("apt", equalTo(icaoCode))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Airport not found\"}")));

        // When & Then
        assertThatThrownBy(() -> aviationApiClient.getAirportsByIcao(icaoCode))
                .isInstanceOf(RestClientException.class);

        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/v1/airports"))
                .withQueryParam("apt", equalTo(icaoCode)));
    }

    @Test
    @DisplayName("Should handle API returning 500 Internal Server Error")
    void getAirportsByIcao_ApiReturns500_ThrowsException() {
        // Given
        String icaoCode = "KBAB";

        wireMockServer.stubFor(get(urlPathEqualTo("/v1/airports"))
                .withQueryParam("apt", equalTo(icaoCode))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Internal server error\"}")));

        // When & Then
        assertThatThrownBy(() -> aviationApiClient.getAirportsByIcao(icaoCode))
                .isInstanceOf(RestClientException.class);

        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/v1/airports"))
                .withQueryParam("apt", equalTo(icaoCode)));
    }

    @Test
    @DisplayName("Should handle API timeout")
    void getAirportsByIcao_ApiTimeout_ThrowsException() {
        // Given
        String icaoCode = "KBAB";

        wireMockServer.stubFor(get(urlPathEqualTo("/v1/airports"))
                .withQueryParam("apt", equalTo(icaoCode))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(5000) // 5 second delay to simulate timeout
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        // Configure RestTemplate with short timeout for this test
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1000);
        factory.setReadTimeout(1000);
        restTemplate.setRequestFactory(factory);
        aviationApiClient = new AviationApiClient(restTemplate, "http://localhost:8089");

        // When & Then
        assertThatThrownBy(() -> aviationApiClient.getAirportsByIcao(icaoCode))
                .isInstanceOf(RestClientException.class);
    }

    @Test
    @DisplayName("Should handle malformed JSON response")
    void getAirportsByIcao_MalformedJsonResponse_ThrowsAviationServiceException() {
        // Given
        String icaoCode = "KBAB";
        String malformedJson = "{ \"KBAB\": [ { invalid json } ] }";

        wireMockServer.stubFor(get(urlPathEqualTo("/v1/airports"))
                .withQueryParam("apt", equalTo(icaoCode))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(malformedJson)));

        // When & Then
        assertThatThrownBy(() -> aviationApiClient.getAirportsByIcao(icaoCode))
                .isInstanceOf(AviationServiceException.class);

        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/v1/airports"))
                .withQueryParam("apt", equalTo(icaoCode)));
    }

    @Test
    @DisplayName("Should handle empty response array")
    void getAirportsByIcao_EmptyResponseArray_ReturnsNull() {
        // Given
        String icaoCode = "KBAB";
        String emptyArrayResponse = """
            {
                "KBAB": []
            }
            """;

        wireMockServer.stubFor(get(urlPathEqualTo("/v1/airports"))
                .withQueryParam("apt", equalTo(icaoCode))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(emptyArrayResponse)));

        // When & Then
        assertThat(aviationApiClient.getAirportsByIcao(icaoCode)).isNull();

        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/v1/airports"))
                .withQueryParam("apt", equalTo(icaoCode)));
    }

    @Test
    @DisplayName("Should construct correct URL with ICAO code parameter")
    void getAirportsByIcao_CorrectUrlConstruction_MakesRequestWithCorrectParameters() {
        // Given
        String icaoCode = "EGLL";
        String mockResponseJson = """
            {
                "EGLL": [
                    {
                        "icao_ident": "EGLL",
                        "faa_ident": "LHR",
                        "facility_name": "London Heathrow Airport",
                        "region": "European",
                        "district_office": "London ADO",
                        "state": "",
                        "state_full": "England",
                        "city": "London",
                        "county": "Greater London",
                        "latitude": "51.4700",
                        "longitude": "-0.4543",
                        "elevation": 83
                    }
                ]
            }
            """;

        wireMockServer.stubFor(get(urlPathEqualTo("/v1/airports"))
                .withQueryParam("apt", equalTo(icaoCode))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponseJson)));

        // When
        AviationApiResponse result = aviationApiClient.getAirportsByIcao(icaoCode);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.icao_ident()).isEqualTo("EGLL");

        // Verify the exact request parameters
        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/v1/airports"))
                .withQueryParam("apt", equalTo("EGLL")));

        // Additional test for toDto() method
        var airportDto = result.toDto();
        assertThat(airportDto.icao()).isEqualTo("EGLL");
        assertThat(airportDto.facility_name()).isEqualTo("London Heathrow Airport");
    }

    @Test
    @DisplayName("Should handle multiple consecutive requests correctly")
    void getAirportsByIcao_MultipleRequests_HandlesCorrectly() {
        // Given
        String[] icaoCodes = {"KBAB", "EGLL", "KJFK"};
        
        // Setup responses for each ICAO code
        for (String icaoCode : icaoCodes) {
            String mockResponseJson = String.format("""
                {
                    "%s": [
                        {
                            "icao_ident": "%s",
                            "faa_ident": "TST",
                            "facility_name": "Test Airport %s",
                            "region": "Test Region",
                            "district_office": "Test ADO",
                            "state": "TS",
                            "state_full": "Test State",
                            "city": "Test City",
                            "county": "Test County",
                            "latitude": "0.0000",
                            "longitude": "0.0000",
                            "elevation": 0
                        }
                    ]
                }
                """, icaoCode, icaoCode, icaoCode);

            wireMockServer.stubFor(get(urlPathEqualTo("/v1/airports"))
                    .withQueryParam("apt", equalTo(icaoCode))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponseJson)));
        }

        // When & Then
        for (String icaoCode : icaoCodes) {
            AviationApiResponse result = aviationApiClient.getAirportsByIcao(icaoCode);
            
            assertThat(result).isNotNull();
            assertThat(result.icao_ident()).isEqualTo(icaoCode);
            assertThat(result.facility_name()).isEqualTo("Test Airport " + icaoCode);
            
            wireMockServer.verify(getRequestedFor(urlPathEqualTo("/v1/airports"))
                    .withQueryParam("apt", equalTo(icaoCode)));
        }
    }

    @Test
    @DisplayName("Should handle API returning partial data")
    void getAirportsByIcao_PartialDataResponse_ParsesAvailableFields() {
        // Given
        String icaoCode = "KMIN";
        String partialDataResponse = """
            {
                "KMIN": [
                    {
                        "icao_ident": "KMIN",
                        "facility_name": "Minimal Airport",
                        "city": "Minimal City",
                        "latitude": "45.0000",
                        "longitude": "-93.0000"
                    }
                ]
            }
            """;

        wireMockServer.stubFor(get(urlPathEqualTo("/v1/airports"))
                .withQueryParam("apt", equalTo(icaoCode))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(partialDataResponse)));

        // When
        AviationApiResponse result = aviationApiClient.getAirportsByIcao(icaoCode);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.icao_ident()).isEqualTo("KMIN");
        assertThat(result.facility_name()).isEqualTo("Minimal Airport");
        assertThat(result.city()).isEqualTo("Minimal City");
        // Fields not provided in JSON should be null
        assertThat(result.faa_ident()).isNull();
        assertThat(result.state()).isNull();
        assertThat(result.elevation()).isNull();

        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/v1/airports"))
                .withQueryParam("apt", equalTo(icaoCode)));
    }
}