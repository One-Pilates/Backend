package com.onePilates.agendamento.professor.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgendamentosPorProfessorDTOTest {

    @Test
    void construtorPadrao_DeveCriarInstancia() {
        AgendamentosPorProfessorDTO dto = new AgendamentosPorProfessorDTO();

        assertNotNull(dto);
    }

    @Test
    void gettersESetters_DeveFuncionarCorretamente() {
        AgendamentosPorProfessorDTO dto = new AgendamentosPorProfessorDTO();

        dto.setProfessorId(1L);
        dto.setNomeProfessor("Professor Teste");
        dto.setTotalAgendamentosPorProfessor(50L);

        assertEquals(1L, dto.getProfessorId());
        assertEquals("Professor Teste", dto.getNomeProfessor());
        assertEquals(50L, dto.getTotalAgendamentosPorProfessor());
    }

    @Test
    void setters_DeveAceitarNull() {
        AgendamentosPorProfessorDTO dto = new AgendamentosPorProfessorDTO();

        dto.setProfessorId(null);
        dto.setNomeProfessor(null);
        dto.setTotalAgendamentosPorProfessor(null);

        assertNull(dto.getProfessorId());
        assertNull(dto.getNomeProfessor());
        assertNull(dto.getTotalAgendamentosPorProfessor());
    }
}


