package com.onePilates.agendamento.sala.controller;

import com.onePilates.agendamento.sala.dto.SalaDTO;
import com.onePilates.agendamento.sala.dto.SalaResponseDTO;
import com.onePilates.agendamento.sala.service.SalaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salas")
public class SalaController {

    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<SalaResponseDTO> criarSala(@RequestBody SalaDTO dto) {
        SalaResponseDTO response = salaService.toResponseDTO(salaService.criarSala(dto));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<List<SalaResponseDTO>> listarSalas() {
        return ResponseEntity.ok(salaService.listarTodasDTO());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<SalaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(salaService.buscarPorIdDTO(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<SalaResponseDTO> atualizarSalaParcial(@PathVariable Long id, @RequestBody SalaDTO dto) {
        return ResponseEntity.ok(salaService.atualizarSala(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<Void> excluirSala(@PathVariable Long id) {
        salaService.excluirSala(id);
        return ResponseEntity.noContent().build();
    }


}