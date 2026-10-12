package com.onePilates.agendamento.agendamento.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AulaPorEspecialidadeDTOTest {

    @Test
    void construtorPadrao_DeveCriarInstancia() {
        AulaPorEspecialidadeDTO dto = new AulaPorEspecialidadeDTO();

        assertNotNull(dto);
    }

    @Test
    void construtorComParametros_DeveCriarInstanciaCorretamente() {
        AulaPorEspecialidadeDTO dto = new AulaPorEspecialidadeDTO(
                1L, "Pilates Clássico", 75.5
        );

        assertEquals(1L, dto.getProfessorId());
        assertEquals("Pilates Clássico", dto.getEspecialidade());
        assertEquals(75.5, dto.getPercentualAulas());
    }

    @Test
    void gettersESetters_DeveFuncionarCorretamente() {
        AulaPorEspecialidadeDTO dto = new AulaPorEspecialidadeDTO();

        dto.setProfessorId(2L);
        dto.setEspecialidade("Pilates Funcional");
        dto.setPercentualAulas(50.0);

        assertEquals(2L, dto.getProfessorId());
        assertEquals("Pilates Funcional", dto.getEspecialidade());
        assertEquals(50.0, dto.getPercentualAulas());
    }

    @Test
    void setters_DeveAceitarNull() {
        AulaPorEspecialidadeDTO dto = new AulaPorEspecialidadeDTO();

        dto.setProfessorId(null);
        dto.setEspecialidade(null);
        dto.setPercentualAulas(null);

        assertNull(dto.getProfessorId());
        assertNull(dto.getEspecialidade());
        assertNull(dto.getPercentualAulas());
    }
}


