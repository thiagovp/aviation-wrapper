# Aviation Wrapper API

A resilient and scalable wrapper API for aviation data, built with Spring Boot. This service provides airport information by connecting to the AviationAPI.com service with added features like caching, monitoring, and error handling.

## Features

- **Resilience**: Circuit breaker pattern and retry mechanisms
- **Scalability**: Lightweight Spring Boot application with caching
- **Extensibility**: Clean architecture with easy-to-extend endpoints
- **Observability**: Prometheus metrics and health checks
- **Testing**: Comprehensive unit and integration tests with WireMock

## Technology Stack

- **Java 21**: Modern Java features and performance
- **Spring Boot 3.5.5**: Latest framework version
- **Maven**: Dependency management and build tool
- **Spring Cache**: Simple in-memory caching (Redis upgrade recommended)
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
git clone <repository-url>
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
# Get airport information
curl http://localhost:8080/api/v1/airports/{airport-code}

# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/prometheus
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
| GET | `/api/v1/airports/{code}` | Get airport details by IATA/ICAO code |
| GET | `/actuator/health` | Application health status |
| GET | `/actuator/prometheus` | Prometheus metrics |
| GET | `/swagger-ui/index.html` | API documentation interface |

## Architecture Decisions

### 1. **Wrapper Pattern**
- Provides abstraction layer over external AviationAPI.com
- Enables adding business logic without changing external dependencies
- Allows for future API provider switching

### 2. **Caching Strategy**
- Simple in-memory cache for development and testing
- Airport data is relatively static, making caching effective
- **Future improvement**: Redis for distributed caching in production

### 3. **Error Handling**
- Global exception handler for consistent error responses
- Circuit breaker pattern to handle external API failures
- Retry mechanism with exponential backoff
- Graceful degradation when external API is unavailable

### 4. **Observability**
- Prometheus metrics for monitoring API performance
- Custom metrics for external API calls and cache hits
- Spring Boot Actuator for health checks and operational endpoints

### 5. **Testing Strategy**
- WireMock for integration testing without external dependencies

## Configuration

### Application Properties
```yaml
# External API configuration
aviation.api.base-url=https://aviationapi.com
aviation.api.timeout=5000
aviation.api.retry.max-attempts=3

# Cache configuration
spring.cache.type=simple
spring.cache.cache-names=airports

# Actuator endpoints
management.endpoints.web.exposure.include=health,prometheus
```

### Environment Variables
- `AVIATION_API_KEY`: API key for AviationAPI.com (if required)
- `SERVER_PORT`: Application port (default: 8080)
- `LOG_LEVEL`: Logging level (default: INFO)

## Assumptions

1. **External API Stability**: AviationAPI.com provides consistent response format
2. **Data Freshness**: Airport data doesn't change frequently, making caching suitable
3. **Load Patterns**: Moderate traffic expected, simple cache is sufficient initially
4. **Security**: API key authentication is sufficient for external API access

## Error Handling

### HTTP Status Codes
- `200`: Success
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
  "message": "Airport with code 'XYZ' not found",
  "path": "/api/v1/airports/XYZ"
}
```

## Monitoring and Metrics

### Key Metrics
- Request duration and count
- External API call success/failure rates
- Cache hit/miss ratios
- Circuit breaker state changes

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

All AI-generated code has been reviewed, tested, and customized for project requirements.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

GNU Public General License