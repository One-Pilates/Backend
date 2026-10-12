package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.agendamento.dto.AgendamentoDTO;
import com.onePilates.agendamento.agendamento.service.AgendamentoService;
import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.enums.Role;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class CriarAgendamentoTool implements FerramentaIA {

    private final AgendamentoService agendamentoService;

    public CriarAgendamentoTool(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @Override
    public String nome() { return "criar_agendamento"; }

    @Override
    public String descricao() {
        return "Marca (agenda) uma aula no calendário com professor, sala, especialidade, horário e alunos. "
                + "NÃO use para plano de aula, sugestão de exercícios ou montar o conteúdo de uma aula. "
                + "Só use quando o usuário pedir explicitamente. Descubra antes todos os ids com as tools de consulta "
                + "(buscar_alunos, listar_professores_e_salas, listar_especialidades). A sala e o professor devem atender "
                + "a especialidade. Se o sistema recusar por conflito ou regra, informe o motivo e, se o usuário "
                + "pediu 'uma sala disponível', tente outra sala compatível.";
    }

    /** Mesma permissão do POST /api/agendamentos. */
    @Override
    public Set<Role> rolesPermitidas() { return Set.of(Role.ADMINISTRADOR, Role.SECRETARIA); }

    @Override
    public Map<String, Object> parametros() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "dataHora", Map.of("type", "string", "description", "Horário AAAA-MM-DDTHH:MM"),
                        "professorId", Map.of("type", "integer"),
                        "salaId", Map.of("type", "integer"),
                        "especialidadeId", Map.of("type", "integer"),
                        "alunoIds", Map.of("type", "array", "items", Map.of("type", "integer"))),
                "required", List.of("dataHora", "professorId", "salaId", "especialidadeId", "alunoIds"));
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        AgendamentoDTO dto = new AgendamentoDTO();
        try {
            dto.setDataHora(LocalDateTime.parse(String.valueOf(argumentos.get("dataHora"))));
        } catch (DateTimeParseException e) {
            return Map.of("erro", "dataHora inválida, use AAAA-MM-DDTHH:MM");
        }
        dto.setProfessorId(ArgumentosTool.longObrigatorio(argumentos, "professorId"));
        dto.setSalaId(ArgumentosTool.longObrigatorio(argumentos, "salaId"));
        dto.setEspecialidadeId(ArgumentosTool.longObrigatorio(argumentos, "especialidadeId"));

        Set<Long> alunoIds = new HashSet<>();
        if (argumentos.get("alunoIds") instanceof List<?> lista) {
            for (Object item : lista) {
                alunoIds.add(item instanceof Number n ? n.longValue() : Long.parseLong(String.valueOf(item).trim()));
            }
        }
        if (alunoIds.isEmpty()) {
            return Map.of("erro", "Informe ao menos um aluno");
        }
        dto.setAlunoIds(alunoIds);

        Agendamento criado = agendamentoService.criarAgendamento(dto);
        return Map.of(
                "sucesso", true,
                "agendamentoId", criado.getId(),
                "horario", criado.getDataHora().toString(),
                "professor", criado.getProfessor().getNome(),
                "sala", criado.getSala().getNome(),
                "especialidade", criado.getEspecialidade().getNome(),
                "alunos", criado.getAgendamentoAlunos().stream().map(aa -> aa.getAluno().getNome()).toList());
    }
}
