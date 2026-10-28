package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.agendamento.dto.AgendamentoDTO;
import com.onePilates.agendamento.agendamento.dto.AgendamentoResponseDTO;
import com.onePilates.agendamento.agendamento.service.AgendamentoService;
import com.onePilates.agendamento.model.enums.Role;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AtualizarAulaTool implements FerramentaIA {

    private final AgendamentoService agendamentoService;

    public AtualizarAulaTool(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @Override
    public String nome() {
        return "atualizar_aula";
    }

    @Override
    public String descricao() {
        return "Atualiza uma aula agendada. Informe apenas os campos que devem mudar: horário, professor, "
                + "sala, especialidade ou a lista completa de alunos. Só use quando o usuário pedir explicitamente "
                + "e depois de confirmar os ids com as tools de consulta. As regras de conflito são aplicadas.";
    }

    @Override
    public Set<Role> rolesPermitidas() {
        return Set.of(Role.ADMINISTRADOR, Role.SECRETARIA);
    }

    @Override
    public Map<String, Object> parametros() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "agendamentoId", Map.of("type", "integer"),
                        "dataHora", Map.of("type", "string", "description", "Novo horário AAAA-MM-DDTHH:MM"),
                        "professorId", Map.of("type", "integer"),
                        "salaId", Map.of("type", "integer"),
                        "especialidadeId", Map.of("type", "integer"),
                        "alunoIds", Map.of("type", "array", "items", Map.of("type", "integer"),
                                "description", "Lista completa de ids dos alunos da aula")),
                "required", List.of("agendamentoId"));
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        Long agendamentoId = ArgumentosTool.longObrigatorio(argumentos, "agendamentoId");
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

        if (argumentos.containsKey("alunoIds")) {
            try {
                dto.setAlunoIds(lerAlunoIds(argumentos.get("alunoIds")));
            } catch (IllegalArgumentException e) {
                return Map.of("erro", "alunoIds deve ser uma lista de números");
            }
        }

        if (dto.getDataHora() == null
                && dto.getProfessorId() == null
                && dto.getSalaId() == null
                && dto.getEspecialidadeId() == null
                && !argumentos.containsKey("alunoIds")) {
            return Map.of("erro", "Nenhuma alteração informada");
        }

        AgendamentoResponseDTO atualizado = agendamentoService.atualizarAgendamento(agendamentoId, dto);
        return Map.of(
                "sucesso", true,
                "agendamentoAtual", Map.of(
                        "id", atualizado.getId(),
                        "horario", String.valueOf(atualizado.getDataHora()),
                        "professor", ArgumentosTool.texto(atualizado.getProfessor()),
                        "sala", ArgumentosTool.texto(atualizado.getSala()),
                        "especialidade", ArgumentosTool.texto(atualizado.getEspecialidade()),
                        "alunos", atualizado.getAlunos() == null ? List.of() : atualizado.getAlunos()));
    }

    private Set<Long> lerAlunoIds(Object valor) {
        if (!(valor instanceof List<?> lista)) {
            throw new IllegalArgumentException();
        }

        Set<Long> alunoIds = new HashSet<>();
        for (Object item : lista) {
            if (item == null) {
                throw new IllegalArgumentException();
            }
            try {
                alunoIds.add(item instanceof Number numero
                        ? numero.longValue()
                        : Long.parseLong(String.valueOf(item).trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return alunoIds;
    }
}
