package com.onePilates.agendamento.ia.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Único ponto de contato com a API da Groq (formato OpenAI chat/completions e audio/transcriptions).
 * Quem precisar da Groq injeta este client em vez de montar a requisição HTTP.
 */
@Component
public class GroqClient {

    private static final Logger log = LoggerFactory.getLogger(GroqClient.class);
    private static final String URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String TRANSCRIPTION_URL = "https://api.groq.com/openai/v1/audio/transcriptions";
    private static final Set<String> EXTENSOES_AUDIO_SUPORTADAS = Set.of(
            "flac", "mp3", "mp4", "mpeg", "mpga", "m4a", "ogg", "wav", "webm"
    );
    private static final int MAX_TENTATIVAS_429 = 3;
    /** Soma máxima de espera por chamada à Groq (várias esperas curtas cabem, uma muito longa não). */
    private static final long ORCAMENTO_ESPERA_MS = 45_000;
    private static final Pattern ESPERA_PATTERN = Pattern.compile("try again in ([0-9.]+)(ms|s)");

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;

    @Value("${groq.whisper.model:whisper-large-v3-turbo}")
    private String whisperModel;

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

    /**
     * Transcreve um arquivo de áudio para texto em português utilizando o modelo Whisper na Groq.
     */
    public String transcreverAudio(byte[] audioBytes, String nomeOriginal, String contentType) {
        if (audioBytes == null || audioBytes.length == 0) {
            throw new IllegalArgumentException("O arquivo de áudio enviado está vazio.");
        }

        String nomeArquivo = normalizarNomeArquivoAudio(nomeOriginal, contentType);
        String mimeType = (contentType != null && !contentType.isBlank()) ? contentType : "audio/m4a";
        String boundary = "----OnePilatesGroqBoundary" + UUID.randomUUID().toString().replace("-", "");

        long inicioChamada = System.currentTimeMillis();
        log.info("[IA][groq][whisper] Transcrevendo áudio ({} bytes, arquivo={}, tipo={}, modelo={})",
                audioBytes.length, nomeArquivo, mimeType, whisperModel);

        try {
            byte[] multipartBody = construirMultipartAudio(boundary, audioBytes, nomeArquivo, mimeType);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TRANSCRIPTION_URL))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("Authorization", "Bearer " + groqApiKey.trim())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                    .timeout(Duration.ofSeconds(45))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            long esperado = 0;
            for (int tentativa = 1; tentativa <= MAX_TENTATIVAS_429 && response.statusCode() == 429; tentativa++) {
                long espera = esperaSugeridaMs(response.body());
                if (esperado + espera > ORCAMENTO_ESPERA_MS) {
                    break;
                }
                log.warn("[IA][groq][whisper] Limite 429 na transcrição. Aguardando {} ms (tentativa {}/{})",
                        espera, tentativa, MAX_TENTATIVAS_429);
                Thread.sleep(espera);
                esperado += espera;
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            }

