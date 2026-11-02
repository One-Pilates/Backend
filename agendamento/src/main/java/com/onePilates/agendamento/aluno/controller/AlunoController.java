package com.onePilates.agendamento.aluno.controller;

import com.onePilates.agendamento.aluno.dto.AlunoDTO;
import com.onePilates.agendamento.aluno.dto.AlunoPaginadoResponseDTO;
import com.onePilates.agendamento.aluno.dto.AlunoResponseDTO;
import com.onePilates.agendamento.aluno.dto.PacoteAulaDTO;
import com.onePilates.agendamento.aluno.dto.PacoteAulaResponseDTO;
import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.aluno.service.AlunoService;
import com.onePilates.agendamento.aluno.service.PacoteAulaService;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.model.Aluno;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService alunoService;
    private final PacoteAulaService pacoteAulaService;
    private final AlunoRepository alunoRepository;

    public AlunoController(AlunoService alunoService, PacoteAulaService pacoteAulaService, AlunoRepository alunoRepository) {
        this.alunoService = alunoService;
        this.pacoteAulaService = pacoteAulaService;
        this.alunoRepository = alunoRepository;
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

    @GetMapping("/{id}/pacotes")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA', 'ALUNO')")
    @Operation(summary = "Consulta o saldo de aulas do aluno. Aula dada (PRESENTE) diminui o saldo; aula cancelada não diminui.")
    @ApiResponse(responseCode = "200", description = "Saldos por especialidade")
    @ApiResponse(responseCode = "403", description = "Aluno consultando outro id")
    public ResponseEntity<List<PacoteAulaResponseDTO>> consultarPacotes(@PathVariable Long id, Authentication authentication) {
        if (eAluno(authentication)) {
            Aluno aluno = alunoDoToken(authentication);
            if (!aluno.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(pacoteAulaService.listar(id));
    }

    @PutMapping("/{alunoId}/pacotes/{especialidadeId}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    @Operation(summary = "Grava o saldo absoluto de aulas da especialidade. Não soma ao valor anterior. Zero é válido.")
    @ApiResponse(responseCode = "200", description = "Saldo gravado")
    @ApiResponse(responseCode = "403", description = "Aluno ou professor")
    public ResponseEntity<PacoteAulaResponseDTO> definirPacote(@PathVariable Long alunoId,
                                                               @PathVariable Long especialidadeId,
                                                               @RequestBody PacoteAulaDTO dto) {
        var pacote = pacoteAulaService.definir(alunoId, especialidadeId, dto.getAulasRemanescentes());
        PacoteAulaResponseDTO resposta = new PacoteAulaResponseDTO();
        resposta.setAlunoId(alunoId);
        resposta.setEspecialidadeId(especialidadeId);
        resposta.setAulasRemanescentes(pacote.getAulasRemanescentes());
        return ResponseEntity.ok(resposta);
    }

    private boolean eAluno(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(a -> "ALUNO".equals(a.getAuthority()));
    }

    private Aluno alunoDoToken(Authentication authentication) {
        return alunoRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Aluno não encontrado"));
    }
}