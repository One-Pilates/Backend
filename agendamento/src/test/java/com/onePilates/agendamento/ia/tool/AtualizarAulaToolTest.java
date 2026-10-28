package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.agendamento.dto.AgendamentoResponseDTO;
import com.onePilates.agendamento.agendamento.service.AgendamentoService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AtualizarAulaToolTest {

    private final AgendamentoService service = mock(AgendamentoService.class);
    private final AtualizarAulaTool tool = new AtualizarAulaTool(service);

    @Test
    void deveAtualizarCamposDaAulaEListaDeAlunos() {
        AgendamentoResponseDTO resposta = new AgendamentoResponseDTO();
        resposta.setId(7L);
        resposta.setDataHora(LocalDateTime.of(2026, 10, 20, 10, 0));
        resposta.setProfessor("Professora A");
        resposta.setSala("Sala 1");
        resposta.setEspecialidade("Pilates");
        when(service.atualizarAgendamento(eq(7L), any())).thenReturn(resposta);

        Object resultado = tool.executar(Map.of(
                "agendamentoId", 7,
                "dataHora", "2026-10-20T10:30",
                "professorId", 3,
                "alunoIds", List.of(10, 11)), null);

        assertTrue(resultado instanceof Map<?, ?>);
        assertEquals(true, ((Map<?, ?>) resultado).get("sucesso"));
        verify(service).atualizarAgendamento(eq(7L), any());
    }

    @Test
    void deveRecusarQuandoNenhumaAlteracaoFoiInformada() {
        Object resultado = tool.executar(Map.of("agendamentoId", 7), null);

        assertEquals("Nenhuma alteração informada", ((Map<?, ?>) resultado).get("erro"));
    }
}
