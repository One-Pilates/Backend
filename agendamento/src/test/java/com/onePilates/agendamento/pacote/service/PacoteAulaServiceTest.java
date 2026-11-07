package com.onePilates.agendamento.pacote.service;

import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.especialidade.repository.EspecialidadeRepository;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.exception.OperacaoInvalidaException;
import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.AgendamentoAluno;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.model.MovimentoPacote;
import com.onePilates.agendamento.model.PacoteAula;
import com.onePilates.agendamento.model.enums.MotivoMovimentoPacote;
import com.onePilates.agendamento.pacote.dto.PacoteResponseDTO;
import com.onePilates.agendamento.pacote.dto.VerificacaoPacoteDTO;
import com.onePilates.agendamento.pacote.repository.MovimentoPacoteRepository;
import com.onePilates.agendamento.pacote.repository.PacoteAulaRepository;
import com.onePilates.agendamento.testsupport.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    private PacoteAulaRepository pacoteRepository;
    @Mock
    private MovimentoPacoteRepository movimentoRepository;
    @Mock
    private AlunoRepository alunoRepository;
    @Mock
    private EspecialidadeRepository especialidadeRepository;

    @InjectMocks
    private PacoteAulaService service;

    private Aluno ana;
    private Especialidade pilates;
    private Especialidade fisio;
    private AgendamentoAluno vinculo;

    @BeforeEach
    void setUp() {
        ana = Fixtures.aluno(1L, "Ana", "ana@teste.com");
        pilates = Fixtures.especialidade(10L, "Pilates");
        fisio = Fixtures.especialidade(11L, "Fisioterapia");
        Agendamento aula = Fixtures.aula(100L, LocalDateTime.now().plusDays(3), pilates,
                Fixtures.professor(5L, "Mariana"), Fixtures.sala(7L, "Sala 1"));
        vinculo = Fixtures.vincular(aula, ana);
    }

    private PacoteAula pacoteComSaldo(Especialidade esp, int saldo) {
        return new PacoteAula(ana, esp, saldo);
    }

    // ------------------------------------------------------------ definir saldo

    @Test
    void definirSaldo_gravaOSaldoERegistraAVariacao() {
        PacoteAula pacote = pacoteComSaldo(pilates, 4);
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(especialidadeRepository.findById(10L)).thenReturn(Optional.of(pilates));
        when(pacoteRepository.buscarComTrava(1L, 10L)).thenReturn(Optional.of(pacote));

        PacoteResponseDTO resposta = service.definirSaldo(1L, 10L, 12);

        assertEquals(12, pacote.getSaldo());
        assertEquals(12, resposta.saldo());
        ArgumentCaptor<MovimentoPacote> captor = ArgumentCaptor.forClass(MovimentoPacote.class);
        verify(movimentoRepository).save(captor.capture());
        assertEquals(8, captor.getValue().getVariacao());
        assertEquals(12, captor.getValue().getSaldoApos());
        assertEquals(MotivoMovimentoPacote.AJUSTE_MANUAL, captor.getValue().getMotivo());
    }

    @Test
    void definirSaldo_aceitaDiminuirOPacote() {
        PacoteAula pacote = pacoteComSaldo(pilates, 10);
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(especialidadeRepository.findById(10L)).thenReturn(Optional.of(pilates));
        when(pacoteRepository.buscarComTrava(1L, 10L)).thenReturn(Optional.of(pacote));

        service.definirSaldo(1L, 10L, 3);

        assertEquals(3, pacote.getSaldo());
        ArgumentCaptor<MovimentoPacote> captor = ArgumentCaptor.forClass(MovimentoPacote.class);
        verify(movimentoRepository).save(captor.capture());
        assertEquals(-7, captor.getValue().getVariacao());
    }

    @Test
    void definirSaldo_semMudancaNaoRegistraMovimento() {
        PacoteAula pacote = pacoteComSaldo(pilates, 5);
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(especialidadeRepository.findById(10L)).thenReturn(Optional.of(pilates));
        when(pacoteRepository.buscarComTrava(1L, 10L)).thenReturn(Optional.of(pacote));

        service.definirSaldo(1L, 10L, 5);

        verify(movimentoRepository, never()).save(any());
    }

    @Test
    void definirSaldo_criaOPacoteQuandoAindaNaoExiste() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(especialidadeRepository.findById(10L)).thenReturn(Optional.of(pilates));
        when(pacoteRepository.buscarComTrava(1L, 10L)).thenReturn(Optional.empty());
        when(pacoteRepository.save(any(PacoteAula.class))).thenAnswer(inv -> inv.getArgument(0));

        PacoteResponseDTO resposta = service.definirSaldo(1L, 10L, 8);

        assertEquals(8, resposta.saldo());
        ArgumentCaptor<MovimentoPacote> captor = ArgumentCaptor.forClass(MovimentoPacote.class);
        verify(movimentoRepository).save(captor.capture());
        assertEquals(8, captor.getValue().getVariacao());
    }

    @Test
    void definirSaldo_recusaValorNegativoOuNulo() {
        assertThrows(OperacaoInvalidaException.class, () -> service.definirSaldo(1L, 10L, -1));
        assertThrows(OperacaoInvalidaException.class, () -> service.definirSaldo(1L, 10L, null));
        verify(pacoteRepository, never()).save(any());
    }

    @Test
    void definirSaldo_alunoInexistente() {
        when(alunoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.definirSaldo(99L, 10L, 5));
    }

    // ----------------------------------------------------------------- débito

    @Test
    void debitar_tiraUmaAulaMarcaOVinculoERegistra() {
        PacoteAula pacote = pacoteComSaldo(pilates, 3);
        when(pacoteRepository.buscarComTrava(1L, 10L)).thenReturn(Optional.of(pacote));

        service.debitar(vinculo);

        assertEquals(2, pacote.getSaldo());
        assertTrue(vinculo.isCreditoDebitado());
        ArgumentCaptor<MovimentoPacote> captor = ArgumentCaptor.forClass(MovimentoPacote.class);
        verify(movimentoRepository).save(captor.capture());
        assertEquals(-1, captor.getValue().getVariacao());
        assertEquals(2, captor.getValue().getSaldoApos());
        assertEquals(MotivoMovimentoPacote.AGENDAMENTO, captor.getValue().getMotivo());
        assertEquals(100L, captor.getValue().getAgendamentoId());
    }

    @Test
    void debitar_semSaldoNaoBloqueiaESaldoFicaNegativo() {
        PacoteAula pacote = pacoteComSaldo(pilates, 0);
        when(pacoteRepository.buscarComTrava(1L, 10L)).thenReturn(Optional.of(pacote));

        service.debitar(vinculo);

        assertEquals(-1, pacote.getSaldo());
        assertTrue(vinculo.isCreditoDebitado());
    }

    @Test
    void debitar_semPacoteCadastradoCriaComZeroEFicaNegativo() {
        when(pacoteRepository.buscarComTrava(1L, 10L)).thenReturn(Optional.empty());
        PacoteAula criado = pacoteComSaldo(pilates, 0);
        when(pacoteRepository.save(any(PacoteAula.class))).thenReturn(criado);

        service.debitar(vinculo);

        assertEquals(-1, criado.getSaldo());
    }

    @Test
    void debitar_naoDebitaDuasVezesOMesmoVinculo() {
        vinculo.setCreditoDebitado(true);

        service.debitar(vinculo);

        verify(pacoteRepository, never()).buscarComTrava(any(), any());
        verify(movimentoRepository, never()).save(any());
    }

    // -------------------------------------------------------------- devolução

    @Test
    void devolver_voltaUmaAulaQuandoOVinculoTinhaDebito() {
        PacoteAula pacote = pacoteComSaldo(pilates, 2);
        vinculo.setCreditoDebitado(true);
        when(pacoteRepository.buscarComTrava(1L, 10L)).thenReturn(Optional.of(pacote));

        boolean devolveu = service.devolver(vinculo, MotivoMovimentoPacote.CANCELAMENTO_ALUNO_COM_DEVOLUCAO);

        assertTrue(devolveu);
        assertEquals(3, pacote.getSaldo());
        assertFalse(vinculo.isCreditoDebitado());
        ArgumentCaptor<MovimentoPacote> captor = ArgumentCaptor.forClass(MovimentoPacote.class);
        verify(movimentoRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getVariacao());
        assertEquals(MotivoMovimentoPacote.CANCELAMENTO_ALUNO_COM_DEVOLUCAO, captor.getValue().getMotivo());
    }

    @Test
    void devolver_vinculoSemDebitoNaoMexeNoSaldo() {
        boolean devolveu = service.devolver(vinculo, MotivoMovimentoPacote.DEVOLUCAO_EQUIPE);

        assertFalse(devolveu);
        verify(pacoteRepository, never()).buscarComTrava(any(), any());
        verify(movimentoRepository, never()).save(any());
    }

    @Test
    void devolver_comEspecialidadeExplicitaDevolveNaAntiga() {
        PacoteAula pacoteFisio = pacoteComSaldo(fisio, 0);
        vinculo.setCreditoDebitado(true);
        when(pacoteRepository.buscarComTrava(1L, 11L)).thenReturn(Optional.of(pacoteFisio));

        boolean devolveu = service.devolver(vinculo, fisio, MotivoMovimentoPacote.DEVOLUCAO_EQUIPE);

        assertTrue(devolveu);
        assertEquals(1, pacoteFisio.getSaldo());
    }

    // ----------------------------------------------------------------- avisos

    @Test
    void avisosDeExcedente_listaSoQuemFicouComSaldoNegativo() {
        Aluno bia = Fixtures.aluno(2L, "Bia", "bia@teste.com");
        AgendamentoAluno vinculoBia = Fixtures.vincular(vinculo.getAgendamento(), bia);
        vinculo.setCreditoDebitado(true);
        vinculoBia.setCreditoDebitado(true);
        when(pacoteRepository.findByAlunoIdAndEspecialidadeId(1L, 10L))
                .thenReturn(Optional.of(new PacoteAula(ana, pilates, -2)));
        when(pacoteRepository.findByAlunoIdAndEspecialidadeId(2L, 10L))
                .thenReturn(Optional.of(new PacoteAula(bia, pilates, 4)));

        List<String> avisos = service.avisosDeExcedente(List.of(vinculo, vinculoBia));

        assertEquals(1, avisos.size());
        assertTrue(avisos.get(0).contains("Ana"));
        assertTrue(avisos.get(0).contains("Pilates"));
        assertTrue(avisos.get(0).contains("-2"));
    }

    @Test
    void verificar_avisaQuemNaoTemAulaDisponivelSemBloquear() {
        Aluno bia = Fixtures.aluno(2L, "Bia", "bia@teste.com");
        when(especialidadeRepository.findById(10L)).thenReturn(Optional.of(pilates));
        when(pacoteRepository.findByAlunoIdIn(any())).thenReturn(List.of(new PacoteAula(bia, pilates, 3)));
        when(alunoRepository.findAllById(any())).thenReturn(List.of(ana, bia));

        List<VerificacaoPacoteDTO> resultado = service.verificar(List.of(1L, 2L), 10L);

        VerificacaoPacoteDTO daAna = resultado.stream().filter(v -> v.alunoId().equals(1L)).findFirst().orElseThrow();
        VerificacaoPacoteDTO daBia = resultado.stream().filter(v -> v.alunoId().equals(2L)).findFirst().orElseThrow();
        assertTrue(daAna.excedeUmPacote());
        assertEquals(0, daAna.saldoAtual());
        assertTrue(daAna.mensagem().contains("Ana"));
        assertFalse(daBia.excedeUmPacote());
        assertEquals(3, daBia.saldoAtual());
    }

    @Test
    void verificar_saldoUmAindaCobreAAula() {
        when(especialidadeRepository.findById(10L)).thenReturn(Optional.of(pilates));
        when(pacoteRepository.findByAlunoIdIn(any())).thenReturn(List.of(new PacoteAula(ana, pilates, 1)));
        when(alunoRepository.findAllById(any())).thenReturn(List.of(ana));

        VerificacaoPacoteDTO resultado = service.verificar(Set.of(1L), 10L).get(0);

        assertFalse(resultado.excedeUmPacote());
    }

    // ------------------------------------------------------------------ listar

    @Test
    void listar_mostraTodasAsEspecialidadesComZeroParaAsSemPacote() {
        when(alunoRepository.existsById(1L)).thenReturn(true);
        when(pacoteRepository.findByAlunoId(1L)).thenReturn(List.of(new PacoteAula(ana, pilates, 6)));
        when(especialidadeRepository.findAll()).thenReturn(List.of(pilates, fisio));

        List<PacoteResponseDTO> lista = service.listar(1L);

        assertEquals(2, lista.size());
        assertEquals("Fisioterapia", lista.get(0).especialidade());
        assertEquals(0, lista.get(0).saldo());
        assertEquals("Pilates", lista.get(1).especialidade());
        assertEquals(6, lista.get(1).saldo());
    }

    @Test
    void listar_alunoInexistente() {
        when(alunoRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.listar(99L));
    }

    @Test
    void removerDadosDoAluno_apagaMovimentosEPacotes() {
        service.removerDadosDoAluno(1L);

        verify(movimentoRepository).deleteByAlunoId(1L);
        verify(pacoteRepository).deleteByAlunoId(1L);
    }
}
