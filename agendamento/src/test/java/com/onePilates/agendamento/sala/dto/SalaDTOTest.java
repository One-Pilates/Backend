package com.onePilates.agendamento.sala.dto;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SalaDTOTest {

    @Test
    void construtorPadrao_DeveCriarInstancia() {
        SalaDTO dto = new SalaDTO();

        assertNotNull(dto);
    }

    @Test
    void construtorComParametros_DeveCriarInstanciaCorretamente() {
        Set<Long> especialidadeIds = Set.of(1L, 2L);
        SalaDTO dto = new SalaDTO("Sala 1", especialidadeIds);

        assertEquals("Sala 1", dto.getNome());
        assertEquals(especialidadeIds, dto.getEspecialidadeIds());
    }

    @Test
    void gettersESetters_DeveFuncionarCorretamente() {
        SalaDTO dto = new SalaDTO();

        dto.setNome("Sala 2");
        Set<Long> especialidadeIds = new HashSet<>();
        especialidadeIds.add(3L);
        especialidadeIds.add(4L);
        dto.setEspecialidadeIds(especialidadeIds);
        dto.setQuantidadeMaximaAlunos(10);
        dto.setQuantidadeEquipamentosPCD(2);

        assertEquals("Sala 2", dto.getNome());
        assertEquals(especialidadeIds, dto.getEspecialidadeIds());
        assertEquals(10, dto.getQuantidadeMaximaAlunos());
        assertEquals(2, dto.getQuantidadeEquipamentosPCD());
    }

    @Test
    void setters_DeveAceitarNull() {
        SalaDTO dto = new SalaDTO();

        dto.setNome(null);
        dto.setEspecialidadeIds(null);
        dto.setQuantidadeMaximaAlunos(null);
        dto.setQuantidadeEquipamentosPCD(null);

        assertNull(dto.getNome());
        assertNull(dto.getEspecialidadeIds());
        assertNull(dto.getQuantidadeMaximaAlunos());
        assertNull(dto.getQuantidadeEquipamentosPCD());
    }
}


