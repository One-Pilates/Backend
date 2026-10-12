package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.agendamento.repository.AgendamentoRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Component
public class ListarAgendamentosTool implements FerramentaIA {

    private final AgendamentoRepository agendamentoRepository;

    public ListarAgendamentosTool(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    @Override
    public String nome() { return "listar_agendamentos"; }

    @Override
    public String descricao() { return "Lista as aulas agendadas em um dia, com horário, professor, sala, especialidade e alunos."; }

    @Override
    public Map<String, Object> parametros() {
        return Map.of(
                "type", "object",
                "properties", Map.of("data", Map.of("type", "string", "description", "Data no formato AAAA-MM-DD")),
                "required", List.of("data"));
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        LocalDate data;
        try {
            data = LocalDate.parse(String.valueOf(argumentos.get("data")));
        } catch (DateTimeParseException e) {
            return Map.of("erro", "Data inválida, use AAAA-MM-DD");
        }
        List<Map<String, Object>> aulas = agendamentoRepository
                .buscarAgendamentosPorIntervalo(data.atStartOfDay(), data.plusDays(1).atStartOfDay()).stream()
                .map(a -> Map.<String, Object>of(
                        "id", a.getId(),
                        "horario", a.getDataHora().toString(),
                        "professor", a.getProfessor() != null ? a.getProfessor().getNome() : "-",
                        "sala", a.getSala() != null ? a.getSala().getNome() : "-",
                        "especialidade", a.getEspecialidade() != null ? a.getEspecialidade().getNome() : "-",
                        "alunos", a.getAgendamentoAlunos().stream()
                                .map(aa -> Map.<String, Object>of("id", aa.getAluno().getId(), "nome", aa.getAluno().getNome()))
                                .toList()))
                .toList();
        return Map.of("data", data.toString(), "agendamentos", aulas);
    }
}
