package com.onePilates.agendamento.agendamento.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgendamentoPorDiaDTOTest {

    @Test
    void construtorPadrao_DeveCriarInstancia() {
        AgendamentoPorDiaDTO dto = new AgendamentoPorDiaDTO();

        assertNotNull(dto);
    }

    @Test
    void construtorComParametros_DeveCriarInstanciaCorretamente() {
        AgendamentoPorDiaDTO dto = new AgendamentoPorDiaDTO(
                "SEGUNDA", 10L
        );

        assertEquals("SEGUNDA", dto.getDiaSemana());
        assertEquals(10L, dto.getTotalAgendamentos());
    }

    @Test
    void gettersESetters_DeveFuncionarCorretamente() {
        AgendamentoPorDiaDTO dto = new AgendamentoPorDiaDTO();

        dto.setDiaSemana("TERCA");
        dto.setTotalAgendamentos(20L);

        assertEquals("TERCA", dto.getDiaSemana());
        assertEquals(20L, dto.getTotalAgendamentos());
    }

    @Test
    void setters_DeveAceitarNull() {
        AgendamentoPorDiaDTO dto = new AgendamentoPorDiaDTO();

        dto.setDiaSemana(null);
        dto.setTotalAgendamentos(null);

        assertNull(dto.getDiaSemana());
        assertNull(dto.getTotalAgendamentos());
    }
}


