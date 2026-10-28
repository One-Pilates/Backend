package com.onePilates.agendamento.secretaria.controller;

import com.onePilates.agendamento.dashboard.dto.ResponsDashSecretariaAdmDTO;
import com.onePilates.agendamento.secretaria.dto.SecretariaDTO;
import com.onePilates.agendamento.secretaria.dto.SecretariaResponseDTO;
import com.onePilates.agendamento.secretaria.service.SecretariaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/secretarias")
@CrossOrigin(origins = "*")
public class SecretariaController {

    private final SecretariaService secretariaService;

    public SecretariaController(SecretariaService secretariaService) {
        this.secretariaService = secretariaService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<SecretariaResponseDTO> criarSecretaria( @Valid @RequestBody SecretariaDTO dto) {
        return ResponseEntity.ok(secretariaService.toResponseDTO(secretariaService.criarSecretaria(dto)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<List<SecretariaResponseDTO>> listarSecretarias() {
        return ResponseEntity.ok(secretariaService.listarTodosDTO());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<SecretariaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(secretariaService.buscarPorIdDTO(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<SecretariaResponseDTO> atualizarSecretariaParcial(@PathVariable Long id, @RequestBody SecretariaDTO dto) {
        return ResponseEntity.ok(secretariaService.atualizarSecretaria(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<Void> excluirSecretaria(@PathVariable Long id) {
        secretariaService.excluirSecretaria(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/uploadFoto")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<String> uploadFoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            String caminho = secretariaService.salvarFoto(id, file);
            return ResponseEntity.ok( caminho);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao salvar foto: " + e.getMessage());
        }
    }

    @GetMapping("qtdUltimosDias/{qtdUltimosDias}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<ResponsDashSecretariaAdmDTO> buscarPorIdDados(
            @PathVariable Integer qtdUltimosDias) {


        if (qtdUltimosDias == null || qtdUltimosDias < 1 || qtdUltimosDias > 365) {
            throw new IllegalArgumentException("Período deve estar entre 1 e 365 dias");
        }

        return ResponseEntity.ok(secretariaService.respostaDashSecretariaAdm( qtdUltimosDias));
    }
}
