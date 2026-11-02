package com.onePilates.agendamento.aluno.controller;

import com.onePilates.agendamento.aluno.dto.AlunoResponseDTO;
import com.onePilates.agendamento.aluno.dto.FeedbackDTO;
import com.onePilates.agendamento.aluno.dto.PacoteAulaResponseDTO;
import com.onePilates.agendamento.aluno.dto.PerfilAlunoDTO;
import com.onePilates.agendamento.aluno.dto.RespostaDashAlunoDTO;
import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.aluno.service.PacoteAulaService;
import com.onePilates.agendamento.aluno.service.PortalAlunoService;
import com.onePilates.agendamento.exception.BusinessException;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.exception.FeedbackJaExistenteException;
import com.onePilates.agendamento.model.Aluno;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/aluno")
@Tag(name = "Portal do aluno")
public class AlunoPortalController {

    private final PacoteAulaService pacoteAulaService;
    private final PortalAlunoService portalAlunoService;
    private final AlunoRepository alunoRepository;

    public AlunoPortalController(PacoteAulaService pacoteAulaService, PortalAlunoService portalAlunoService,
                                 AlunoRepository alunoRepository) {
        this.pacoteAulaService = pacoteAulaService;
        this.portalAlunoService = portalAlunoService;
        this.alunoRepository = alunoRepository;
    }

    @GetMapping("/pacotes")
    @PreAuthorize("hasAuthority('ALUNO')")
    @Operation(summary = "Saldo de aulas do aluno autenticado. Aula dada (PRESENTE) diminui o saldo; aula cancelada não diminui.")
    @ApiResponse(responseCode = "200", description = "Saldos do próprio aluno")
    public ResponseEntity<List<PacoteAulaResponseDTO>> meusPacotes(Authentication authentication) {
        Aluno aluno = alunoRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Aluno não encontrado"));
        return ResponseEntity.ok(pacoteAulaService.listar(aluno.getId()));
    }

    @GetMapping("/agenda")
    @PreAuthorize("hasAuthority('ALUNO')")
    @Operation(summary = "Aulas futuras em que o aluno ainda está")
    @ApiResponse(responseCode = "200", description = "Agenda do token")
    public ResponseEntity<?> agenda(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
                                    Authentication authentication) {
        return ResponseEntity.ok(portalAlunoService.listarAgenda(alunoId(authentication), de, ate));
    }

    @PatchMapping("/agenda/{agendamentoId}/presenca")
    @PreAuthorize("hasAuthority('ALUNO')")
    @Operation(summary = "Confirma a intenção de comparecer a uma aula futura")
    @ApiResponse(responseCode = "200", description = "Presença confirmada")
    @ApiResponse(responseCode = "400", description = "AULA_JA_REALIZADA")
    @ApiResponse(responseCode = "404", description = "Aula não é do aluno")
    public ResponseEntity<Void> confirmarPresenca(@PathVariable Long agendamentoId, Authentication authentication) {
        portalAlunoService.confirmarPresenca(alunoId(authentication), agendamentoId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/agenda/{agendamentoId}")
    @PreAuthorize("hasAuthority('ALUNO')")
    @Operation(summary = "Cancela a participação do aluno com no mínimo 24 horas de antecedência")
    @ApiResponse(responseCode = "204", description = "Participação cancelada")
    @ApiResponse(responseCode = "400", description = "CANCELAMENTO_FORA_DO_PRAZO")
    public ResponseEntity<Void> cancelar(@PathVariable Long agendamentoId, Authentication authentication) {
        portalAlunoService.cancelar(alunoId(authentication), agendamentoId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/agenda/{agendamentoId}/feedback")
    @PreAuthorize("hasAuthority('ALUNO')")
    @Operation(summary = "Registra o feedback de uma aula já iniciada")
    @ApiResponse(responseCode = "201", description = "Feedback criado")
    @ApiResponse(responseCode = "409", description = "Já existe feedback desta aula")
    public ResponseEntity<Void> criarFeedback(@PathVariable Long agendamentoId, @RequestBody FeedbackDTO dto,
                                              Authentication authentication) {
        portalAlunoService.criarFeedback(alunoId(authentication), agendamentoId, dto.getNota(), dto.getComentario());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/agenda/{agendamentoId}/feedback")
    @PreAuthorize("hasAuthority('ALUNO')")
    @Operation(summary = "Atualiza o próprio feedback")
    public ResponseEntity<Void> atualizarFeedback(@PathVariable Long agendamentoId, @RequestBody FeedbackDTO dto,
                                                  Authentication authentication) {
        portalAlunoService.atualizarFeedback(alunoId(authentication), agendamentoId, dto.getNota(), dto.getComentario());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/perfil")
    @PreAuthorize("hasAuthority('ALUNO')")
    @Operation(summary = "Perfil do aluno autenticado")
    public ResponseEntity<AlunoResponseDTO> perfil(Authentication authentication) {
        return ResponseEntity.ok(portalAlunoService.obterPerfil(alunoId(authentication)));
    }

    @PatchMapping("/perfil")
    @PreAuthorize("hasAuthority('ALUNO')")
    @Operation(summary = "Atualiza somente notificação e tipo de contato")
    public ResponseEntity<Void> atualizarPerfil(@RequestBody PerfilAlunoDTO dto, Authentication authentication) {
        portalAlunoService.atualizarPerfil(alunoId(authentication), dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/perfil/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ALUNO')")
    @Operation(summary = "Envia a foto do aluno autenticado")
    public ResponseEntity<String> uploadFoto(@RequestParam("foto") MultipartFile foto, Authentication authentication) {
        return ResponseEntity.ok(portalAlunoService.uploadFoto(alunoId(authentication), foto));
    }

    @GetMapping("/dashboard/{qtdUltimosDias}")
    @PreAuthorize("hasAuthority('ALUNO')")
    @Operation(summary = "Histórico, KPIs e gráficos do aluno. Período de 1 a 365 dias")
    public ResponseEntity<RespostaDashAlunoDTO> dashboard(@PathVariable int qtdUltimosDias, Authentication authentication) {
        return ResponseEntity.ok(portalAlunoService.dashboard(alunoId(authentication), qtdUltimosDias));
    }

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(EntidadeNaoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", ex.getMessage(), "codigoErro", ex.getCodigoErro()));
    }

    @ExceptionHandler(FeedbackJaExistenteException.class)
    public ResponseEntity<Map<String, String>> conflito(FeedbackJaExistenteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", ex.getMessage(), "codigoErro", ex.getCodigoErro()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, String>> negocio(BusinessException ex) {
        return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage(), "codigoErro", ex.getCodigoErro()));
    }

    private Long alunoId(Authentication authentication) {
        Aluno aluno = alunoRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Aluno não encontrado"));
        return aluno.getId();
    }
}
