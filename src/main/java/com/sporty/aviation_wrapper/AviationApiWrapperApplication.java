package com.sporty.aviation_wrapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AviationApiWrapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(AviationApiWrapperApplication.class, args);
    }
}