package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.agendamento.dto.AgendamentoDTO;
import com.onePilates.agendamento.agendamento.service.AgendamentoService;
import com.onePilates.agendamento.exception.BusinessException;
import com.onePilates.agendamento.exception.ValidacaoMultiplaException;
import com.onePilates.agendamento.model.enums.Role;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Agenda a mesma aula toda semana (ex.: todas as quintas às 10h) dentro de um período de no máximo 6 meses.
 * Cada data é criada em separado: as que o sistema recusar (conflito, ausência, sala lotada...) não impedem
 * as demais e voltam listadas com o motivo.
 */
@Component
public class CriarAgendamentosRecorrentesTool implements FerramentaIA {

    private static final int MAX_MESES = 6;

    private final AgendamentoService agendamentoService;

    public CriarAgendamentosRecorrentesTool(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @Override
    public String nome() { return "criar_agendamentos_recorrentes"; }

    @Override
    public String descricao() {
        return "Agenda a mesma aula toda semana em um dia da semana fixo (ex.: 'todas as quintas às 10h') dentro de um "
                + "período de NO MÁXIMO 6 meses, com professor, sala, especialidade e alunos. Cada data é criada em separado: "
                + "as que o sistema recusar não impedem as outras e voltam em 'falhas' com o motivo. Se o usuário pedir "
                + "recorrência e NÃO informar até quando, pergunte o período (máximo 6 meses) antes de chamar; nunca "
                + "assuma o período. Descubra antes todos os ids com as tools de consulta (buscar_alunos, "
                + "listar_professores_e_salas, listar_especialidades). Para uma aula única use criar_agendamento.";
    }

    /** Mesma permissão do POST /api/agendamentos. */
    @Override
    public Set<Role> rolesPermitidas() { return Set.of(Role.ADMINISTRADOR, Role.SECRETARIA); }

    @Override
    public Map<String, Object> parametros() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "diaSemana", Map.of("type", "string",
                                "description", "SEGUNDA, TERCA, QUARTA, QUINTA, SEXTA, SABADO ou DOMINGO"),
                        "horario", Map.of("type", "string", "description", "Horário HH:MM, ex.: 10:00"),
                        "dataInicio", Map.of("type", "string",
                                "description", "AAAA-MM-DD; a primeira aula é a primeira ocorrência do dia da semana a partir desta data"),
                        "dataFim", Map.of("type", "string",
                                "description", "AAAA-MM-DD (inclusive); no máximo 6 meses após dataInicio"),
                        "professorId", Map.of("type", "integer"),
                        "salaId", Map.of("type", "integer"),
                        "especialidadeId", Map.of("type", "integer"),
                        "alunoIds", Map.of("type", "array", "items", Map.of("type", "integer"))),
                "required", List.of("diaSemana", "horario", "dataInicio", "dataFim",
                        "professorId", "salaId", "especialidadeId", "alunoIds"));
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        DayOfWeek dia = lerDiaSemana(argumentos.get("diaSemana"));
        if (dia == null) {
            return Map.of("erro", "diaSemana inválido; use SEGUNDA, TERCA, QUARTA, QUINTA, SEXTA, SABADO ou DOMINGO");
        }
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
        Set<Long> alunoIds = lerAlunoIds(argumentos.get("alunoIds"));
        if (alunoIds.isEmpty()) {
            return Map.of("erro", "Informe ao menos um aluno");
        }

        List<LocalDateTime> datas = new ArrayList<>();
        for (LocalDate d = inicio.with(TemporalAdjusters.nextOrSame(dia)); !d.isAfter(fim); d = d.plusWeeks(1)) {
            datas.add(d.atTime(horario));
        }
        if (datas.isEmpty()) {
            return Map.of("erro", "Não há nenhuma " + dia + " entre " + inicio + " e " + fim);
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
                "diaSemana", dia.toString(),
                "periodo", inicio + " a " + fim,
                "totalTentativas", datas.size(),
                "totalCriadas", criadas.size(),
                "totalComErro", falhas.size(),
                "criadas", criadas,
                "falhas", falhas);
    }

    /** Aceita "quinta", "QUINTA", "quinta-feira", "Thursday"... */
    private DayOfWeek lerDiaSemana(Object bruto) {
        if (bruto == null) return null;
        String texto = Normalizer.normalize(bruto.toString(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase().trim();
        if (texto.startsWith("seg") || texto.startsWith("mon")) return DayOfWeek.MONDAY;
        if (texto.startsWith("ter") || texto.startsWith("tue")) return DayOfWeek.TUESDAY;
        if (texto.startsWith("qua") || texto.startsWith("wed")) return DayOfWeek.WEDNESDAY;
        if (texto.startsWith("qui") || texto.startsWith("thu")) return DayOfWeek.THURSDAY;
        if (texto.startsWith("sex") || texto.startsWith("fri")) return DayOfWeek.FRIDAY;
        if (texto.startsWith("sab") || texto.startsWith("sat")) return DayOfWeek.SATURDAY;
        if (texto.startsWith("dom") || texto.startsWith("sun")) return DayOfWeek.SUNDAY;
        return null;
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
