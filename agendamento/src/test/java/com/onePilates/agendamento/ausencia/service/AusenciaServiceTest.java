package com.onePilates.agendamento.ausencia.service;

import com.onePilates.agendamento.ausencia.dto.AusenciaDTO;
import com.onePilates.agendamento.ausencia.dto.AusenciaResponseDTO;
import com.onePilates.agendamento.ausencia.repository.AusenciaRepository;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.model.Ausencia;
import com.onePilates.agendamento.model.Professor;
import com.onePilates.agendamento.model.enums.DiaSemana;
import com.onePilates.agendamento.professor.repository.ProfessorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AusenciaServiceTest {

    @Mock
    private AusenciaRepository ausenciaRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @InjectMocks
    private AusenciaService ausenciaService;

    private AusenciaDTO dto;
    private Ausencia ausencia;
    private Professor professor;

    @BeforeEach
    void setUp() {
        professor = new Professor();
        professor.setId(1L);
        professor.setNome("Professor Teste");

        dto = new AusenciaDTO();
        dto.setProfessorId(1L);
        dto.setDataInicio(LocalDateTime.now());
        dto.setDataFim(LocalDateTime.now().plusDays(5));
        dto.setDiaSemanaInicio(DiaSemana.SEGUNDA);
        dto.setDiaSemanaFim(DiaSemana.SEXTA);
        dto.setMotivo("Férias");

        ausencia = new Ausencia();
        ausencia.setId(1);
        ausencia.setProfessor(professor);
        ausencia.setDataInicio(dto.getDataInicio());
        ausencia.setDataFim(dto.getDataFim());
        ausencia.setDiaSemanaInicio(dto.getDiaSemanaInicio());
        ausencia.setDiaSemanaFim(dto.getDiaSemanaFim());
        ausencia.setMotivo(dto.getMotivo());
    }

    @Test
    void registrarAusencia_DeveRegistrarComSucesso() {
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(ausenciaRepository.save(any(Ausencia.class))).thenReturn(ausencia);

        AusenciaResponseDTO result = ausenciaService.registrarAusencia(dto);

        assertNotNull(result);
        assertEquals("Professor Teste", result.getNomeProfessor());
        verify(professorRepository).findById(1L);
        verify(ausenciaRepository).save(any(Ausencia.class));
    }

    @Test
    void registrarAusencia_DeveLancarExcecao_QuandoProfessorNaoEncontrado() {
        when(professorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            ausenciaService.registrarAusencia(dto);
        });

        verify(ausenciaRepository, never()).save(any());
    }

    @Test
    void listarPorProfessor_DeveRetornarLista() {
        List<Ausencia> ausencias = Arrays.asList(ausencia);
        when(ausenciaRepository.findByProfessorId(1L)).thenReturn(ausencias);

        List<AusenciaResponseDTO> result = ausenciaService.listarPorProfessor(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Professor Teste", result.get(0).getNomeProfessor());
        verify(ausenciaRepository).findByProfessorId(1L);
    }

    @Test
    void atualizarAusencia_DeveAtualizarComSucesso() {
        AusenciaDTO dtoAtualizacao = new AusenciaDTO();
        dtoAtualizacao.setMotivo("Férias Atualizadas");

        when(ausenciaRepository.findById(1)).thenReturn(Optional.of(ausencia));
        when(ausenciaRepository.save(any(Ausencia.class))).thenReturn(ausencia);

        AusenciaResponseDTO result = ausenciaService.atualizarAusencia(1, dtoAtualizacao);

        assertNotNull(result);
        verify(ausenciaRepository).findById(1);
        verify(ausenciaRepository).save(any(Ausencia.class));
    }

    @Test
    void atualizarAusencia_DeveAtualizarProfessor_QuandoFornecido() {
        Professor novoProfessor = new Professor();
        novoProfessor.setId(2L);
        novoProfessor.setNome("Novo Professor");

        AusenciaDTO dtoAtualizacao = new AusenciaDTO();
        dtoAtualizacao.setProfessorId(2L);

        when(ausenciaRepository.findById(1)).thenReturn(Optional.of(ausencia));
        when(professorRepository.findById(2L)).thenReturn(Optional.of(novoProfessor));
        when(ausenciaRepository.save(any(Ausencia.class))).thenReturn(ausencia);

        ausenciaService.atualizarAusencia(1, dtoAtualizacao);

        verify(professorRepository).findById(2L);
        verify(ausenciaRepository).save(any(Ausencia.class));
    }

    @Test
    void atualizarAusencia_DeveLancarExcecao_QuandoAusenciaNaoEncontrada() {
        AusenciaDTO dtoAtualizacao = new AusenciaDTO();

        when(ausenciaRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            ausenciaService.atualizarAusencia(1, dtoAtualizacao);
        });
    }

    @Test
    void atualizarAusencia_DeveLancarExcecao_QuandoProfessorNaoEncontrado() {
        AusenciaDTO dtoAtualizacao = new AusenciaDTO();
        dtoAtualizacao.setProfessorId(999L);

        when(ausenciaRepository.findById(1)).thenReturn(Optional.of(ausencia));
        when(professorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            ausenciaService.atualizarAusencia(1, dtoAtualizacao);
        });
    }

    @Test
    void deletarAusencia_DeveDeletarComSucesso() {
        when(ausenciaRepository.findById(1)).thenReturn(Optional.of(ausencia));
        doNothing().when(ausenciaRepository).delete(ausencia);

        ausenciaService.deletarAusencia(1);

        verify(ausenciaRepository).findById(1);
        verify(ausenciaRepository).delete(ausencia);
    }

    @Test
    void deletarAusencia_DeveLancarExcecao_QuandoNaoEncontrado() {
        when(ausenciaRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            ausenciaService.deletarAusencia(1);
        });

        verify(ausenciaRepository, never()).delete(any());
    }
}

