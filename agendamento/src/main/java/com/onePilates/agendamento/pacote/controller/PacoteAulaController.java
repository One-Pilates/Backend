package com.onePilates.agendamento.pacote.controller;

import com.onePilates.agendamento.pacote.dto.AjustePacoteDTO;
import com.onePilates.agendamento.pacote.dto.MovimentoPacoteResponseDTO;
import com.onePilates.agendamento.pacote.dto.PacoteResponseDTO;
import com.onePilates.agendamento.pacote.dto.VerificacaoPacoteDTO;
import com.onePilates.agendamento.pacote.service.PacoteAulaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Pacote de aulas: somente secretaria e administrador consultam e alteram. */
@RestController
@RequestMapping("/api")
public class PacoteAulaController {

    private final PacoteAulaService pacoteService;

    public PacoteAulaController(PacoteAulaService pacoteService) {
        this.pacoteService = pacoteService;
    }

    @GetMapping("/alunos/{alunoId}/pacotes")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<List<PacoteResponseDTO>> listar(@PathVariable Long alunoId) {
        return ResponseEntity.ok(pacoteService.listar(alunoId));
    }

    @PutMapping("/alunos/{alunoId}/pacotes/{especialidadeId}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<PacoteResponseDTO> definirSaldo(@PathVariable Long alunoId,
                                                          @PathVariable Long especialidadeId,
                                                          @RequestBody AjustePacoteDTO dto) {
        return ResponseEntity.ok(pacoteService.definirSaldo(alunoId, especialidadeId, dto.aulas()));
    }

    @GetMapping("/alunos/{alunoId}/pacotes/historico")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<List<MovimentoPacoteResponseDTO>> historico(@PathVariable Long alunoId) {
        return ResponseEntity.ok(pacoteService.historico(alunoId));
    }

    /** Usado na tela de agendamento: avisa quem ficaria além do pacote antes de salvar a aula. */
    @GetMapping("/agendamentos/verificar-pacote")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<List<VerificacaoPacoteDTO>> verificar(@RequestParam List<Long> alunoIds,
                                                                @RequestParam Long especialidadeId) {
        return ResponseEntity.ok(pacoteService.verificar(alunoIds, especialidadeId));
    }
}
