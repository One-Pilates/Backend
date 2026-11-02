package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.aluno.dto.AgendaItemDTO;
import com.onePilates.agendamento.aluno.service.PacoteAulaService;
import com.onePilates.agendamento.aluno.service.PortalAlunoService;
import com.onePilates.agendamento.model.enums.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MinhaAgendaToolTest {

    @Test
    void minhaAgenda_DeveIgnorarAlunoIdDoModelo() {
        PortalAlunoService portal = mock(PortalAlunoService.class);
        PacoteAulaService pacotes = mock(PacoteAulaService.class);
        AgendaItemDTO item = new AgendaItemDTO();
        item.setDataHora(LocalDateTime.of(2026, 6, 12, 10, 0));
        item.setProfessor("Professora");
        item.setSala("Sala 1");
        item.setEspecialidade("Pilates");
        item.setPresencaConfirmada(true);
        when(portal.listarAgenda(5L, null, null)).thenReturn(List.of(item));
        when(pacotes.listar(5L)).thenReturn(List.of());

        MinhaAgendaTool tool = new MinhaAgendaTool(portal, pacotes);
        Object resposta = tool.executar(Map.of("alunoId", 99), new ContextoUsuario(5L, "Ana", Role.ALUNO, "minha agenda"));

        verify(portal).listarAgenda(5L, null, null);
        String texto = resposta.toString();
        assertTrue(texto.contains("Professora"));
        assertTrue(texto.contains("Sala 1"));
        assertTrue(texto.contains("Pilates"));
        assertTrue(texto.contains("true"));
        assertFalse(texto.contains("99"));
        assertFalse(texto.contains("outro aluno"));
    }
}
