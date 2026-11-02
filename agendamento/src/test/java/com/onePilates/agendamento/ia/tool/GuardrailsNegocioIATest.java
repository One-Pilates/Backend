package com.onePilates.agendamento.ia.tool;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GuardrailsNegocioIATest {

    @Test
    void deveBloquearAgendamentoNoSabadoEDomingo() {
        LocalDate proximoSabado = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        LocalDate proximoDomingo = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

        String erroSabado = GuardrailsNegocioIA.validarDataHoraAula(proximoSabado.atTime(10, 0));
        String erroDomingo = GuardrailsNegocioIA.validarDataHoraAula(proximoDomingo.atTime(10, 0));

        assertNotNull(erroSabado);
        assertTrue(erroSabado.contains("sábados e domingos"));
        assertNotNull(erroDomingo);
        assertTrue(erroDomingo.contains("sábados e domingos"));
    }

    @Test
    void deveBloquearHorarioForaDoExpedienteOuMinutosQuebrados() {
        assertNotNull(GuardrailsNegocioIA.validarHorarioExpediente(LocalTime.of(7, 0)));
        assertNotNull(GuardrailsNegocioIA.validarHorarioExpediente(LocalTime.of(20, 0)));
        assertNotNull(GuardrailsNegocioIA.validarHorarioExpediente(LocalTime.of(10, 30)));
        assertNull(GuardrailsNegocioIA.validarHorarioExpediente(LocalTime.of(8, 0)));
        assertNull(GuardrailsNegocioIA.validarHorarioExpediente(LocalTime.of(19, 0)));
    }

    @Test
    void criarAgendamentosRecorrentesDeveBloquearSabadoSemChamarService() {
        CriarAgendamentosRecorrentesTool tool = new CriarAgendamentosRecorrentesTool(null);
        LocalDate inicio = LocalDate.now().plusDays(1);

        Object resultado = tool.executar(Map.of(
                "diaSemana", "SABADO",
                "horario", "10:00",
                "dataInicio", inicio.toString(),
                "dataFim", inicio.plusWeeks(4).toString(),
                "professorId", 1,
                "salaId", 1,
                "especialidadeId", 1,
                "alunoIds", List.of(1)
        ), null);

        assertTrue(resultado instanceof Map<?, ?>);
        String erro = String.valueOf(((Map<?, ?>) resultado).get("erro"));
        assertTrue(erro.contains("sábados e domingos"));
    }

    @Test
    void criarAgendamentoDeveBloquearListaAlunosVaziaOuIdInvalido() {
        assertNotNull(GuardrailsNegocioIA.validarListaAlunos(Set.of()));
        assertNotNull(GuardrailsNegocioIA.validarIdPositivo(0L, "professor"));
        assertNotNull(GuardrailsNegocioIA.validarIdPositivo(-5L, "sala"));
        assertNull(GuardrailsNegocioIA.validarIdPositivo(1L, "sala"));
    }
}
