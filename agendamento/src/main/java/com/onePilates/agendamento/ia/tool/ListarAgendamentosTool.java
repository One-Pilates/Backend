package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.agendamento.repository.AgendamentoRepository;
import com.onePilates.agendamento.model.Agendamento;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class ListarAgendamentosTool implements FerramentaIA {

    private static final int MAX_DIAS = 31;
    private static final int MAX_AULAS = 40;

    private final AgendamentoRepository agendamentoRepository;

    public ListarAgendamentosTool(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    @Override
    public String nome() { return "listar_agendamentos"; }

    @Override
    public String descricao() {
        return "Lista as aulas agendadas em um dia ou em um período (máximo 31 dias), em ordem de data, com id, horário, "
                + "professor, sala, especialidade e alunos (id e nome). Para um dia use dataInicio igual a dataFim. "
                + "Aceita filtrar por professorId e/ou alunoId. Para 'semana', 'amanhã', 'mês' etc., calcule as datas "
                + "e chame direto, sem pedir ao usuário.";
    }

    @Override
    public Map<String, Object> parametros() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "dataInicio", Map.of("type", "string", "description", "AAAA-MM-DD"),
                        "dataFim", Map.of("type", "string", "description", "AAAA-MM-DD (inclusive); se omitida, só o dia de dataInicio"),
                        "professorId", Map.of("type", "integer"),
                        "alunoId", Map.of("type", "integer")),
                "required", List.of("dataInicio"));
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        LocalDate inicio;
        LocalDate fim;
        try {
            // "data" é aceita como apelido antigo de dataInicio
            Object bruto = argumentos.get("dataInicio") != null ? argumentos.get("dataInicio") : argumentos.get("data");
            inicio = LocalDate.parse(String.valueOf(bruto));
            fim = argumentos.get("dataFim") == null ? inicio : LocalDate.parse(String.valueOf(argumentos.get("dataFim")));
        } catch (DateTimeParseException e) {
            return Map.of("erro", "Data inválida, use AAAA-MM-DD");
        }
        if (fim.isBefore(inicio)) {
            return Map.of("erro", "A data de fim não pode ser anterior à de início");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) + 1 > MAX_DIAS) {
            return Map.of("erro", "Período máximo de " + MAX_DIAS + " dias; divida a consulta");
        }
        Long professorId = ArgumentosTool.longOpcional(argumentos, "professorId");
        Long alunoId = ArgumentosTool.longOpcional(argumentos, "alunoId");

        List<Agendamento> encontrados = agendamentoRepository
                .buscarAgendamentosPorIntervalo(inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay()).stream()
                .filter(a -> professorId == null || (a.getProfessor() != null && a.getProfessor().getId().equals(professorId)))
                .filter(a -> alunoId == null || a.getAgendamentoAlunos().stream().anyMatch(aa -> aa.getAluno().getId().equals(alunoId)))
                .sorted(Comparator.comparing(Agendamento::getDataHora))
                .toList();

        List<Map<String, Object>> aulas = encontrados.stream().limit(MAX_AULAS)
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
        return Map.of("periodo", inicio + " a " + fim, "total", encontrados.size(),
                "exibidas", aulas.size(), "agendamentos", aulas);
    }
}
