package com.onePilates.agendamento.aluno.controller;

import com.onePilates.agendamento.aluno.dto.AlunoDTO;
import com.onePilates.agendamento.aluno.dto.AlunoPaginadoResponseDTO;
import com.onePilates.agendamento.aluno.dto.AlunoResponseDTO;
import com.onePilates.agendamento.aluno.service.AlunoService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<AlunoResponseDTO> criarAluno(@RequestBody AlunoDTO dto) {
        return ResponseEntity.ok(alunoService.toResponseDTO(alunoService.criarAluno(dto)));
    }

    @GetMapping("/paginacao")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<AlunoPaginadoResponseDTO> listarAlunosPaginados(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(alunoService.listarTodosPaginadosDTO(pageable, nome, status));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<List<AlunoResponseDTO>> listarAlunos() {
        return ResponseEntity.ok(alunoService.listarTodosDTO());

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<AlunoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(alunoService.buscarPorIdDTO(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<AlunoResponseDTO> atualizarAlunoParcial(@PathVariable Long id, @RequestBody AlunoDTO dto) {
        return ResponseEntity.ok(alunoService.atualizarAluno(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<Void> excluirAluno(@PathVariable Long id) {
        alunoService.excluirAluno(id);
        return ResponseEntity.noContent().build();
    }
}