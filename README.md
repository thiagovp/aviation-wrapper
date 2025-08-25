# Aviation Wrapper API

A resilient and scalable wrapper API for aviation data, built with Spring Boot. This service provides airport information by connecting to the AviationAPI.com service with added features like caching, monitoring, and error handling.

## Features

- **Resilience**: Circuit breaker, retry mechanisms, and rate limiting with Resilience4j
- **Scalability**: Lightweight Spring Boot application with Caffeine caching
- **Extensibility**: Clean architecture with easy-to-extend endpoints
- **Observability**: Prometheus metrics, health checks, and circuit breaker monitoring
- **Testing**: Comprehensive unit and integration tests with WireMock

## Technology Stack

- **Java 21**: Modern Java features and performance
- **Spring Boot 3.5.5**: Latest framework version
- **Maven**: Dependency management and build tool
- **Caffeine Cache**: High-performance in-memory caching (Redis upgrade recommended)
- **Resilience4j**: Circuit breaker, retry, and rate limiting
- **Prometheus**: Metrics collection and monitoring
- **WireMock**: Integration testing with external API mocks
- **Docker Compose**: Container orchestration

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker and Docker Compose
- Internet connection (for AviationAPI.com access)

## Quick Start

### 1. Clone and Build

```bash
git clone https://github.com/thiagovp/aviation-wrapper.git
cd aviation-wrapper
mvn clean package
```

### 2. Run with Docker Compose

```bash
docker-compose up -d
```

This will start:
- Aviation Wrapper API on `http://localhost:8080`
- Prometheus on `http://localhost:9090`

### 3. Test the API

```bash
# Get airport information by ICAO code
curl http://localhost:8080/api/v1/airports/KBAB

# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/prometheus

# Circuit breaker status
curl http://localhost:8080/actuator/circuitbreakers
```

### 4. Access Web Interfaces

- **API Documentation**: http://localhost:8080/swagger-ui/index.html
- **Prometheus Query Interface**: http://localhost:9090/query

## Running Tests

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Test Coverage
```bash
mvn jacoco:report
# Report available at target/site/jacoco/index.html
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/airports/{icaoCode}` | Get airport details by 4-letter ICAO code |
| GET | `/actuator/health` | Application health status |
| GET | `/actuator/metrics` | Application metrics |
| GET | `/actuator/prometheus` | Prometheus metrics |
| GET | `/actuator/circuitbreakers` | Circuit breaker status |
| GET | `/swagger-ui/index.html` | API documentation interface |

## Architecture Decisions

### 1. **Wrapper Pattern**
- Provides abstraction layer over external AviationAPI.com
- Enables adding business logic without changing external dependencies
- Allows for future API provider switching

### 2. **Caching Strategy**
- Caffeine cache for high-performance in-memory caching
- Maximum 1000 entries with 15-minute expiration
- Airport data is relatively static, making caching effective
- **Future improvement**: Redis for distributed caching in production

### 3. **Resilience Pattern**
- **Circuit Breaker**: 50% failure rate threshold, 30-second recovery time
- **Retry Logic**: 3 attempts with exponential backoff (1s, 2s, 4s)
- **Rate Limiting**: 100 requests per minute to external API
- Automatic health monitoring and recovery mechanisms

### 4. **Error Handling**
- Global exception handler for consistent error responses
- Input validation for ICAO codes (4 letters, alphabetic only)
- Resilience4j circuit breaker for external API failures
- Retry mechanism with exponential backoff for transient errors
- Rate limiting to prevent external API overload
- Graceful degradation when external API is unavailable

### 5. **Observability**
- Prometheus metrics for monitoring API performance
- Circuit breaker health indicators and status monitoring
- Custom metrics for external API calls and cache performance
- Spring Boot Actuator for comprehensive operational endpoints

### 6. **Testing Strategy**
- WireMock for integration testing without external dependencies
- Unit tests for general coverage

## Configuration

### Application Properties
```yaml
# Server configuration
server:
  port: 8080
  shutdown: graceful

# External API configuration
aviation:
  api:
    base-url: https://api.aviationapi.com

# Caffeine cache configuration
spring:
  cache:
    type: simple
    cache-names: airports
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=15m

# Resilience4j configuration
resilience4j:
  circuitbreaker:
    instances:
      aviation-api:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
  retry:
    instances:
      aviation-api:
        max-attempts: 3
        wait-duration: 1s
        enable-exponential-backoff: true
  ratelimiter:
    instances:
      aviation-api:
        limit-for-period: 100
        limit-refresh-period: 60s

# Actuator endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,circuitbreakers
```


## Assumptions

1. **External API Stability**: AviationAPI.com provides consistent response format
2. **ICAO Code Standard**: API uses 4-letter ICAO codes exclusively (not IATA codes)
3. **Data Freshness**: Airport data doesn't change frequently, making caching suitable
4. **Load Patterns**: Moderate traffic expected, Caffeine cache is sufficient initially
5. **Input Validation**: Strict ICAO format validation prevents unnecessary external calls

## Error Handling

### HTTP Status Codes
- `200`: Success
- `400`: Invalid ICAO code format (must be 4 letters)
- `404`: Airport not found
- `429`: Rate limit exceeded (with retry-after header)
- `500`: Internal server error
- `502`: External API unavailable
- `503`: Service temporarily unavailable (circuit breaker open)

### Error Response Format
```json
{
  "timestamp": "2024-01-01T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Airport with ICAO code 'ABCD' not found",
  "path": "/api/v1/airports/ABCD"
}
```

### Validation Error Example
```json
{
  "timestamp": "2024-01-01T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "ICAO code must be exactly 4 characters",
  "path": "/api/v1/airports/ABC"
}
```

## Monitoring and Metrics

### Key Metrics
- Request duration and count
- External API call success/failure rates
- Cache hit/miss ratios (Caffeine cache statistics)
- Circuit breaker state changes and failure rates
- Rate limiter usage and throttling events

### Prometheus Queries
```promql
# Request rate
rate(http_requests_total[5m])

# Error rate
rate(http_requests_total{status=~"5.."}[5m])

# Cache effectiveness
cache_gets_total{result="hit"} / cache_gets_total
```

## Future Improvements

1. **Redis Cache**: Replace simple cache with Redis for production
2. **Database Integration**: Store frequently accessed data locally
3. **Rate Limiting**: Implement API rate limiting
4. **Authentication**: Add API key or JWT authentication
5. **Load Balancing**: Support for multiple instances

## AI-Generated Code

This project uses AI assistance for:
- Initial project structure and configuration
- Test case generation and edge case identification
- Documentation and README creation
- Code review and optimization suggestions

All AI-generated code has been reviewed, tested, and customized for project requirements.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

### GPL v3.0 Summary

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.