            if (response.statusCode() != 200) {
                log.error("[IA][groq][whisper] Erro {} ao transcrever áudio: {}", response.statusCode(), response.body());
                throw new RuntimeException("Falha na transcrição de áudio (status " + response.statusCode() + ")");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
            Object textObj = responseMap.get("text");
            String texto = textObj == null ? "" : textObj.toString().trim();

            log.info("[IA][groq][whisper] Áudio transcrito em {} ms: \"{}\"",
                    System.currentTimeMillis() - inicioChamada, texto);
            return texto;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Falha ao transcrever áudio na IA: " + e.getMessage(), e);
        }
    }

    private String normalizarNomeArquivoAudio(String nomeOriginal, String contentType) {
        String base = (nomeOriginal == null || nomeOriginal.isBlank()) ? "audio.m4a" : nomeOriginal.trim();
        int idxPonto = base.lastIndexOf('.');
        if (idxPonto > 0 && idxPonto < base.length() - 1) {
            String ext = base.substring(idxPonto + 1).toLowerCase(Locale.ROOT);
            if (EXTENSOES_AUDIO_SUPORTADAS.contains(ext)) {
                return base;
            }
            base = base.substring(0, idxPonto);
        } else {
            base = "audio";
        }

        String ct = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (ct.contains("webm")) return base + ".webm";
        if (ct.contains("wav")) return base + ".wav";
        if (ct.contains("ogg")) return base + ".ogg";
        if (ct.contains("mp3") || ct.contains("mpeg")) return base + ".mp3";
        if (ct.contains("flac")) return base + ".flac";
        if (ct.contains("mp4")) return base + ".mp4";
        return base + ".m4a";
    }

    private byte[] construirMultipartAudio(String boundary, byte[] audioBytes, String nomeArquivo, String mimeType)
            throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(audioBytes.length + 512);
        String crlf = "\r\n";

        // Campo file
        out.write(("--" + boundary + crlf).getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + nomeArquivo + "\"" + crlf)
                .getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Type: " + mimeType + crlf + crlf).getBytes(StandardCharsets.UTF_8));
        out.write(audioBytes);
        out.write(crlf.getBytes(StandardCharsets.UTF_8));

        // Campo model
        escreverCampoForm(out, boundary, "model", whisperModel);
        // Campo language
        escreverCampoForm(out, boundary, "language", "pt");
        // Campo response_format
        escreverCampoForm(out, boundary, "response_format", "json");

        out.write(("--" + boundary + "--" + crlf).getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }

    private void escreverCampoForm(ByteArrayOutputStream out, String boundary, String nome, String valor)
            throws IOException {
        String crlf = "\r\n";
        out.write(("--" + boundary + crlf).getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"" + nome + "\"" + crlf + crlf)
                .getBytes(StandardCharsets.UTF_8));
        out.write((valor + crlf).getBytes(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("unchecked")
    private String usoDeTokens(Map<String, Object> responseMap) {
        Map<String, Object> usage = (Map<String, Object>) responseMap.get("usage");
        return "entrada=" + usage.get("prompt_tokens") + " saída=" + usage.get("completion_tokens")
                + " total=" + usage.get("total_tokens");
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
        long inicioChamada = System.currentTimeMillis();
        log.info("[IA][groq] Enviando {} mensagem(ns) ao modelo {}{}", messages.size(), model,
                tools == null || tools.isEmpty() ? "" : " com " + tools.size() + " ferramenta(s)");
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
            long esperado = 0;
            for (int tentativa = 1; tentativa <= MAX_TENTATIVAS_429 && response.statusCode() == 429; tentativa++) {
                long espera = esperaSugeridaMs(response.body());
                if (esperado + espera > ORCAMENTO_ESPERA_MS) {
                    break;
                }
                log.warn("[IA][groq] Limite de tokens por minuto (429). Aguardando {} ms antes da tentativa {}/{}",
                        espera, tentativa, MAX_TENTATIVAS_429);
                Thread.sleep(espera);
                esperado += espera;
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            }
            if (response.statusCode() == 429) {
                log.error("[IA][groq] Limite de tokens por minuto persistiu; desistindo (esperou {} ms no total)", esperado);
            }

            if (response.statusCode() != 200) {
                throw new RuntimeException("API da Groq retornou erro " + response.statusCode() + ": " + response.body());
            }

            Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
            log.info("[IA][groq] Resposta em {} ms (modelo {}, tokens: {})",
                    System.currentTimeMillis() - inicioChamada, model, responseMap.get("usage") == null ? "?" : usoDeTokens(responseMap));

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

            Object motivoFim = choices.get(0).get("finish_reason");
            if ("length".equals(motivoFim)) {
                log.warn("[IA][groq] Resposta cortada por limite de tokens de saída (finish_reason=length); "
                        + "modelos de raciocínio gastam parte do limite pensando");
            }

            return message;

        } catch (Exception e) {
            throw new RuntimeException("Falha ao obter resposta da Groq: " + e.getMessage(), e);
        }
    }
}
