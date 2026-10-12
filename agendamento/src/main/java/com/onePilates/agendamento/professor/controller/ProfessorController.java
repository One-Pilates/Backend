package com.onePilates.agendamento.professor.controller;

import com.onePilates.agendamento.dashboard.dto.RespostaDashProfessoraDTO;
import com.onePilates.agendamento.professor.dto.ProfessorDTO;
import com.onePilates.agendamento.professor.dto.ProfessorPaginadoResponseDTO;
import com.onePilates.agendamento.professor.dto.ProfessorResponseDTO;
import com.onePilates.agendamento.professor.service.ProfessorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/professores")
@CrossOrigin(origins = "*")
public class ProfessorController {

    private final ProfessorService professorService;

    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<ProfessorResponseDTO> criarProfessor(@Valid @RequestBody ProfessorDTO dto) {
        ProfessorResponseDTO response = professorService.criarProfessor(dto);
        URI location = URI.create(String.format("/api/professores/%d", response.getId()));
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/paginacao")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<ProfessorPaginadoResponseDTO> listarProfessoresPaginados(
            @RequestParam(required = false) String nome,
            Pageable pageable
    ) {
        return ResponseEntity.ok(professorService.listarTodosPaginadosDTO(pageable, nome));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<List<ProfessorResponseDTO>> listarProfessores() {
        return ResponseEntity.ok(professorService.listarTodosDTO());

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<ProfessorResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(professorService.buscarPorIdDTO(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<ProfessorResponseDTO> atualizarProfessorParcial(@PathVariable Long id,
                                                                          @RequestBody ProfessorDTO dto) {
        ProfessorResponseDTO updated = professorService.atualizarProfessor(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRADOR')")
    public ResponseEntity<Void> excluirProfessor(@PathVariable Long id) {
        professorService.excluirProfessor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/{qtdUltimosDias}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'PROFESSOR')")
    public ResponseEntity<RespostaDashProfessoraDTO> buscarPorIdDados(
            @PathVariable Long id,
            @PathVariable Integer qtdUltimosDias) {
        
        // Validação de parâmetros
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID do professor deve ser um valor positivo");
        }
        if (qtdUltimosDias == null || qtdUltimosDias < 1 || qtdUltimosDias > 365) {
            throw new IllegalArgumentException("Período deve estar entre 1 e 365 dias");
        }
        
        return ResponseEntity.ok(professorService.respostaDashProfessora(id, qtdUltimosDias));
    }

    @PostMapping("/{id}/uploadFoto")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'PROFESSOR')")
    public ResponseEntity<?> uploadFoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("Arquivo não pode ser vazio");
            }
            String caminho = professorService.salvarFoto(id, file);
            return ResponseEntity.ok( caminho);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao salvar foto: " + e.getMessage());
        }
    }





}