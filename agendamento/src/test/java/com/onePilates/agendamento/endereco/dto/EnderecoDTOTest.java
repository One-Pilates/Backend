package com.onePilates.agendamento.endereco.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnderecoDTOTest {

    @Test
    void construtorPadrao_DeveCriarInstancia() {
        EnderecoDTO dto = new EnderecoDTO();

        assertNotNull(dto);
    }

    @Test
    void construtorComParametros_DeveCriarInstanciaCorretamente() {
        EnderecoDTO dto = new EnderecoDTO(
                "Rua Teste", "123", "Centro", "São Paulo",
                "São Paulo", "01234567", "SP"
        );

        assertEquals("Rua Teste", dto.getRua());
        assertEquals("123", dto.getNumero());
        assertEquals("Centro", dto.getBairro());
        assertEquals("São Paulo", dto.getCidade());
        assertEquals("São Paulo", dto.getEstado());
        assertEquals("01234567", dto.getCep());
        assertEquals("SP", dto.getUf());
    }

    @Test
    void gettersESetters_DeveFuncionarCorretamente() {
        EnderecoDTO dto = new EnderecoDTO();

        dto.setRua("Rua Nova");
        dto.setNumero("456");
        dto.setBairro("Vila Nova");
        dto.setCidade("Rio de Janeiro");
        dto.setEstado("Rio de Janeiro");
        dto.setCep("20000000");
        dto.setUf("RJ");

        assertEquals("Rua Nova", dto.getRua());
        assertEquals("456", dto.getNumero());
        assertEquals("Vila Nova", dto.getBairro());
        assertEquals("Rio de Janeiro", dto.getCidade());
        assertEquals("Rio de Janeiro", dto.getEstado());
        assertEquals("20000000", dto.getCep());
        assertEquals("RJ", dto.getUf());
    }
}


