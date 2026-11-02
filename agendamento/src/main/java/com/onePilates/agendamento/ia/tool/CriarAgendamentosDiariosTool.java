package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.agendamento.dto.AgendamentoDTO;
import com.onePilates.agendamento.agendamento.service.AgendamentoService;
import com.onePilates.agendamento.exception.BusinessException;
import com.onePilates.agendamento.exception.ValidacaoMultiplaException;
import com.onePilates.agendamento.model.enums.Role;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Agenda a mesma aula todos os dias úteis (ex.: "todo dia às 8h") dentro de um período de no máximo 6 meses.
 * Sábados e domingos são sempre ignorados conforme regra de negócio do estúdio (funcionamento de segunda a sexta).
 */
@Component
public class CriarAgendamentosDiariosTool implements FerramentaIA {

    private static final int MAX_MESES = 6;

    private final AgendamentoService agendamentoService;

    public CriarAgendamentosDiariosTool(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @Override
    public String nome() { return "criar_agendamentos_diarios"; }

    @Override
    public String descricao() {
        return "Agenda a mesma aula TODOS OS DIAS ÚTEIS (segunda a sexta-feira, ex.: 'todo dia às 8h', 'de segunda a sexta às 18h') "
                + "dentro de um período de NO MÁXIMO 6 meses, com professor, sala, especialidade e alunos. "
                + "Sábados e domingos são automaticamente ignorados pois o estúdio não funciona aos finais de semana. "
                + "Cada data é criada em separado: as que o sistema recusar não impedem as outras e voltam em 'falhas' com o motivo. "
                + "Se o usuário pedir recorrência diária e NÃO informar até quando, pergunte o período (máximo 6 meses) antes de chamar; "
                + "nunca assuma o período. Descubra antes todos os ids com as tools de consulta. Para um único dia da semana use "
                + "criar_agendamentos_recorrentes; para uma aula única use criar_agendamento.";
    }

    /** Mesma permissão do POST /api/agendamentos. */
    @Override
    public Set<Role> rolesPermitidas() { return Set.of(Role.ADMINISTRADOR, Role.SECRETARIA); }

    @Override
    public Map<String, Object> parametros() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "horario", Map.of("type", "string",
                                "description", "Horário em hora cheia HH:00 entre 08:00 e 19:00, ex.: 08:00"),
                        "dataInicio", Map.of("type", "string", "description", "AAAA-MM-DD; primeiro dia (inclusive)"),
                        "dataFim", Map.of("type", "string",
                                "description", "AAAA-MM-DD (inclusive); no máximo 6 meses após dataInicio"),
                        "apenasDiasUteis", Map.of("type", "boolean",
                                "description", "Sempre considerado true (o estúdio funciona apenas de segunda a sexta-feira)"),
                        "professorId", Map.of("type", "integer"),
                        "salaId", Map.of("type", "integer"),
                        "especialidadeId", Map.of("type", "integer"),
                        "alunoIds", Map.of("type", "array", "items", Map.of("type", "integer"))),
                "required", List.of("horario", "dataInicio", "dataFim",
                        "professorId", "salaId", "especialidadeId", "alunoIds"));
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        LocalTime horario;
        LocalDate inicio;
        LocalDate fim;
        try {
            horario = LocalTime.parse(String.valueOf(argumentos.get("horario")).trim());
            inicio = LocalDate.parse(String.valueOf(argumentos.get("dataInicio")));
            fim = LocalDate.parse(String.valueOf(argumentos.get("dataFim")));
        } catch (DateTimeParseException e) {
            return Map.of("erro", "horario deve ser HH:MM e as datas AAAA-MM-DD");
        }

        String erroHorario = GuardrailsNegocioIA.validarHorarioExpediente(horario);
        if (erroHorario != null) {
            return Map.of("erro", erroHorario);
        }

        if (fim.isBefore(inicio)) {
            return Map.of("erro", "A data de fim não pode ser anterior à de início");
        }
        if (fim.isAfter(inicio.plusMonths(MAX_MESES))) {
            return Map.of("erro", "O período máximo para agendamento recorrente é de " + MAX_MESES
                    + " meses. Peça ao usuário um período menor.");
        }

        Long professorId = ArgumentosTool.longObrigatorio(argumentos, "professorId");
        Long salaId = ArgumentosTool.longObrigatorio(argumentos, "salaId");
        Long especialidadeId = ArgumentosTool.longObrigatorio(argumentos, "especialidadeId");
        for (Map.Entry<String, Long> idEntry : List.of(
                Map.entry("professor", professorId),
                Map.entry("sala", salaId),
                Map.entry("especialidade", especialidadeId))) {
            String erroId = GuardrailsNegocioIA.validarIdPositivo(idEntry.getValue(), idEntry.getKey());
            if (erroId != null) {
                return Map.of("erro", erroId);
            }
        }

        Set<Long> alunoIds = lerAlunoIds(argumentos.get("alunoIds"));
        String erroAlunos = GuardrailsNegocioIA.validarListaAlunos(alunoIds);
        if (erroAlunos != null) {
            return Map.of("erro", erroAlunos);
        }

        // Regra de negócio inviolável: o estúdio funciona apenas de segunda a sexta-feira (pula sábado e domingo)
        List<LocalDateTime> datas = new ArrayList<>();
        for (LocalDate d = inicio; !d.isAfter(fim); d = d.plusDays(1)) {
            boolean fimDeSemana = d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY;
            if (!fimDeSemana) {
                datas.add(d.atTime(horario));
            }
        }
        if (datas.isEmpty()) {
            return Map.of("erro", "Regra de negócio: o estúdio não abre aos sábados e domingos. "
                    + "Não há nenhum dia útil (segunda a sexta-feira) entre " + inicio + " e " + fim + ".");
        }

        // cada criação tem a própria transação (no service): uma recusa não desfaz as que deram certo
        List<String> criadas = new ArrayList<>();
        List<Map<String, Object>> falhas = new ArrayList<>();
        for (LocalDateTime dataHora : datas) {
            AgendamentoDTO dto = new AgendamentoDTO();
            dto.setDataHora(dataHora);
            dto.setProfessorId(professorId);
            dto.setSalaId(salaId);
            dto.setEspecialidadeId(especialidadeId);
            dto.setAlunoIds(new HashSet<>(alunoIds));
            try {
                agendamentoService.criarAgendamento(dto);
                criadas.add(dataHora.toString());
            } catch (ValidacaoMultiplaException e) {
                falhas.add(Map.of("data", dataHora.toString(), "motivos", e.getErros()));
            } catch (BusinessException e) {
                falhas.add(Map.of("data", dataHora.toString(), "motivos", List.of(String.valueOf(e.getMessage()))));
            } catch (Exception e) {
                falhas.add(Map.of("data", dataHora.toString(),
                        "motivos", List.of("Erro inesperado: " + e.getMessage())));
            }
        }
        return Map.of(
                "sucesso", !criadas.isEmpty(),
                "frequencia", "dias úteis (segunda a sexta-feira)",
                "periodo", inicio + " a " + fim,
                "totalTentativas", datas.size(),
                "totalCriadas", criadas.size(),
                "totalComErro", falhas.size(),
                "criadas", criadas,
                "falhas", falhas);
    }

    private Set<Long> lerAlunoIds(Object bruto) {
        Set<Long> ids = new HashSet<>();
        if (bruto instanceof List<?> lista) {
            for (Object item : lista) {
                ids.add(item instanceof Number n ? n.longValue() : Long.parseLong(String.valueOf(item).trim()));
            }
        }
        return ids;
    }
}
