package com.onePilates.agendamento.administrador.controller;

import com.onePilates.agendamento.administrador.dto.AdministradorDTO;
import com.onePilates.agendamento.administrador.dto.AdministradorResponseDTO;
import com.onePilates.agendamento.administrador.service.AdministradorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/administradores")
@CrossOrigin(origins = "*")
public class AdministradorController {

    private final AdministradorService administradorService;

    public AdministradorController(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<AdministradorResponseDTO> criarAdministrador(@RequestBody AdministradorDTO dto) {
        return ResponseEntity.ok(administradorService.toResponseDTO(administradorService.criarAdministrador(dto)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<List<AdministradorResponseDTO>> listarAdministradores() {
        return ResponseEntity.ok(administradorService.listarTodosDTO());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<AdministradorResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(administradorService.buscarPorIdDTO(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<AdministradorResponseDTO> atualizarAdministradorParcial(@PathVariable Long id, @RequestBody AdministradorDTO dto) {
        return ResponseEntity.ok(administradorService.atualizarAdministrador(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<Void> excluirAdministrador(@PathVariable Long id) {
        administradorService.excluirAdministrador(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/uploadFoto")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<String> uploadFoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            String caminho = administradorService.salvarFoto(id, file);
            return ResponseEntity.ok(caminho);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao salvar foto: " + e.getMessage());
        }
    }
}