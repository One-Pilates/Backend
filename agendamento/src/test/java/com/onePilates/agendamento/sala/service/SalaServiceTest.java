package com.onePilates.agendamento.sala.service;

import com.onePilates.agendamento.especialidade.repository.EspecialidadeRepository;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.model.Sala;
import com.onePilates.agendamento.sala.dto.SalaDTO;
import com.onePilates.agendamento.sala.dto.SalaResponseDTO;
import com.onePilates.agendamento.sala.repository.SalaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaServiceTest {

    @Mock
    private SalaRepository salaRepository;

    @Mock
    private EspecialidadeRepository especialidadeRepository;

    @InjectMocks
    private SalaService salaService;

    private SalaDTO dto;
    private Sala sala;
    private Especialidade especialidade;

    @BeforeEach
    void setUp() {
        dto = new SalaDTO();
        dto.setNome("Sala Teste");
        dto.setQuantidadeMaximaAlunos(10);
        dto.setQuantidadeEquipamentosPCD(2);
        dto.setEspecialidadeIds(Set.of(1L));

        especialidade = new Especialidade();
        especialidade.setId(1L);
        especialidade.setNome("Pilates Clássico");

        sala = new Sala();
        sala.setId(1L);
        sala.setNome("Sala Teste");
        sala.setQuantidadeMaximaAlunos(10);
        sala.setQuantidadeEquipamentosPCD(2);
        sala.setEspecialidades(Set.of(especialidade));
    }

    @Test
    void criarSala_DeveCriarComSucesso() {
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(salaRepository.save(any(Sala.class))).thenReturn(sala);

        Sala result = salaService.criarSala(dto);

        assertNotNull(result);
        verify(especialidadeRepository).findById(1L);
        verify(salaRepository).save(any(Sala.class));
    }

    @Test
    void criarSala_DeveLancarExcecao_QuandoEspecialidadeNaoEncontrada() {
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            salaService.criarSala(dto);
        });
    }

    @Test
    void listarTodasDTO_DeveRetornarLista() {
        List<Sala> salas = Arrays.asList(sala);
        when(salaRepository.findAll()).thenReturn(salas);

        List<SalaResponseDTO> result = salaService.listarTodasDTO();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Sala Teste", result.get(0).getNome());
        verify(salaRepository).findAll();
    }

    @Test
    void buscarPorIdDTO_DeveRetornarSala() {
        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));

        SalaResponseDTO result = salaService.buscarPorIdDTO(1L);

        assertNotNull(result);
        assertEquals("Sala Teste", result.getNome());
        verify(salaRepository).findById(1L);
    }

    @Test
    void buscarPorIdDTO_DeveLancarExcecao_QuandoNaoEncontrado() {
        when(salaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            salaService.buscarPorIdDTO(1L);
        });
    }

    @Test
    void atualizarSala_DeveAtualizarComSucesso() {
        SalaDTO dtoAtualizacao = new SalaDTO();
        dtoAtualizacao.setNome("Sala Atualizada");
        dtoAtualizacao.setQuantidadeMaximaAlunos(15);

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(salaRepository.save(any(Sala.class))).thenReturn(sala);

        SalaResponseDTO result = salaService.atualizarSala(1L, dtoAtualizacao);

        assertNotNull(result);
        verify(salaRepository).findById(1L);
        verify(salaRepository).save(any(Sala.class));
    }

    @Test
    void atualizarSala_DeveAtualizarEspecialidades_QuandoFornecidas() {
        SalaDTO dtoAtualizacao = new SalaDTO();
        dtoAtualizacao.setEspecialidadeIds(Set.of(1L, 2L));

        Especialidade especialidade2 = new Especialidade();
        especialidade2.setId(2L);
        especialidade2.setNome("RPG");

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(especialidadeRepository.findById(2L)).thenReturn(Optional.of(especialidade2));
        when(salaRepository.save(any(Sala.class))).thenReturn(sala);

        salaService.atualizarSala(1L, dtoAtualizacao);

        verify(especialidadeRepository).findById(1L);
        verify(especialidadeRepository).findById(2L);
        verify(salaRepository).save(any(Sala.class));
    }

    @Test
    void atualizarSala_DeveLancarExcecao_QuandoEspecialidadeNaoEncontrada() {
        SalaDTO dtoAtualizacao = new SalaDTO();
        dtoAtualizacao.setEspecialidadeIds(Set.of(999L));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(especialidadeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            salaService.atualizarSala(1L, dtoAtualizacao);
        });
    }

    @Test
    void excluirSala_DeveExcluirComSucesso() {
        when(salaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(salaRepository).deleteById(1L);

        salaService.excluirSala(1L);

        verify(salaRepository).existsById(1L);
        verify(salaRepository).deleteById(1L);
    }

    @Test
    void excluirSala_DeveLancarExcecao_QuandoNaoEncontrado() {
        when(salaRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntidadeNaoEncontradaException.class, () -> {
            salaService.excluirSala(1L);
        });

        verify(salaRepository, never()).deleteById(any());
    }

    @Test
    void toResponseDTO_DeveConverterCorretamente() {
        SalaResponseDTO result = salaService.toResponseDTO(sala);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Sala Teste", result.getNome());
        assertEquals(10, result.getQuantidadeMaximaAlunos());
        assertEquals(2, result.getQuantidadeEquipamentosPCD());
        assertNotNull(result.getEspecialidades());
        assertTrue(result.getEspecialidades().contains("Pilates Clássico"));
    }
}

