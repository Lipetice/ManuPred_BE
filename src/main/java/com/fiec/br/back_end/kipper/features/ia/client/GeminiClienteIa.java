package com.fiec.br.back_end.kipper.features.ia.client;

import com.fiec.br.back_end.kipper.features.ia.exception.IaIndisponivelException;
import com.fiec.br.back_end.kipper.features.ia.exception.LimiteIaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GeminiClienteIa implements ClienteIa {

    /** Quantas vezes tenta de novo quando o Google demora ou fica sobrecarregado. */
    private static final int TENTATIVAS_EXTRAS = 1;

    private final RestClient restClient;
    private final String apiKey;
    private final String modelo;

    public GeminiClienteIa(@Value("${app.ia.api-key}") String apiKey,
                           @Value("${app.ia.modelo}") String modelo,
                           @Value("${app.ia.timeout-segundos:40}") int timeoutSegundos) {
        this.apiKey = apiKey;
        this.modelo = modelo;

        // Timeout para a requisição não travar para sempre
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

        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelo + ":generateContent";

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", instrucaoSistema))),
                "contents", List.of(
                        Map.of("role", "user", "parts", List.of(Map.of("text", mensagemUsuario)))
                ),
                "generationConfig", Map.of("responseMimeType", "application/json", "temperature", 0.1)
        );

        // A primeira chamada às vezes falha por demora ou sobrecarga do Google (o erro 503 que
        // aparecia na primeira tentativa). Nesses casos tenta mais uma vez antes de desistir.
        for (int tentativa = 0; ; tentativa++) {
            try {
                return chamar(url, body);
            } catch (ResourceAccessException e) {
                // Timeout ou falha de conexão
                if (tentativa < TENTATIVAS_EXTRAS) {
                    log.warn("Gemini não respondeu a tempo ({}). Tentando de novo...", e.getMessage());
                    continue;
                }
                log.error("Gemini não respondeu: {}", e.getMessage());
                throw new IaIndisponivelException("A IA demorou para responder. Tente novamente.");
            } catch (RestClientResponseException e) {
                int status = e.getStatusCode().value();
                if (status >= 500 && tentativa < TENTATIVAS_EXTRAS) {
                    log.warn("Gemini respondeu {} (instabilidade). Tentando de novo...", status);
                    continue;
                }
                log.error("Erro ao chamar Gemini: {} {}", status, e.getResponseBodyAsString());
                if (status == 429) {
                    throw new LimiteIaException("O limite de uso da IA foi atingido. Aguarde um pouco e tente de novo.");
                }
                throw new IaIndisponivelException("Não foi possível obter resposta da IA.");
            } catch (IaIndisponivelException e) {
                throw e;
            } catch (Exception e) {
                log.error("Erro ao chamar Gemini: {}", e.getMessage());
                throw new IaIndisponivelException("Não foi possível obter resposta da IA.");
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String chamar(String url, Map<String, Object> body) {
        Map response = restClient.post()
                .uri(url)
                .header("x-goog-api-key", apiKey) // chave no header, não na URL
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IaIndisponivelException("A IA não retornou resposta. Tente novamente.");
        }

        var candidates = (List<Map>) response.get("candidates");
        // Sem candidatos: o Gemini bloqueou a resposta
        if (candidates == null || candidates.isEmpty()) {
            log.warn("Gemini não retornou candidatos. Resposta bloqueada ou inválida.");
            throw new IaIndisponivelException("A IA não conseguiu processar a solicitação. Tente novamente.");
        }

        var content = (Map) candidates.get(0).get("content");
        var parts = content != null ? (List<Map>) content.get("parts") : null;
        if (parts == null || parts.isEmpty() || parts.get(0).get("text") == null) {
            log.warn("Gemini retornou uma resposta sem texto.");
            throw new IaIndisponivelException("A IA não conseguiu processar a solicitação. Tente novamente.");
        }
        return (String) parts.get(0).get("text");
    }
}
