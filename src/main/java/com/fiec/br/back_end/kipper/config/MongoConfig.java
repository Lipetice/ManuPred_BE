package com.fiec.br.back_end.kipper.config;

import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public MongoClientSettingsBuilderCustomizer customizer() {
        System.out.println("🔥🔥🔥 CONECTANDO AO BANCO: " + mongoUri);
        return builder -> builder.uuidRepresentation(UuidRepresentation.STANDARD);
    }
}