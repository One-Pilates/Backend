package com.onePilates.agendamento.ausencia.controller;

import com.onePilates.agendamento.ausencia.dto.AusenciaDTO;
import com.onePilates.agendamento.ausencia.dto.AusenciaResponseDTO;
import com.onePilates.agendamento.ausencia.service.AusenciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ausencias")
@CrossOrigin(origins = "*")
public class AusenciaController {

    private final AusenciaService ausenciaService;

    public AusenciaController(AusenciaService ausenciaService) {
        this.ausenciaService = ausenciaService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<AusenciaResponseDTO> registrar( @Valid @RequestBody AusenciaDTO dto) {
        AusenciaResponseDTO response = ausenciaService.registrarAusencia(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/professor/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<List<AusenciaResponseDTO>> listarPorProfessor(@PathVariable Long id) {
        return ResponseEntity.ok(ausenciaService.listarPorProfessor(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<AusenciaResponseDTO> atualizarAusenciaParcial(@PathVariable Integer id, @RequestBody AusenciaDTO dto) {
        return ResponseEntity.ok(ausenciaService.atualizarAusencia(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<Map<String, String>> deletar(@PathVariable Integer id) {
        ausenciaService.deletarAusencia(id);

        Map<String, String> resposta = new HashMap<>();
        resposta.put("mensagem", "Ausência apagada com sucesso.");

        return ResponseEntity.ok(resposta);
    }

}
