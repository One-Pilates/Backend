package com.onePilates.agendamento.aluno.service;

import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.aluno.repository.PacoteAulaRepository;
import com.onePilates.agendamento.especialidade.repository.EspecialidadeRepository;
import com.onePilates.agendamento.exception.BusinessException;
import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.AgendamentoAluno;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.model.PacoteAula;
import com.onePilates.agendamento.model.enums.StatusPresenca;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PacoteAulaServiceTest {

    @Mock
    private PacoteAulaRepository pacoteAulaRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private EspecialidadeRepository especialidadeRepository;

    @InjectMocks
    private PacoteAulaService pacoteAulaService;

    private Aluno aluno;
    private Especialidade pilates;
    private Especialidade fisio;

    @BeforeEach
    void setUp() {
        aluno = new Aluno();
        aluno.setId(1L);
        pilates = new Especialidade();
        pilates.setId(2L);
        pilates.setNome("Pilates");
        fisio = new Especialidade();
        fisio.setId(3L);
        fisio.setNome("Fisioterapia");
    }

    @Test
    void definirPacote_DeveCriar_QuandoNaoExiste() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(especialidadeRepository.findById(2L)).thenReturn(Optional.of(pilates));
        when(pacoteAulaRepository.findByAlunoIdAndEspecialidadeId(1L, 2L)).thenReturn(Optional.empty());
        when(pacoteAulaRepository.save(any(PacoteAula.class))).thenAnswer(inv -> inv.getArgument(0));

        PacoteAula salvo = pacoteAulaService.definir(1L, 2L, 10);

        assertEquals(10, salvo.getAulasRemanescentes());
        assertEquals(aluno, salvo.getAluno());
        assertEquals(pilates, salvo.getEspecialidade());
    }

    @Test
    void definirPacote_DeveSubstituirSaldo() {
        PacoteAula existente = pacote(aluno, pilates, 10);
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(especialidadeRepository.findById(2L)).thenReturn(Optional.of(pilates));
        when(pacoteAulaRepository.findByAlunoIdAndEspecialidadeId(1L, 2L)).thenReturn(Optional.of(existente));
        when(pacoteAulaRepository.save(any(PacoteAula.class))).thenAnswer(inv -> inv.getArgument(0));

        PacoteAula salvo = pacoteAulaService.definir(1L, 2L, 4);

        assertEquals(4, salvo.getAulasRemanescentes());
    }

    @Test
    void definirPacote_DeveAceitarZero() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(especialidadeRepository.findById(2L)).thenReturn(Optional.of(pilates));
        when(pacoteAulaRepository.findByAlunoIdAndEspecialidadeId(1L, 2L)).thenReturn(Optional.empty());
        when(pacoteAulaRepository.save(any(PacoteAula.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(0, pacoteAulaService.definir(1L, 2L, 0).getAulasRemanescentes());
    }

    @Test
    void definirPacote_DeveRejeitarNegativo() {
        assertThrows(BusinessException.class, () -> pacoteAulaService.definir(1L, 2L, -1));
        verify(pacoteAulaRepository, never()).save(any());
    }

    @Test
    void debitarPresenca_DeveBaixarUmDaEspecialidadeDaAula() {
        PacoteAula saldo = pacote(aluno, pilates, 10);
        AgendamentoAluno vinculo = vinculo(aluno, pilates, StatusPresenca.PRESENTE, false);
        when(pacoteAulaRepository.findByAlunoIdAndEspecialidadeId(1L, 2L)).thenReturn(Optional.of(saldo));

        pacoteAulaService.aplicarPresenca(vinculo);

        assertEquals(9, saldo.getAulasRemanescentes());
        assertTrue(vinculo.getCreditoConsumido());
    }

    @Test
    void debitarPresenca_NaoDeveDebitarDeNovo_QuandoJaConsumido() {
        PacoteAula saldo = pacote(aluno, pilates, 10);
        AgendamentoAluno vinculo = vinculo(aluno, pilates, StatusPresenca.PRESENTE, true);

        pacoteAulaService.aplicarPresenca(vinculo);

        assertEquals(10, saldo.getAulasRemanescentes());
        verify(pacoteAulaRepository, never()).save(any());
    }

    @Test
    void debitarPresenca_NaoDeveDebitar_QuandoFalta() {
        PacoteAula saldo = pacote(aluno, pilates, 10);
        AgendamentoAluno vinculo = vinculo(aluno, pilates, StatusPresenca.FALTA, false);

        pacoteAulaService.aplicarPresenca(vinculo);

        assertEquals(10, saldo.getAulasRemanescentes());
        assertFalse(Boolean.TRUE.equals(vinculo.getCreditoConsumido()));
        verify(pacoteAulaRepository, never()).save(any());
    }

    @Test
    void debitarPresenca_NaoDeveDebitar_QuandoPendente() {
        PacoteAula saldo = pacote(aluno, pilates, 10);
        AgendamentoAluno vinculo = vinculo(aluno, pilates, StatusPresenca.PENDENTE, false);

        pacoteAulaService.aplicarPresenca(vinculo);

        assertEquals(10, saldo.getAulasRemanescentes());
        verify(pacoteAulaRepository, never()).save(any());
    }

    @Test
    void debitarPresenca_NaoAlteraSaldo_QuandoNaoHaPacote() {
        AgendamentoAluno vinculo = vinculo(aluno, pilates, StatusPresenca.PRESENTE, false);
        when(pacoteAulaRepository.findByAlunoIdAndEspecialidadeId(1L, 2L)).thenReturn(Optional.empty());

        pacoteAulaService.aplicarPresenca(vinculo);

        assertFalse(Boolean.TRUE.equals(vinculo.getCreditoConsumido()));
        verify(pacoteAulaRepository, never()).save(any());
    }

    @Test
    void debitarPresenca_NaoFicaNegativo_QuandoSaldoZero() {
        PacoteAula saldo = pacote(aluno, pilates, 0);
        AgendamentoAluno vinculo = vinculo(aluno, pilates, StatusPresenca.PRESENTE, false);
        when(pacoteAulaRepository.findByAlunoIdAndEspecialidadeId(1L, 2L)).thenReturn(Optional.of(saldo));

        pacoteAulaService.aplicarPresenca(vinculo);

        assertEquals(0, saldo.getAulasRemanescentes());
        assertFalse(Boolean.TRUE.equals(vinculo.getCreditoConsumido()));
        verify(pacoteAulaRepository, never()).save(any());
    }

    @Test
    void estornar_DeveDevolverUm_QuandoCreditoFoiConsumido() {
        PacoteAula saldo = pacote(aluno, pilates, 9);
        AgendamentoAluno vinculo = vinculo(aluno, pilates, StatusPresenca.PRESENTE, true);
        when(pacoteAulaRepository.findByAlunoIdAndEspecialidadeId(1L, 2L)).thenReturn(Optional.of(saldo));

        pacoteAulaService.estornar(vinculo);

        assertEquals(10, saldo.getAulasRemanescentes());
        assertFalse(vinculo.getCreditoConsumido());
    }

    @Test
    void estornar_NaoMexeNoSaldo_QuandoNaoHouveDebito() {
        PacoteAula saldo = pacote(aluno, pilates, 9);
        AgendamentoAluno vinculo = vinculo(aluno, pilates, StatusPresenca.PRESENTE, false);

        pacoteAulaService.estornar(vinculo);

        assertEquals(9, saldo.getAulasRemanescentes());
        verify(pacoteAulaRepository, never()).save(any());
    }

    @Test
    void trocarEspecialidade_DeveEstornarAntigaEDebitarNova_SoDosPresentes() {
        PacoteAula saldoAntigo = pacote(aluno, pilates, 5);
        PacoteAula saldoNovo = pacote(aluno, fisio, 3);
        AgendamentoAluno presente = vinculo(aluno, pilates, StatusPresenca.PRESENTE, true);
        Aluno outro = new Aluno();
        outro.setId(8L);
        AgendamentoAluno falta = vinculo(outro, pilates, StatusPresenca.FALTA, false);
        when(pacoteAulaRepository.findByAlunoIdAndEspecialidadeId(1L, 2L)).thenReturn(Optional.of(saldoAntigo));
        when(pacoteAulaRepository.findByAlunoIdAndEspecialidadeId(1L, 3L)).thenReturn(Optional.of(saldoNovo));

        pacoteAulaService.trocarEspecialidade(List.of(presente, falta), pilates, fisio);

        assertEquals(6, saldoAntigo.getAulasRemanescentes());
        assertEquals(2, saldoNovo.getAulasRemanescentes());
        assertTrue(presente.getCreditoConsumido());
        assertFalse(Boolean.TRUE.equals(falta.getCreditoConsumido()));
    }

    private PacoteAula pacote(Aluno dono, Especialidade especialidade, int saldo) {
        PacoteAula pacote = new PacoteAula();
        pacote.setAluno(dono);
        pacote.setEspecialidade(especialidade);
        pacote.setAulasRemanescentes(saldo);
        return pacote;
    }

    private AgendamentoAluno vinculo(Aluno dono, Especialidade especialidade, StatusPresenca status, boolean consumido) {
        Agendamento agendamento = new Agendamento();
        agendamento.setEspecialidade(especialidade);
        AgendamentoAluno vinculo = new AgendamentoAluno(agendamento, dono);
        vinculo.setStatusPresenca(status);
        vinculo.setCreditoConsumido(consumido);
        return vinculo;
    }
}
