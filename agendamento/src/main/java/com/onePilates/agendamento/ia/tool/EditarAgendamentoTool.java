package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.agendamento.dto.AgendamentoDTO;
import com.onePilates.agendamento.agendamento.dto.AgendamentoResponseDTO;
import com.onePilates.agendamento.agendamento.service.AgendamentoService;
import com.onePilates.agendamento.model.enums.Role;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class EditarAgendamentoTool implements FerramentaIA {

    private final AgendamentoService agendamentoService;

    public EditarAgendamentoTool(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @Override
    public String nome() { return "editar_agendamento"; }

    @Override
    public String descricao() {
        return "Edita uma aula agendada: muda horário, professor, sala ou especialidade. Informe só o que muda. "
                + "Só use quando o usuário pedir explicitamente e depois de confirmar os ids com as tools de consulta. "
                + "As regras de conflito do sistema são aplicadas e podem recusar a mudança.";
    }

    /** Mesma permissão do PATCH /api/agendamentos/{id}. */
    @Override
    public Set<Role> rolesPermitidas() { return Set.of(Role.ADMINISTRADOR, Role.SECRETARIA); }

    @Override
    public Map<String, Object> parametros() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "agendamentoId", Map.of("type", "integer"),
                        "dataHora", Map.of("type", "string", "description", "Novo horário AAAA-MM-DDTHH:MM"),
                        "professorId", Map.of("type", "integer"),
                        "salaId", Map.of("type", "integer"),
                        "especialidadeId", Map.of("type", "integer")),
                "required", List.of("agendamentoId"));
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        Long id = ArgumentosTool.longObrigatorio(argumentos, "agendamentoId");
        AgendamentoDTO dto = new AgendamentoDTO();
        try {
            if (argumentos.get("dataHora") != null) {
                dto.setDataHora(LocalDateTime.parse(String.valueOf(argumentos.get("dataHora"))));
            }
        } catch (DateTimeParseException e) {
            return Map.of("erro", "dataHora inválida, use AAAA-MM-DDTHH:MM");
        }
        dto.setProfessorId(ArgumentosTool.longOpcional(argumentos, "professorId"));
        dto.setSalaId(ArgumentosTool.longOpcional(argumentos, "salaId"));
        dto.setEspecialidadeId(ArgumentosTool.longOpcional(argumentos, "especialidadeId"));
        if (dto.getDataHora() == null && dto.getProfessorId() == null
                && dto.getSalaId() == null && dto.getEspecialidadeId() == null) {
            return Map.of("erro", "Nenhuma alteração informada");
        }

        AgendamentoResponseDTO depois = agendamentoService.atualizarAgendamento(id, dto);
        return Map.of("sucesso", true, "agendamentoAtual", Map.of(
                "id", depois.getId(),
                "horario", String.valueOf(depois.getDataHora()),
                "professor", String.valueOf(depois.getProfessor()),
                "sala", String.valueOf(depois.getSala()),
                "especialidade", String.valueOf(depois.getEspecialidade())));
    }
}
