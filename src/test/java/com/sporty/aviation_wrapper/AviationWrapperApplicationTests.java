package com.sporty.aviation_wrapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "aviation.api.base-url=http://localhost:9999",
        "resilience4j.circuitbreaker.instances.aviation-api.minimum-number-of-calls=1"
})
class AviationApiWrapperApplicationTests {

    @Test
    void contextLoads() {
        // Test that Spring context loads successfully
    }
}