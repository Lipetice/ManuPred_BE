package com.fiec.br.back_end.kipper.features.ia.client;

import com.fiec.br.back_end.kipper.features.ia.exception.IaIndisponivelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
public class GeminiClienteIa implements ClienteIa {

    private final RestClient restClient;
    private final String apiKey;
    private final String modelo;

    public GeminiClienteIa(@Value("${app.ia.api-key}") String apiKey,
                           @Value("${app.ia.modelo}") String modelo,
                           @Value("${app.ia.timeout-segundos:20}") int timeoutSegundos) {
        this.apiKey = apiKey;
        this.modelo = modelo;

        // Configurando o Timeout para evitar que a requisição trave para sempre
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutSegundos * 1000);
        factory.setReadTimeout(timeoutSegundos * 1000);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public String gerarJson(String instrucaoSistema, String mensagemUsuario) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IaIndisponivelException("IA indisponível no momento.");
        }

        // CORREÇÃO: Chave retirada da URL
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelo + ":generateContent";

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of("parts", new Object[]{Map.of("text", instrucaoSistema)}),
                "contents", new Object[]{
                        Map.of("role", "user", "parts", new Object[]{Map.of("text", mensagemUsuario)})
                },
                "generationConfig", Map.of("responseMimeType", "application/json", "temperature", 0.1)
        );

        try {
            Map response = restClient.post()
                    .uri(url)
                    .header("x-goog-api-key", apiKey) // Chave no Header (Seguro)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            var candidates = (java.util.List<Map>) response.get("candidates");

            // Verifica se o Gemini bloqueou a resposta
            if (candidates == null || candidates.isEmpty()) {
                log.warn("Gemini não retornou candidatos. Resposta bloqueada ou inválida.");
                throw new IaIndisponivelException("A IA não conseguiu processar a solicitação. Tente novamente.");
            }

            var content = (Map) candidates.get(0).get("content");
            var parts = (java.util.List<Map>) content.get("parts");
            return (String) parts.get(0).get("text");

        } catch (IaIndisponivelException e) {
            throw e; // Repassa o erro amigável
        } catch (Exception e) {
            log.error("Erro ao chamar Gemini: {}", e.getMessage());
            throw new IaIndisponivelException("Não foi possível obter resposta da IA.");
        }
    }
}