package com.onePilates.agendamento.especialidade.service;

import com.onePilates.agendamento.especialidade.dto.EspecialidadeDTO;
import com.onePilates.agendamento.especialidade.dto.EspecialidadeResponseDTO;
import com.onePilates.agendamento.especialidade.repository.EspecialidadeRepository;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.model.Professor;
import com.onePilates.agendamento.model.Sala;
import com.onePilates.agendamento.professor.dto.ProfessorPorEspecialidadeResponseDTO;
import com.onePilates.agendamento.professor.repository.ProfessorRepository;
import com.onePilates.agendamento.sala.dto.SalasPorEspecialidadeResponseDTO;
import com.onePilates.agendamento.sala.repository.SalaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EspecialidadeServiceTest {

    @Mock
    private EspecialidadeRepository especialidadeRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private SalaRepository salaRepository;

    @InjectMocks
    private EspecialidadeService especialidadeService;

    private EspecialidadeDTO dto;
    private Especialidade especialidade;
    private Professor professor;
    private Sala sala;

    @BeforeEach
    void setUp() {
        dto = new EspecialidadeDTO();
        dto.setNome("Pilates Clássico");

        especialidade = new Especialidade();
        especialidade.setId(1L);
        especialidade.setNome("Pilates Clássico");

        professor = new Professor();
        professor.setId(1L);
        professor.setNome("Professor Teste");

        sala = new Sala();
        sala.setId(1L);
        sala.setNome("Sala Teste");
    }

    @Test
    void criarEspecialidade_DeveCriarComSucesso() {
        when(especialidadeRepository.save(any(Especialidade.class))).thenReturn(especialidade);

        Especialidade result = especialidadeService.criarEspecialidade(dto);

        assertNotNull(result);
        assertEquals("Pilates Clássico", result.getNome());
        verify(especialidadeRepository).save(any(Especialidade.class));
    }

    @Test
    void listarTodasDTO_DeveRetornarLista() {
        List<Especialidade> especialidades = Arrays.asList(especialidade);
        when(especialidadeRepository.findAll()).thenReturn(especialidades);

        List<EspecialidadeResponseDTO> result = especialidadeService.listarTodasDTO();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Pilates Clássico", result.get(0).getNome());
        verify(especialidadeRepository).findAll();
    }

    @Test
    void buscarPorIdDTO_DeveRetornarEspecialidade() {
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));

        EspecialidadeResponseDTO result = especialidadeService.buscarPorIdDTO(1L);

        assertNotNull(result);
        assertEquals("Pilates Clássico", result.getNome());
        verify(especialidadeRepository).findById(1L);
    }

    @Test
    void buscarPorIdDTO_DeveLancarExcecao_QuandoNaoEncontrado() {
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            especialidadeService.buscarPorIdDTO(1L);
        });
    }

    @Test
    void atualizarEspecialidade_DeveAtualizarComSucesso() {
        EspecialidadeDTO dtoAtualizacao = new EspecialidadeDTO();
        dtoAtualizacao.setNome("Pilates Avançado");

        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(especialidadeRepository.save(any(Especialidade.class))).thenReturn(especialidade);

        EspecialidadeResponseDTO result = especialidadeService.atualizarEspecialidade(1L, dtoAtualizacao);

        assertNotNull(result);
        verify(especialidadeRepository).findById(1L);
        verify(especialidadeRepository).save(any(Especialidade.class));
    }

    @Test
    void atualizarEspecialidade_DeveLancarExcecao_QuandoNaoEncontrado() {
        EspecialidadeDTO dtoAtualizacao = new EspecialidadeDTO();

        when(especialidadeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            especialidadeService.atualizarEspecialidade(1L, dtoAtualizacao);
        });
    }

    @Test
    void excluirEspecialidade_DeveExcluirComSucesso() {
        when(especialidadeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(especialidadeRepository).deleteById(1L);

        especialidadeService.excluirEspecialidade(1L);

        verify(especialidadeRepository).existsById(1L);
        verify(especialidadeRepository).deleteById(1L);
    }

    @Test
    void excluirEspecialidade_DeveLancarExcecao_QuandoNaoEncontrado() {
        when(especialidadeRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            especialidadeService.excluirEspecialidade(1L);
        });

        verify(especialidadeRepository, never()).deleteById(any());
    }

    @Test
    void buscarProfessor_DeveRetornarLista() {
        List<Professor> professores = Arrays.asList(professor);
        when(professorRepository.findByEspecialidadesId(1L)).thenReturn(professores);

        List<ProfessorPorEspecialidadeResponseDTO> result = especialidadeService.BuscarProfessor(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Professor Teste", result.get(0).getNome());
        verify(professorRepository).findByEspecialidadesId(1L);
    }

    @Test
    void buscarSalasPorEspecialidade_DeveRetornarLista() {
        List<Sala> salas = Arrays.asList(sala);
        when(salaRepository.findByEspecialidadesId(1L)).thenReturn(salas);

        List<SalasPorEspecialidadeResponseDTO> result = especialidadeService.buscarSalasPorEspecialidade(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Sala Teste", result.get(0).getNome());
        verify(salaRepository).findByEspecialidadesId(1L);
    }

    @Test
    void toResponseDTO_DeveConverterCorretamente() {
        EspecialidadeResponseDTO result = especialidadeService.toResponseDTO(especialidade);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Pilates Clássico", result.getNome());
    }
}

