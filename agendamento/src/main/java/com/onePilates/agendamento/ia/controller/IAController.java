package com.onePilates.agendamento.ia.controller;

import com.onePilates.agendamento.ia.dto.IARealizacaoDTO;
import com.onePilates.agendamento.ia.dto.IARequestDTO;
import com.onePilates.agendamento.ia.dto.IASolicitacaoDTO;
import com.onePilates.agendamento.ia.service.IAService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ia")
public class IAController {

    private final IAService iaService;

    public IAController(IAService iaService) {
        this.iaService = iaService;
    }

    /** Registra o pedido e devolve 202 na hora; o resultado vem de GET /api/ia/realizacoes/{id}. */
    @PostMapping("/agente")
    public ResponseEntity<IASolicitacaoDTO> agente(@Valid @RequestBody IARequestDTO request,
                                                   Authentication authentication) {
        // o JwtAuthFilter já validou o header Authorization; o principal é o e-mail do usuário
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(iaService.solicitar(request, authentication.getName()));
    }

    /** Andamento do pedido: status, passos já executados e, ao concluir, a resposta final. */
    @GetMapping("/realizacoes/{id}")
    public ResponseEntity<IARealizacaoDTO> consultar(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(iaService.consultar(id, authentication.getName()));
    }
}
