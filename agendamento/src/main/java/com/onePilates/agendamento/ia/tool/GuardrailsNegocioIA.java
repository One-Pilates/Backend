package com.onePilates.agendamento.ia.tool;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Guardrails de Regras de Negócio do Estúdio OnePilates para a OneIA.
 * Atua como bloqueador determinístico antes que qualquer ferramenta da IA
 * crie ou manipule agendamentos, evitando alucinações de parâmetros ou
 * violação das regras operacionais do estúdio.
 */
public final class GuardrailsNegocioIA {

    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private GuardrailsNegocioIA() {
    }

    /**
     * Valida data e horário completos de uma aula segundo as regras do estúdio:
     * - Proibido agendar no passado
     * - Antecedência máxima de 1 ano
     * - Funcionamento apenas de segunda a sexta-feira (proibido sábado e domingo)
     * - Expediente das 08:00 às 20:00 (aulas de 1h iniciando entre 08:00 e 19:00)
     * - Aulas devem iniciar em horas cheias (minutos zerados, ex.: 08:00, 14:00)
     *
     * @return mensagem de erro da regra de negócio ou {@code null} se válido
     */
    public static String validarDataHoraAula(LocalDateTime dataHora) {
        if (dataHora == null) {
            return "Regra de negócio: a data e o horário da aula são obrigatórios.";
        }

        LocalDateTime agora = LocalDateTime.now();
        if (dataHora.isBefore(agora)) {
            return String.format(
                    "Regra de negócio: não é permitido criar ou mover aulas para uma data/hora no passado (%s).",
                    dataHora.format(FORMATO_DATA_HORA));
        }

        if (dataHora.isAfter(agora.plusYears(1))) {
            return "Regra de negócio: não é permitido agendar aulas com mais de 1 ano de antecedência.";
        }

        String erroDia = validarDiaSemanaFuncionamento(dataHora.getDayOfWeek());
        if (erroDia != null) {
            return erroDia;
        }

        return validarHorarioExpediente(dataHora.toLocalTime());
    }

    /**
     * Valida se o dia da semana é dia útil de funcionamento do estúdio (Segunda a Sexta).
     *
     * @return mensagem de erro ou {@code null} se válido
     */
    public static String validarDiaSemanaFuncionamento(DayOfWeek dia) {
        if (dia == null) {
            return "Regra de negócio: dia da semana inválido.";
        }
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            return "Regra de negócio: o estúdio OnePilates não abre aos sábados e domingos. "
                    + "As aulas só podem ser agendadas de segunda a sexta-feira.";
        }
        return null;
    }

    /**
     * Valida se o horário de início da aula respeita o expediente (08:00 às 19:00 em hora cheia).
     *
     * @return mensagem de erro ou {@code null} se válido
     */
    public static String validarHorarioExpediente(LocalTime horario) {
        if (horario == null) {
            return "Regra de negócio: o horário da aula é obrigatório.";
        }
        int hora = horario.getHour();
        if (hora < 8 || hora >= 20) {
            return String.format(
                    "Regra de negócio: o horário de início da aula deve ser entre 08:00 e 19:00 "
                            + "(expediente do estúdio das 08:00 às 20:00). Horário informado: %02d:%02d.",
                    hora, horario.getMinute());
        }
        if (horario.getMinute() != 0) {
            return String.format(
                    "Regra de negócio: as aulas têm duração de 1 hora e devem iniciar sempre em horas cheias "
                            + "(ex.: %02d:00). Horário informado: %02d:%02d.",
                    hora, hora, horario.getMinute());
        }
        return null;
    }

    /**
     * Impede que a IA envie IDs nulos, zerados ou negativos (alucinação de IDs).
     *
     * @return mensagem de erro ou {@code null} se válido
     */
    public static String validarIdPositivo(Long id, String nomeEntidade) {
        if (id == null || id <= 0) {
            return String.format(
                    "Guardrail de integridade: o identificador de %s é inválido (%s). "
                            + "Consulte os registros reais antes de executar a ação.",
                    nomeEntidade, id);
        }
        return null;
    }

    /**
     * Valida a lista de alunos de uma aula (mínimo de 1 aluno e todos com IDs positivos).
     *
     * @return mensagem de erro ou {@code null} se válido
     */
    public static String validarListaAlunos(Set<Long> alunoIds) {
        if (alunoIds == null || alunoIds.isEmpty()) {
            return "Regra de negócio: toda aula deve possuir pelo menos 1 aluno ativo agendado.";
        }
        for (Long id : alunoIds) {
            if (id == null || id <= 0) {
                return "Guardrail de integridade: um ou mais IDs de alunos informados são inválidos. "
                        + "Use buscar_alunos para obter os IDs reais.";
            }
        }
        return null;
    }
}
