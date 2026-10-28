package com.onePilates.agendamento.agendamento.controller;

import com.onePilates.agendamento.agendamento.dto.AgendamentoDTO;
import com.onePilates.agendamento.agendamento.dto.AgendamentoResponseDTO;
import com.onePilates.agendamento.agendamento.service.AgendamentoService;
import com.onePilates.agendamento.aluno.dto.AtualizarObservacaoAlunoDTO;
import com.onePilates.agendamento.model.enums.StatusPresenca;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agendamentos")
@CrossOrigin(origins = "*")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    /**
     * Cria um novo agendamento.
     * 
     * @param dto DTO contendo os dados do agendamento
     * @return ResponseEntity com o agendamento criado e status 201 (Created)
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<AgendamentoResponseDTO> criarAgendamento( @Valid @RequestBody AgendamentoDTO dto) {
        return ResponseEntity.ok(agendamentoService.toResponseDTO(agendamentoService.criarAgendamento(dto)));
    }

    /**
     * Caminho do agendamento absoluto, que ignora todas as regras de agendamento
     */
    @PostMapping("/absoluto")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<AgendamentoResponseDTO> criarAgendamentoAbsoluto(@RequestBody AgendamentoDTO dto){
        return ResponseEntity.ok(agendamentoService.toResponseDTO(agendamentoService.criarAgendamentoAbsoluto(dto)));
    }
    /**
     * Lista todos os agendamentos cadastrados.
     * 
     * @return ResponseEntity com a lista de agendamentos e status 200 (OK)
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarAgendamentos() {
        return ResponseEntity.ok(agendamentoService.listarTodosDTO());
    }

    /**
     * Busca um agendamento específico pelo ID.
     * 
     * @param id ID do agendamento
     * @return ResponseEntity com o agendamento encontrado e status 200 (OK)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<AgendamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.buscarPorIdDTO(id));
    }

    @GetMapping("/{idSala}/{idProfessor}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<List<AgendamentoResponseDTO>>buscarAgendamentoPorSalaEProfessor(@PathVariable Long idSala, @PathVariable Long idProfessor) {
        return ResponseEntity.ok(agendamentoService.buscarAgendamentosPorIdsDeSalaEProfessor(idSala, idProfessor));
    }

    /**
     * Atualiza parcialmente um agendamento existente.
     * 
     * @param id ID do agendamento a ser atualizado
     * @param dto DTO contendo os campos a serem atualizados
     * @return ResponseEntity com o agendamento atualizado e status 200 (OK)
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<AgendamentoResponseDTO> atualizarAgendamentoParcial(@PathVariable Long id, @RequestBody AgendamentoDTO dto) {
        return ResponseEntity.ok(agendamentoService.atualizarAgendamento(id, dto));
    }

    /**
     * Exclui um agendamento do sistema.
     * 
     * @param id ID do agendamento a ser excluído
     * @return ResponseEntity com status 204 (No Content) se bem-sucedido
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA')")
    public ResponseEntity<Void> excluirAgendamento(@PathVariable Long id) {
        agendamentoService.excluirAgendamento(id);
        return ResponseEntity.noContent().build();
    }
    /**
     * Lista todos os agendamentos de um professor específico.
     * 
     * @param id ID do professor
     * @return ResponseEntity com a lista de agendamentos do professor e status 200 (OK)
     */
    @GetMapping("/professorId/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarPorProfessorId(@PathVariable Long id) {
       return  ResponseEntity.ok(agendamentoService.buscarAgendamentosPorIdProfessor(id));
    }


    @GetMapping("/sala/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<List<AgendamentoResponseDTO>> agendamentosPorSala(@PathVariable Long id) {
        return  ResponseEntity.ok(agendamentoService.buscarAgendamentosPorIdSala(id));
    }
    /**
     * Registra a presença dos alunos em um agendamento.
     * 
     * @param agendamentoId ID do agendamento
     * @param presencas Mapa com o ID do aluno como chave e o status de presença como valor
     * @return ResponseEntity com status 200 (OK) se bem-sucedido
     */
    @PatchMapping("/{agendamentoId}/presenca")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<Void> registrarPresenca(
            @PathVariable Long agendamentoId,
            @RequestBody Map<Long, StatusPresenca> presencas
    ) {
        agendamentoService.registrarPresencas(agendamentoId, presencas);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{agendamentoId}/alunos/{alunoId}/observacao")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SECRETARIA', 'PROFESSOR')")
    public ResponseEntity<Void> atualizarObservacaoAluno(
            @PathVariable Long agendamentoId,
            @PathVariable Long alunoId,
            @RequestBody AtualizarObservacaoAlunoDTO dto
    ) {
        agendamentoService.atualizarObservacaoAluno(agendamentoId, alunoId, dto.getObservacao());
        return ResponseEntity.ok().build();
    }

}
