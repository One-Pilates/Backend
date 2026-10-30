package com.onePilates.agendamento.ia.controller;

import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.exception.OperacaoInvalidaException;
import com.onePilates.agendamento.ia.dto.IARealizacaoDTO;
import com.onePilates.agendamento.ia.dto.IARequestDTO;
import com.onePilates.agendamento.ia.dto.IASolicitacaoDTO;
import com.onePilates.agendamento.ia.service.IAService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/ia")
public class IAController {

    private final IAService iaService;

    public IAController(IAService iaService) {
        this.iaService = iaService;
    }

    /** Registra o pedido em texto e devolve 202 na hora; o resultado vem de GET /api/ia/realizacoes/{id}. */
    @PostMapping("/agente")
    public ResponseEntity<IASolicitacaoDTO> agente(@Valid @RequestBody IARequestDTO request,
                                                   Authentication authentication) {
        // o JwtAuthFilter já validou o header Authorization; o principal é o e-mail do usuário
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(iaService.solicitar(request, authentication.getName()));
    }

    /**
     * Recebe um áudio gravado ou enviado pelo usuário, realiza a transcrição no backend e encaminha
     * diretamente para o modelo de IA, retornando 202 com realizacaoId, status e textoTranscrito.
     */
    @PostMapping(value = "/agente/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IASolicitacaoDTO> agentePorAudio(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam("usuarioId") Long usuarioId,
            @RequestParam(value = "novaConversa", required = false, defaultValue = "false") Boolean novaConversa,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(iaService.solicitarPorAudio(usuarioId, audio, novaConversa, authentication.getName()));
    }

    /** Endpoint auxiliar de transcrição direta de áudio para texto. */
    @PostMapping(value = "/transcrever", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> transcrever(@RequestParam("audio") MultipartFile audio) {
        String texto = iaService.transcreverAudio(audio);
        return ResponseEntity.ok(Map.of("texto", texto));
    }

    /** Andamento do pedido: status, passos já executados e, ao concluir, a resposta final. */
    @GetMapping("/realizacoes/{id}")
    public ResponseEntity<IARealizacaoDTO> consultar(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(iaService.consultar(id, authentication.getName()));
    }

    @ExceptionHandler({IllegalArgumentException.class, OperacaoInvalidaException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Solicitação inválida para o assistente de IA.";
        return ResponseEntity.badRequest().body(Map.of("erro", msg, "message", msg));
    }

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntidadeNaoEncontradaException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Recurso não encontrado.";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", msg, "message", msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralError(Exception ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Erro ao processar solicitação na IA.";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("erro", msg, "message", msg));
    }
}
