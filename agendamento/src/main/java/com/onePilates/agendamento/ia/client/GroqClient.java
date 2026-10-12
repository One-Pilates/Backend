package com.onePilates.agendamento.ia.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Único ponto de contato com a API da Groq (formato OpenAI chat/completions).
 * Quem precisar da Groq injeta este client em vez de montar a requisição HTTP.
 */
@Component
public class GroqClient {

    private static final String URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final int MAX_TENTATIVAS_429 = 2;
    private static final long ESPERA_MAXIMA_MS = 20_000;
    private static final Pattern ESPERA_PATTERN = Pattern.compile("try again in ([0-9.]+)(ms|s)");

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /** Chat simples: devolve só o texto da resposta. */
    public String chat(List<Map<String, String>> messages, double temperature, int maxTokens) {
        Map<String, Object> message = enviar(messages, null, temperature, maxTokens);
        if (!message.containsKey("content")) {
            throw new RuntimeException("Falha ao obter resposta da Groq: resposta não contém o conteúdo da mensagem");
        }
        return (String) message.get("content");
    }

    /**
     * Chat com tool calling: devolve a mensagem completa do assistente ({@code content} e/ou
     * {@code tool_calls}), que deve ser reenviada no histórico junto com o resultado das tools.
     */
    public Map<String, Object> chatComTools(List<Map<String, Object>> messages, List<Map<String, Object>> tools,
                                            double temperature, int maxTokens) {
        return enviar(messages, tools, temperature, maxTokens);
    }

    /** Lê "Please try again in 14.235s" (ou em ms) da mensagem de erro; se não achar, espera 5 s. */
    private long esperaSugeridaMs(String corpo) {
        Matcher m = ESPERA_PATTERN.matcher(corpo == null ? "" : corpo);
        if (!m.find()) {
            return 5_000;
        }
        double valor = Double.parseDouble(m.group(1));
        return (long) ("ms".equals(m.group(2)) ? valor : valor * 1000) + 500;
    }

    private Map<String, Object> enviar(List<? extends Map<String, ?>> messages, List<Map<String, Object>> tools,
                                       double temperature, int maxTokens) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("temperature", temperature);
            body.put("max_tokens", maxTokens);
            if (tools != null && !tools.isEmpty()) {
                body.put("tools", tools);
                body.put("tool_choice", "auto");
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + groqApiKey.trim())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Limite de tokens por minuto: espera o tempo que a Groq informa e tenta de novo
            for (int tentativa = 1; tentativa <= MAX_TENTATIVAS_429 && response.statusCode() == 429; tentativa++) {
                long espera = esperaSugeridaMs(response.body());
                if (espera > ESPERA_MAXIMA_MS) {
                    break;
                }
                Thread.sleep(espera);
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            }

            if (response.statusCode() != 200) {
                throw new RuntimeException("API da Groq retornou erro " + response.statusCode() + ": " + response.body());
            }

            Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");

            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("Resposta da Groq não contém 'choices'");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

            if (message == null) {
                throw new RuntimeException("Resposta da Groq não contém a mensagem");
            }

            return message;

        } catch (Exception e) {
            throw new RuntimeException("Falha ao obter resposta da Groq: " + e.getMessage(), e);
        }
    }
}
