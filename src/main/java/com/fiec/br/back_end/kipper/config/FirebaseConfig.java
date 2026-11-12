package com.fiec.br.back_end.kipper.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class FirebaseConfig {

    // Conteúdo bruto do JSON, injetado via secret -> env var FIREBASE_CREDENTIALS_JSON
    @Value("${app.firebase.credentials-json:}")
    private String credentialsJson;

    // Fallback local (dev): arquivo em src/main/resources
    @Value("${app.firebase.config-file:firebase-service-account.json}")
    private String fallbackConfigFile;

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (InputStream credentials = resolveCredentialsStream()) {
            if (credentials == null) {
                // Não derruba a aplicação: sem credenciais só ficam indisponíveis as notificações
                // push e o login via Firebase (ambos já tratam a falha).
                log.error("Credenciais do Firebase não encontradas (env FIREBASE_CREDENTIALS_JSON vazia e "
                        + "arquivo '{}' ausente). Notificações push e login via Firebase ficarão indisponíveis.",
                        fallbackConfigFile);
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentials))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase inicializado.");
        } catch (IOException | RuntimeException e) {
            log.error("Falha ao inicializar o Firebase: {}", e.getMessage());
        }
    }

    private InputStream resolveCredentialsStream() throws IOException {
        if (credentialsJson != null && !credentialsJson.isBlank()) {
            // Lê direto da memória: não precisa gravar a chave privada em disco.
            return new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
        }
        ClassPathResource resource = new ClassPathResource(fallbackConfigFile);
        return resource.exists() ? resource.getInputStream() : null;
    }
}
