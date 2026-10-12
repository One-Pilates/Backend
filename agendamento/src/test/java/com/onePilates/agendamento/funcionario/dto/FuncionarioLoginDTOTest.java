package com.onePilates.agendamento.funcionario.dto;

import com.onePilates.agendamento.especialidade.dto.EspecialidadeResponseDTO;
import com.onePilates.agendamento.model.Endereco;
import com.onePilates.agendamento.model.enums.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FuncionarioLoginDTOTest {


    @Test
    void construtorSemEspecialidades_DeveCriarInstanciaCorretamente() {
        Endereco endereco = new Endereco();
        FuncionarioLoginDTO dto = new FuncionarioLoginDTO(
                1L, "Nome", "email@test.com", Role.ADMINISTRADOR,
                "12345678900", LocalDate.of(1990, 1, 1), true,
                "foto.jpg", "obs", true, "Cargo",
                endereco, "123456789", true
        );

        assertEquals(1L, dto.getId());
        assertEquals("Nome", dto.getNome());
        assertEquals("email@test.com", dto.getEmail());
        assertEquals(Role.ADMINISTRADOR, dto.getRole());
        assertEquals("12345678900", dto.getCpf());
        assertEquals(LocalDate.of(1990, 1, 1), dto.getDataNascimento());
        assertTrue(dto.getStatus());
        assertEquals("foto.jpg", dto.getFoto());
        assertEquals("obs", dto.getObservacoes());
        assertTrue(dto.getNotificacaoAtiva());
        assertEquals("Cargo", dto.getCargo());
        assertEquals(endereco, dto.getEndereco());
        assertEquals("123456789", dto.getTelefone());
        assertTrue(dto.getPrimeiroAcesso());
    }

    @Test
    void construtorComEspecialidades_DeveCriarInstanciaCorretamente() {
        Endereco endereco = new Endereco();
        List<EspecialidadeResponseDTO> especialidades = new ArrayList<>();
        especialidades.add(new EspecialidadeResponseDTO(1L, "Pilates"));

        FuncionarioLoginDTO dto = new FuncionarioLoginDTO(
                1L, "Nome", "email@test.com", Role.PROFESSOR,
                "12345678900", LocalDate.of(1990, 1, 1), true,
                "foto.jpg", "obs", true, "Cargo",
                endereco, "123456789", especialidades, true
        );

        assertEquals(1L, dto.getId());
        assertEquals("Nome", dto.getNome());
        assertEquals(especialidades, dto.getEspecialidades());
    }

    @Test
    void gettersESetters_DeveFuncionarCorretamente() {
        Endereco enderecoInicial = new Endereco();
        FuncionarioLoginDTO dto = new FuncionarioLoginDTO(
                1L, "Nome", "email@test.com", Role.SECRETARIA,
                "12345678900", LocalDate.of(1990, 1, 1), false,
                "foto.jpg", "obs", false, "Cargo",
                enderecoInicial, "123456789", false
        );

        dto.setId(1L);
        dto.setNome("Nome");
        dto.setEmail("email@test.com");
        dto.setRole(Role.SECRETARIA);
        dto.setCpf("12345678900");
        dto.setDataNascimento(LocalDate.of(1990, 1, 1));
        dto.setStatus(false);
        dto.setFoto("foto.jpg");
        dto.setObservacoes("obs");
        dto.setNotificacaoAtiva(false);
        dto.setCargo("Cargo");
        dto.setTelefone("123456789");
        dto.setPrimeiroAcesso(false);
        
        Endereco enderecoNovo = new Endereco();
        dto.setEndereco(enderecoNovo);
        
        List<EspecialidadeResponseDTO> especialidades = new ArrayList<>();
        dto.setEspecialidades(especialidades);

        assertEquals(1L, dto.getId());
        assertEquals("Nome", dto.getNome());
        assertEquals("email@test.com", dto.getEmail());
        assertEquals(Role.SECRETARIA, dto.getRole());
        assertEquals("12345678900", dto.getCpf());
        assertEquals(LocalDate.of(1990, 1, 1), dto.getDataNascimento());
        assertFalse(dto.getStatus());
        assertEquals("foto.jpg", dto.getFoto());
        assertEquals("obs", dto.getObservacoes());
        assertFalse(dto.getNotificacaoAtiva());
        assertEquals("Cargo", dto.getCargo());
        assertEquals(enderecoNovo, dto.getEndereco());
        assertEquals("123456789", dto.getTelefone());
        assertEquals(especialidades, dto.getEspecialidades());
        assertFalse(dto.getPrimeiroAcesso());
    }
}

