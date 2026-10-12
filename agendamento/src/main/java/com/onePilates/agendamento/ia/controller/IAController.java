package com.onePilates.agendamento.ia.controller;

import com.onePilates.agendamento.ia.dto.IARequestDTO;
import com.onePilates.agendamento.ia.dto.IAResponseDTO;
import com.onePilates.agendamento.ia.service.IAService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/agente")
    public ResponseEntity<IAResponseDTO> agente(@Valid @RequestBody IARequestDTO request) {
        return ResponseEntity.ok(iaService.processar(request));
    }
}
