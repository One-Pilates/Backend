package com.onePilates.agendamento.aluno.portal.controller;

import com.onePilates.agendamento.aluno.portal.dto.AtualizarPerfilAlunoDTO;
import com.onePilates.agendamento.aluno.portal.dto.AulaAlunoDTO;
import com.onePilates.agendamento.aluno.portal.dto.CancelamentoResultadoDTO;
import com.onePilates.agendamento.aluno.portal.dto.DashboardAlunoDTO;
import com.onePilates.agendamento.aluno.portal.dto.PerfilAlunoDTO;
import com.onePilates.agendamento.aluno.portal.service.PortalAlunoService;
import com.onePilates.agendamento.pacote.dto.PacoteResponseDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Portal do aluno. O aluno é identificado pelo e-mail do JWT; nenhuma rota recebe id de aluno. */
@RestController
@RequestMapping("/api/aluno")
@PreAuthorize("hasAuthority('ALUNO')")
public class AlunoPortalController {

    private final PortalAlunoService portalService;

    public AlunoPortalController(PortalAlunoService portalService) {
        this.portalService = portalService;
    }

    /** Aulas no intervalo (futuras, realizadas e canceladas). Sem datas: 30 dias para trás e para frente. */
    @GetMapping("/aulas")
    public ResponseEntity<List<AulaAlunoDTO>> aulas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            Authentication authentication) {
        return ResponseEntity.ok(portalService.aulas(authentication.getName(), de, ate));
    }

    @GetMapping("/aulas/{agendamentoId}")
    public ResponseEntity<AulaAlunoDTO> detalhe(@PathVariable Long agendamentoId, Authentication authentication) {
        return ResponseEntity.ok(portalService.detalhe(authentication.getName(), agendamentoId));
    }

    @PatchMapping("/aulas/{agendamentoId}/presenca")
    public ResponseEntity<Void> confirmarPresenca(@PathVariable Long agendamentoId, Authentication authentication) {
        portalService.confirmarPresenca(authentication.getName(), agendamentoId);
        return ResponseEntity.noContent().build();
    }

    /** Cancela a aula: 24h ou mais devolve ao pacote; menos de 24h continua descontada. */
    @DeleteMapping("/aulas/{agendamentoId}")
    public ResponseEntity<CancelamentoResultadoDTO> cancelar(@PathVariable Long agendamentoId, Authentication authentication) {
        return ResponseEntity.ok(portalService.cancelar(authentication.getName(), agendamentoId));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardAlunoDTO> dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            Authentication authentication) {
        return ResponseEntity.ok(portalService.dashboard(authentication.getName(), de, ate));
    }

    @GetMapping("/pacotes")
    public ResponseEntity<List<PacoteResponseDTO>> pacotes(Authentication authentication) {
        return ResponseEntity.ok(portalService.pacotes(authentication.getName()));
    }

    @GetMapping("/perfil")
    public ResponseEntity<PerfilAlunoDTO> perfil(Authentication authentication) {
        return ResponseEntity.ok(portalService.perfil(authentication.getName()));
    }

    /** O aluno só altera telefone e notificações; nome, e-mail e status são da secretaria. */
    @PatchMapping("/perfil")
    public ResponseEntity<PerfilAlunoDTO> atualizarPerfil(@RequestBody AtualizarPerfilAlunoDTO dto,
                                                          Authentication authentication) {
        return ResponseEntity.ok(portalService.atualizarPerfil(authentication.getName(), dto));
    }
}
