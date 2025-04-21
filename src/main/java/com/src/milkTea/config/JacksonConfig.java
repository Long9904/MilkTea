package com.src.milkTea.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer stringTrimmer() {
        return builder -> builder.deserializerByType(String.class, new StringTrimDeserializer());
    }
}
