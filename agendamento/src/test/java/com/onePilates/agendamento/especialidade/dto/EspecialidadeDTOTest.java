package com.onePilates.agendamento.especialidade.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EspecialidadeDTOTest {

    @Test
    void construtorPadrao_DeveCriarInstancia() {
        EspecialidadeDTO dto = new EspecialidadeDTO();

        assertNotNull(dto);
    }

    @Test
    void construtorComParametros_DeveCriarInstanciaCorretamente() {
        EspecialidadeDTO dto = new EspecialidadeDTO("Pilates Clássico");

        assertEquals("Pilates Clássico", dto.getNome());
    }

    @Test
    void gettersESetters_DeveFuncionarCorretamente() {
        EspecialidadeDTO dto = new EspecialidadeDTO();

        dto.setNome("Pilates Funcional");

        assertEquals("Pilates Funcional", dto.getNome());
    }

    @Test
    void setter_DeveAceitarNull() {
        EspecialidadeDTO dto = new EspecialidadeDTO();

        dto.setNome(null);

        assertNull(dto.getNome());
    }
}


