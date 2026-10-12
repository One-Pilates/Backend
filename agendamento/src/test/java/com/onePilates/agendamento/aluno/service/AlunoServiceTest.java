package com.onePilates.agendamento.aluno.service;

import com.onePilates.agendamento.aluno.dto.AlunoDTO;
import com.onePilates.agendamento.aluno.dto.AlunoResponseDTO;
import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.endereco.dto.EnderecoDTO;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.Endereco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlunoServiceTest {

    @Mock
    private AlunoRepository alunoRepository;

    @InjectMocks
    private AlunoService alunoService;

    private AlunoDTO dto;
    private Aluno aluno;
    private Endereco endereco;

    @BeforeEach
    void setUp() {
        dto = new AlunoDTO();
        dto.setNome("Aluno Teste");
        dto.setEmail("aluno@teste.com");
        dto.setCpf("12345678900");
        dto.setDataNascimento(LocalDate.of(2000, 1, 1));
        dto.setStatus(true);
        dto.setAlunoComLimitacoesFisicas(false);
        dto.setNotificacaoAtiva(true);

        EnderecoDTO enderecoDTO = new EnderecoDTO();
        enderecoDTO.setRua("Rua Teste");
        enderecoDTO.setNumero("123");
        enderecoDTO.setBairro("Bairro Teste");
        enderecoDTO.setCidade("Cidade Teste");
        enderecoDTO.setEstado("Estado Teste");
        enderecoDTO.setCep("12345678");
        enderecoDTO.setUf("SP");
        dto.setEndereco(enderecoDTO);

        aluno = new Aluno();
        aluno.setId(1L);
        aluno.setNome("Aluno Teste");
        aluno.setEmail("aluno@teste.com");
        aluno.setCpf("12345678900");
        aluno.setDataNascimento(LocalDate.of(2000, 1, 1));
        aluno.setStatus(true);
        aluno.setAlunoComLimitacoesFisicas(false);

        endereco = new Endereco();
        endereco.setId(1L);
        endereco.setRua("Rua Teste");
        endereco.setNumero("123");
        endereco.setBairro("Bairro Teste");
        endereco.setCidade("Cidade Teste");
        endereco.setEstado("Estado Teste");
        endereco.setCep("12345678");
        endereco.setUf("SP");
        aluno.setEndereco(endereco);
    }

    @Test
    void criarAluno_DeveCriarComSucesso() {
        when(alunoRepository.save(any(Aluno.class))).thenReturn(aluno);

        Aluno result = alunoService.criarAluno(dto);

        assertNotNull(result);
        assertEquals("Aluno Teste", result.getNome());
        verify(alunoRepository).save(any(Aluno.class));
    }

    @Test
    void listarTodosDTO_DeveRetornarLista() {
        List<Aluno> alunos = Arrays.asList(aluno);
        when(alunoRepository.findAll()).thenReturn(alunos);

        List<AlunoResponseDTO> result = alunoService.listarTodosDTO();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Aluno Teste", result.get(0).getNome());
        verify(alunoRepository).findAll();
    }

    @Test
    void buscarPorIdDTO_DeveRetornarAluno() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));

        AlunoResponseDTO result = alunoService.buscarPorIdDTO(1L);

        assertNotNull(result);
        assertEquals("Aluno Teste", result.getNome());
        verify(alunoRepository).findById(1L);
    }

    @Test
    void buscarPorIdDTO_DeveLancarExcecao_QuandoNaoEncontrado() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            alunoService.buscarPorIdDTO(1L);
        });
    }

    @Test
    void atualizarAluno_DeveAtualizarComSucesso() {
        AlunoDTO dtoAtualizacao = new AlunoDTO();
        dtoAtualizacao.setNome("Aluno Atualizado");
        dtoAtualizacao.setStatus(false);

        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(alunoRepository.save(any(Aluno.class))).thenReturn(aluno);

        AlunoResponseDTO result = alunoService.atualizarAluno(1L, dtoAtualizacao);

        assertNotNull(result);
        verify(alunoRepository).findById(1L);
        verify(alunoRepository).save(any(Aluno.class));
    }

    @Test
    void atualizarAluno_DeveAtualizarEndereco_QuandoEnderecoFornecido() {
        AlunoDTO dtoAtualizacao = new AlunoDTO();
        EnderecoDTO enderecoDTO = new EnderecoDTO();
        enderecoDTO.setRua("Nova Rua");
        enderecoDTO.setCidade("Nova Cidade");
        dtoAtualizacao.setEndereco(enderecoDTO);

        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(alunoRepository.save(any(Aluno.class))).thenReturn(aluno);

        alunoService.atualizarAluno(1L, dtoAtualizacao);

        verify(alunoRepository).save(any(Aluno.class));
    }

    @Test
    void atualizarAluno_DeveCriarNovoEndereco_QuandoAlunoNaoTemEndereco() {
        aluno.setEndereco(null);
        AlunoDTO dtoAtualizacao = new AlunoDTO();
        EnderecoDTO enderecoDTO = new EnderecoDTO();
        enderecoDTO.setRua("Nova Rua");
        dtoAtualizacao.setEndereco(enderecoDTO);

        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(alunoRepository.save(any(Aluno.class))).thenReturn(aluno);

        alunoService.atualizarAluno(1L, dtoAtualizacao);

        verify(alunoRepository).save(any(Aluno.class));
    }

    @Test
    void atualizarAluno_DeveLancarExcecao_QuandoNaoEncontrado() {
        AlunoDTO dtoAtualizacao = new AlunoDTO();

        when(alunoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            alunoService.atualizarAluno(1L, dtoAtualizacao);
        });
    }

    @Test
    void excluirAluno_DeveExcluirComSucesso() {
        when(alunoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(alunoRepository).deleteById(1L);

        alunoService.excluirAluno(1L);

        verify(alunoRepository).existsById(1L);
        verify(alunoRepository).deleteById(1L);
    }

    @Test
    void excluirAluno_DeveLancarExcecao_QuandoNaoEncontrado() {
        when(alunoRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            alunoService.excluirAluno(1L);
        });

        verify(alunoRepository, never()).deleteById(any());
    }

    @Test
    void toResponseDTO_DeveConverterCorretamente() {
        AlunoResponseDTO result = alunoService.toResponseDTO(aluno);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Aluno Teste", result.getNome());
        assertEquals("aluno@teste.com", result.getEmail());
        assertEquals("12345678900", result.getCpf());
        assertNotNull(result.getEndereco());
        assertEquals("Rua Teste", result.getEndereco().getRua());
    }
}

