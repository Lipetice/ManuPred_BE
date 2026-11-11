package com.fiec.br.back_end.kipper.config;

import org.bson.UuidRepresentation;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    @Bean
    public MongoClientSettingsBuilderCustomizer customizer() {
        return builder -> builder.uuidRepresentation(UuidRepresentation.STANDARD);
    }
}