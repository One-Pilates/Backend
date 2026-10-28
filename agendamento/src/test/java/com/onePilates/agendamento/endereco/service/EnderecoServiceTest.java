package com.onePilates.agendamento.endereco.service;

import com.onePilates.agendamento.endereco.dto.EnderecoDTO;
import com.onePilates.agendamento.endereco.repository.EnderecoRepository;
import com.onePilates.agendamento.model.Endereco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock
    private EnderecoRepository repository;

    @InjectMocks
    private EnderecoService enderecoService;

    private EnderecoDTO dto;
    private Endereco endereco;

    @BeforeEach
    void setUp() {
        dto = new EnderecoDTO();
        dto.setRua("Rua Teste");
        dto.setNumero("123");
        dto.setBairro("Bairro Teste");
        dto.setCidade("Cidade Teste");
        dto.setEstado("Estado Teste");
        dto.setCep("12345678");
        dto.setUf("SP");

        endereco = new Endereco();
        endereco.setId(1L);
        endereco.setRua("Rua Teste");
        endereco.setNumero("123");
        endereco.setBairro("Bairro Teste");
        endereco.setCidade("Cidade Teste");
        endereco.setEstado("Estado Teste");
        endereco.setCep("12345678");
        endereco.setUf("SP");
    }

    @Test
    void cadastrarEndereco_DeveCadastrarComSucesso() {
        when(repository.save(any(Endereco.class))).thenReturn(endereco);

        Endereco result = enderecoService.cadastrarEndereco(dto);

        assertNotNull(result);
        assertEquals("Rua Teste", result.getRua());
        assertEquals("123", result.getNumero());
        assertEquals("Bairro Teste", result.getBairro());
        assertEquals("Cidade Teste", result.getCidade());
        assertEquals("Estado Teste", result.getEstado());
        assertEquals("12345678", result.getCep());
        assertEquals("SP", result.getUf());
        verify(repository).save(any(Endereco.class));
    }

    @Test
    void verTodosOsEnderecos_DeveRetornarLista() {
        List<Endereco> enderecos = Arrays.asList(endereco);
        when(repository.findAll()).thenReturn(enderecos);

        List<Endereco> result = enderecoService.verTodosOsEnderecos();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Rua Teste", result.get(0).getRua());
        verify(repository).findAll();
    }
}

