package com.onePilates.agendamento.professor.dto;

import com.onePilates.agendamento.model.enums.DiaSemana;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KPIsProfessorDTOTest {

    @Test
    void construtorPadrao_DeveCriarInstancia() {
        KPIsProfessorDTO dto = new KPIsProfessorDTO();

        assertNotNull(dto);
    }

    @Test
    void gettersESetters_DeveFuncionarCorretamente() {
        KPIsProfessorDTO dto = new KPIsProfessorDTO();

        dto.setQtdAlunosAtendidos(50);
        dto.setQtdTotalSessoesRealizadas(100);
        dto.setDiaSemanaComMaiorAtendimento(DiaSemana.SEGUNDA);
        dto.setEspecialidadeMaisRequisitada("Pilates Clássico");

        assertEquals(50, dto.getQtdAlunosAtendidos());
        assertEquals(100, dto.getQtdTotalSessoesRealizadas());
        assertEquals(DiaSemana.SEGUNDA, dto.getDiaSemanaComMaiorAtendimento());
        assertEquals("Pilates Clássico", dto.getEspecialidadeMaisRequisitada());
    }

    @Test
    void setters_DeveAceitarNull() {
        KPIsProfessorDTO dto = new KPIsProfessorDTO();

        dto.setQtdAlunosAtendidos(null);
        dto.setQtdTotalSessoesRealizadas(null);
        dto.setDiaSemanaComMaiorAtendimento(null);
        dto.setEspecialidadeMaisRequisitada(null);

        assertNull(dto.getQtdAlunosAtendidos());
        assertNull(dto.getQtdTotalSessoesRealizadas());
        assertNull(dto.getDiaSemanaComMaiorAtendimento());
        assertNull(dto.getEspecialidadeMaisRequisitada());
    }

    @Test
    void setters_DeveAceitarTodosOsDiasSemana() {
        KPIsProfessorDTO dto = new KPIsProfessorDTO();

        for (DiaSemana dia : DiaSemana.values()) {
            dto.setDiaSemanaComMaiorAtendimento(dia);
            assertEquals(dia, dto.getDiaSemanaComMaiorAtendimento());
        }
    }
}


