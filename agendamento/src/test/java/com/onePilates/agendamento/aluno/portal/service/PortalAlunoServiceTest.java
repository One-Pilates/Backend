package com.onePilates.agendamento.aluno.portal.service;

import com.onePilates.agendamento.agendamento.repository.AgendamentoAlunoRepository;
import com.onePilates.agendamento.agendamento.repository.AgendamentoRepository;
import com.onePilates.agendamento.agendamento.service.AgendamentoService;
import com.onePilates.agendamento.aluno.portal.dto.AtualizarPerfilAlunoDTO;
import com.onePilates.agendamento.aluno.portal.dto.AulaAlunoDTO;
import com.onePilates.agendamento.aluno.portal.dto.CancelamentoResultadoDTO;
import com.onePilates.agendamento.aluno.portal.dto.DashboardAlunoDTO;
import com.onePilates.agendamento.aluno.portal.dto.PerfilAlunoDTO;
import com.onePilates.agendamento.aluno.portal.dto.SituacaoAulaAluno;
import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.aluno.repository.CancelamentoAulaRepository;
import com.onePilates.agendamento.email.dto.AulaCanceladaAlunoEmailDTO;
import com.onePilates.agendamento.email.dto.EmailRequestDTO;
import com.onePilates.agendamento.email.service.RabbitMQProducer;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.exception.OperacaoInvalidaException;
import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.AgendamentoAluno;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.CancelamentoAula;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.model.Professor;
import com.onePilates.agendamento.model.Sala;
import com.onePilates.agendamento.model.enums.MotivoMovimentoPacote;
import com.onePilates.agendamento.model.enums.TipoEmail;
import com.onePilates.agendamento.pacote.dto.PacoteResponseDTO;
import com.onePilates.agendamento.pacote.service.PacoteAulaService;
import com.onePilates.agendamento.testsupport.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortalAlunoServiceTest {

    /** Segunda-feira, 10h. */
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 9, 10, 0);
    private static final String EMAIL = "ana@teste.com";

    @Mock
    private AlunoRepository alunoRepository;
    @Mock
    private AgendamentoAlunoRepository agendamentoAlunoRepository;
    @Mock
    private AgendamentoRepository agendamentoRepository;
    @Mock
    private CancelamentoAulaRepository cancelamentoRepository;
    @Mock
    private PacoteAulaService pacoteService;
    @Mock
    private AgendamentoService agendamentoService;
    @Mock
    private RabbitMQProducer rabbitMQ;

    @InjectMocks
    private PortalAlunoService service;

    private Aluno ana;
    private Especialidade pilates;
    private Professor professora;
    private Sala sala;

    @BeforeEach
    void setUp() {
        ana = Fixtures.aluno(1L, "Ana", EMAIL);
        pilates = Fixtures.especialidade(10L, "Pilates");
        professora = Fixtures.professor(5L, "Mariana");
        sala = Fixtures.sala(7L, "Sala Pilates 1");
    }

    private void alunoLogado() {
        when(alunoRepository.findByEmail(EMAIL)).thenReturn(Optional.of(ana));
    }

    private AgendamentoAluno aulaDeAna(Long id, LocalDateTime quando) {
        Agendamento aula = Fixtures.aula(id, quando, pilates, professora, sala);
        return Fixtures.vincular(aula, ana);
    }

    private void anaTemAAula(AgendamentoAluno vinculo) {
        when(agendamentoAlunoRepository.findByAlunoIdAndAgendamentoId(1L, vinculo.getAgendamento().getId()))
                .thenReturn(Optional.of(vinculo));
    }

    private CancelamentoAula cancelamentoSalvo() {
        ArgumentCaptor<CancelamentoAula> captor = ArgumentCaptor.forClass(CancelamentoAula.class);
        verify(cancelamentoRepository).save(captor.capture());
        return captor.getValue();
    }

    // ------------------------------------------------------------ cancelamento

    @Test
    void cancelar_com48hDevolveAAulaAoPacote() {
        alunoLogado();
        AgendamentoAluno vinculo = aulaDeAna(100L, AGORA.plusHours(48));
        vinculo.setCreditoDebitado(true);
        Fixtures.vincular(vinculo.getAgendamento(), Fixtures.aluno(2L, "Bia", "bia@teste.com"));
        anaTemAAula(vinculo);
        when(pacoteService.devolver(vinculo, MotivoMovimentoPacote.CANCELAMENTO_ALUNO_COM_DEVOLUCAO)).thenReturn(true);

        CancelamentoResultadoDTO resultado = service.cancelar(EMAIL, 100L, AGORA);

        assertTrue(resultado.aulaDevolvida());
        assertFalse(resultado.foraDoPrazo());
        CancelamentoAula registro = cancelamentoSalvo();
        assertTrue(registro.isCreditoDevolvido());
        assertFalse(registro.isForaDoPrazo());
        assertEquals("Pilates", registro.getEspecialidade());
        assertFalse(vinculo.getAgendamento().getAgendamentoAlunos().contains(vinculo));
    }

    @Test
    void cancelar_comExatamente24hAindaDevolve() {
        alunoLogado();
        AgendamentoAluno vinculo = aulaDeAna(100L, AGORA.plusHours(24));
        vinculo.setCreditoDebitado(true);
        Fixtures.vincular(vinculo.getAgendamento(), Fixtures.aluno(2L, "Bia", "bia@teste.com"));
        anaTemAAula(vinculo);
        when(pacoteService.devolver(vinculo, MotivoMovimentoPacote.CANCELAMENTO_ALUNO_COM_DEVOLUCAO)).thenReturn(true);

        CancelamentoResultadoDTO resultado = service.cancelar(EMAIL, 100L, AGORA);

        assertTrue(resultado.aulaDevolvida());
        assertFalse(resultado.foraDoPrazo());
    }

    @Test
    void cancelar_comMenosDe24hPermiteMasNaoDevolve() {
        alunoLogado();
        AgendamentoAluno vinculo = aulaDeAna(100L, AGORA.plusHours(24).minusMinutes(1));
        vinculo.setCreditoDebitado(true);
        Fixtures.vincular(vinculo.getAgendamento(), Fixtures.aluno(2L, "Bia", "bia@teste.com"));
        anaTemAAula(vinculo);

        CancelamentoResultadoDTO resultado = service.cancelar(EMAIL, 100L, AGORA);

        assertFalse(resultado.aulaDevolvida());
        assertTrue(resultado.foraDoPrazo());
        assertTrue(resultado.mensagem().contains("descontada"));
        verify(pacoteService, never()).devolver(any(AgendamentoAluno.class), any(MotivoMovimentoPacote.class));
        CancelamentoAula registro = cancelamentoSalvo();
        assertFalse(registro.isCreditoDevolvido());
        assertTrue(registro.isForaDoPrazo());
        assertFalse(vinculo.getAgendamento().getAgendamentoAlunos().contains(vinculo));
    }

    @Test
    void cancelar_aulaQueJaComecouNaoPodeSerCancelada() {
        alunoLogado();
        AgendamentoAluno vinculo = aulaDeAna(100L, AGORA.minusMinutes(1));
        anaTemAAula(vinculo);

        assertThrows(OperacaoInvalidaException.class, () -> service.cancelar(EMAIL, 100L, AGORA));

        verify(cancelamentoRepository, never()).save(any());
        verify(pacoteService, never()).devolver(any(AgendamentoAluno.class), any(MotivoMovimentoPacote.class));
        assertTrue(vinculo.getAgendamento().getAgendamentoAlunos().contains(vinculo));
    }

    @Test
    void cancelar_aulaDeOutroAlunoOuInexistenteRespondeNaoEncontrada() {
        alunoLogado();
        when(agendamentoAlunoRepository.findByAlunoIdAndAgendamentoId(1L, 555L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.cancelar(EMAIL, 555L, AGORA));

        verify(cancelamentoRepository, never()).save(any());
    }

    @Test
    void cancelar_seForOUnicoAlunoARemoveAAulaInteira() {
        alunoLogado();
        AgendamentoAluno vinculo = aulaDeAna(100L, AGORA.plusDays(3));
        anaTemAAula(vinculo);

        service.cancelar(EMAIL, 100L, AGORA);

        verify(agendamentoService).excluirAgendamento(100L);
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    void cancelar_seRestamOutrosAlunosAAulaFicaEOProfessorEAvisado() {
        alunoLogado();
        AgendamentoAluno vinculo = aulaDeAna(100L, AGORA.plusDays(3));
        Fixtures.vincular(vinculo.getAgendamento(), Fixtures.aluno(2L, "Bia", "bia@teste.com"));
        anaTemAAula(vinculo);

        service.cancelar(EMAIL, 100L, AGORA);

        verify(agendamentoService, never()).excluirAgendamento(any());
        verify(agendamentoRepository).save(vinculo.getAgendamento());
        ArgumentCaptor<EmailRequestDTO> captor = ArgumentCaptor.forClass(EmailRequestDTO.class);
        verify(rabbitMQ, org.mockito.Mockito.atLeastOnce()).enviarPraFilaDeEmails(captor.capture());
        EmailRequestDTO paraProfessor = captor.getAllValues().stream()
                .filter(e -> e.getTypeEmail() == TipoEmail.AULA_ATUALIZADA).findFirst().orElseThrow();
        assertEquals("prof5@teste.com", paraProfessor.getDestinatario());
    }

    @Test
    void cancelar_professorSemNotificacaoNaoRecebeEmail() {
        alunoLogado();
        professora.setNotificacaoAtiva(false);
        ana.setNotificacaoAtiva(false);
        AgendamentoAluno vinculo = aulaDeAna(100L, AGORA.plusDays(3));
        Fixtures.vincular(vinculo.getAgendamento(), Fixtures.aluno(2L, "Bia", "bia@teste.com"));
        anaTemAAula(vinculo);

        service.cancelar(EMAIL, 100L, AGORA);

        verify(rabbitMQ, never()).enviarPraFilaDeEmails(any());
    }

    @Test
    void cancelar_enviaEmailAoAlunoDizendoSeAAulaFoiPerdida() {
        alunoLogado();
        AgendamentoAluno vinculo = aulaDeAna(100L, AGORA.plusHours(5));
        vinculo.setCreditoDebitado(true);
        professora.setNotificacaoAtiva(false);
        Fixtures.vincular(vinculo.getAgendamento(), Fixtures.aluno(2L, "Bia", "bia@teste.com"));
        anaTemAAula(vinculo);

        service.cancelar(EMAIL, 100L, AGORA);

        ArgumentCaptor<EmailRequestDTO> captor = ArgumentCaptor.forClass(EmailRequestDTO.class);
        verify(rabbitMQ).enviarPraFilaDeEmails(captor.capture());
        EmailRequestDTO email = captor.getValue();
        assertEquals(TipoEmail.AULA_CANCELADA_ALUNO, email.getTypeEmail());
        assertEquals(EMAIL, email.getDestinatario());
        AulaCanceladaAlunoEmailDTO payload = (AulaCanceladaAlunoEmailDTO) email.getPayload();
        assertTrue(payload.getCanceladoPeloAluno());
        assertEquals(Boolean.FALSE, payload.getAulaDevolvidaAoPacote());
    }

    @Test
    void cancelar_falhaNoEnvioDeEmailNaoDesfazOCancelamento() {
        alunoLogado();
        AgendamentoAluno vinculo = aulaDeAna(100L, AGORA.plusDays(3));
        Fixtures.vincular(vinculo.getAgendamento(), Fixtures.aluno(2L, "Bia", "bia@teste.com"));
        anaTemAAula(vinculo);
        org.mockito.Mockito.doThrow(new RuntimeException("fila fora do ar")).when(rabbitMQ).enviarPraFilaDeEmails(any());

        CancelamentoResultadoDTO resultado = service.cancelar(EMAIL, 100L, AGORA);

        assertNotNull(resultado);
        verify(cancelamentoRepository).save(any(CancelamentoAula.class));
    }

    // -------------------------------------------------------- confirmar presença

    @Test
    void confirmarPresenca_marcaAConfirmacao() {
        alunoLogado();
        AgendamentoAluno vinculo = aulaDeAna(100L, AGORA.plusDays(2));
        anaTemAAula(vinculo);

        service.confirmarPresenca(EMAIL, 100L, AGORA);

        assertTrue(vinculo.isPresencaConfirmada());
        assertNotNull(vinculo.getDataConfirmacao());
        verify(agendamentoAlunoRepository).save(vinculo);
    }

    @Test
    void confirmarPresenca_repetirNaoAlteraADataDaPrimeiraConfirmacao() {
        alunoLogado();
        AgendamentoAluno vinculo = aulaDeAna(100L, AGORA.plusDays(2));
        vinculo.confirmarPresenca();
        LocalDateTime primeira = vinculo.getDataConfirmacao();
        anaTemAAula(vinculo);

        service.confirmarPresenca(EMAIL, 100L, AGORA);

        assertEquals(primeira, vinculo.getDataConfirmacao());
        verify(agendamentoAlunoRepository, never()).save(any());
    }

    @Test
    void confirmarPresenca_aulaQueJaPassouEhRecusada() {
        alunoLogado();
        AgendamentoAluno vinculo = aulaDeAna(100L, AGORA.minusHours(2));
        anaTemAAula(vinculo);

        assertThrows(OperacaoInvalidaException.class, () -> service.confirmarPresenca(EMAIL, 100L, AGORA));

        assertFalse(vinculo.isPresencaConfirmada());
    }

    @Test
    void confirmarPresenca_aulaDeOutroAlunoRespondeNaoEncontrada() {
        alunoLogado();
        when(agendamentoAlunoRepository.findByAlunoIdAndAgendamentoId(1L, 777L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.confirmarPresenca(EMAIL, 777L, AGORA));
    }

    // ---------------------------------------------------------------- listagem

    @Test
    void aulas_juntaAgendadasRealizadasECanceladasEmOrdemDeData() {
        alunoLogado();
        AgendamentoAluno passada = aulaDeAna(1L, AGORA.minusDays(5));
        AgendamentoAluno futura = aulaDeAna(2L, AGORA.plusDays(2));
        Agendamento aulaCancelada = Fixtures.aula(3L, AGORA.minusDays(2), pilates, professora, sala);
        CancelamentoAula cancelada = new CancelamentoAula(ana, aulaCancelada, false, true);
        when(agendamentoAlunoRepository.buscarDoAlunoNoPeriodo(eq(1L), any(), any())).thenReturn(List.of(passada, futura));
        when(cancelamentoRepository.findByAlunoIdAndDataHoraAulaBetweenOrderByDataHoraAulaAsc(eq(1L), any(), any()))
                .thenReturn(List.of(cancelada));

        List<AulaAlunoDTO> aulas = service.aulas(EMAIL, null, null, AGORA);

        assertEquals(3, aulas.size());
        assertEquals(SituacaoAulaAluno.REALIZADA, aulas.get(0).situacao());
        assertEquals(SituacaoAulaAluno.CANCELADA, aulas.get(1).situacao());
        assertEquals(SituacaoAulaAluno.AGENDADA, aulas.get(2).situacao());
        assertEquals(Boolean.TRUE, aulas.get(1).foraDoPrazo());
        assertEquals(Boolean.FALSE, aulas.get(1).aulaDevolvida());
        assertFalse(aulas.get(0).podeCancelar());
        assertTrue(aulas.get(2).podeCancelar());
        assertEquals(60, aulas.get(2).duracaoMinutos());
        assertEquals("Mariana", aulas.get(2).professor());
    }

    @Test
    void aulas_indicaSeCancelarAgoraDevolveAAula() {
        alunoLogado();
        AgendamentoAluno daquiA10h = aulaDeAna(1L, AGORA.plusHours(10));
        AgendamentoAluno daquiA30h = aulaDeAna(2L, AGORA.plusHours(30));
        when(agendamentoAlunoRepository.buscarDoAlunoNoPeriodo(eq(1L), any(), any())).thenReturn(List.of(daquiA10h, daquiA30h));
        when(cancelamentoRepository.findByAlunoIdAndDataHoraAulaBetweenOrderByDataHoraAulaAsc(eq(1L), any(), any()))
                .thenReturn(List.of());

        List<AulaAlunoDTO> aulas = service.aulas(EMAIL, null, null, AGORA);

        assertFalse(aulas.get(0).cancelamentoSemPerda());
        assertTrue(aulas.get(1).cancelamentoSemPerda());
    }

    @Test
    void aulas_usaOIntervaloInformadoIncluindoOUltimoDia() {
        alunoLogado();
        when(agendamentoAlunoRepository.buscarDoAlunoNoPeriodo(eq(1L), any(), any())).thenReturn(List.of());
        when(cancelamentoRepository.findByAlunoIdAndDataHoraAulaBetweenOrderByDataHoraAulaAsc(eq(1L), any(), any()))
                .thenReturn(List.of());

        service.aulas(EMAIL, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), AGORA);

        ArgumentCaptor<LocalDateTime> inicio = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> fim = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(agendamentoAlunoRepository).buscarDoAlunoNoPeriodo(eq(1L), inicio.capture(), fim.capture());
        assertEquals(LocalDateTime.of(2026, 11, 1, 0, 0), inicio.getValue());
        assertTrue(fim.getValue().isAfter(LocalDateTime.of(2026, 11, 30, 23, 59)));
        assertTrue(fim.getValue().isBefore(LocalDateTime.of(2026, 12, 1, 0, 0)));
    }

    @Test
    void aulas_recusaIntervaloInvertidoOuGrandeDemais() {
        alunoLogado();

        assertThrows(OperacaoInvalidaException.class,
                () -> service.aulas(EMAIL, LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 1), AGORA));
        assertThrows(OperacaoInvalidaException.class,
                () -> service.aulas(EMAIL, LocalDate.of(2024, 1, 1), LocalDate.of(2026, 11, 1), AGORA));
    }

    // --------------------------------------------------------------- dashboard

    @Test
    void dashboard_contaRealizadasCanceladasEEspecialidadeMaisFrequente() {
        alunoLogado();
        Especialidade fisio = Fixtures.especialidade(11L, "Fisioterapia");
        // segunda 02/11 e segunda 26/10: Pilates; quarta 04/11: Fisioterapia; aula futura não conta como realizada
        AgendamentoAluno p1 = aulaDeAna(1L, LocalDateTime.of(2026, 11, 2, 9, 0));
        AgendamentoAluno p2 = aulaDeAna(2L, LocalDateTime.of(2026, 10, 26, 9, 0));
        Agendamento aulaFisio = Fixtures.aula(3L, LocalDateTime.of(2026, 11, 4, 14, 0), fisio, professora, sala);
        AgendamentoAluno f1 = Fixtures.vincular(aulaFisio, ana);
        AgendamentoAluno futura = aulaDeAna(4L, AGORA.plusDays(2));
        when(agendamentoAlunoRepository.buscarDoAlunoNoPeriodo(eq(1L), eq(LocalDate.of(2026, 10, 11).atStartOfDay()), any()))
                .thenReturn(List.of(p2, p1, f1, futura));
        when(agendamentoAlunoRepository.buscarDoAlunoNoPeriodo(eq(1L), eq(AGORA), any())).thenReturn(List.of(futura));
        Agendamento cancelada1 = Fixtures.aula(5L, LocalDateTime.of(2026, 11, 3, 9, 0), pilates, professora, sala);
        Agendamento cancelada2 = Fixtures.aula(6L, LocalDateTime.of(2026, 11, 5, 9, 0), pilates, professora, sala);
        when(cancelamentoRepository.findByAlunoIdAndDataHoraAulaBetweenOrderByDataHoraAulaAsc(eq(1L), any(), any()))
                .thenReturn(List.of(new CancelamentoAula(ana, cancelada1, true, false),
                        new CancelamentoAula(ana, cancelada2, false, true)));
        when(pacoteService.listar(1L)).thenReturn(List.of(
                new PacoteResponseDTO(10L, "Pilates", 5), new PacoteResponseDTO(11L, "Fisioterapia", -1)));
        when(agendamentoAlunoRepository.contarFuturas(1L, AGORA)).thenReturn(1L);

        DashboardAlunoDTO dash = service.dashboard(EMAIL, LocalDate.of(2026, 10, 11), LocalDate.of(2026, 11, 9), AGORA);

        assertEquals(3, dash.aulasRealizadas());
        assertEquals(2, dash.aulasCanceladas());
        assertEquals(1, dash.aulasPerdidas());
        assertEquals(1, dash.aulasAgendadas());
        assertEquals("Pilates", dash.especialidadeMaisFrequente());
        assertEquals("segunda-feira", dash.diaSemanaMaisFrequente());
        assertEquals(4, dash.saldoTotal());
        assertEquals(2, dash.pacotes().size());
        assertEquals(2, dash.aulasPorEspecialidade().get(0).quantidade());
        assertNotNull(dash.proximaAula());
        assertEquals(4L, dash.proximaAula().agendamentoId());
    }

    @Test
    void dashboard_semAulasNaoQuebraENaoInventaFrequencia() {
        alunoLogado();
        when(agendamentoAlunoRepository.buscarDoAlunoNoPeriodo(eq(1L), any(), any())).thenReturn(List.of());
        when(cancelamentoRepository.findByAlunoIdAndDataHoraAulaBetweenOrderByDataHoraAulaAsc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(pacoteService.listar(1L)).thenReturn(List.of());

        DashboardAlunoDTO dash = service.dashboard(EMAIL, null, null, AGORA);

        assertEquals(0, dash.aulasRealizadas());
        assertNull(dash.especialidadeMaisFrequente());
        assertNull(dash.diaSemanaMaisFrequente());
        assertNull(dash.proximaAula());
        assertEquals(0, dash.saldoTotal());
        assertEquals(AGORA.toLocalDate(), dash.ate());
        assertEquals(AGORA.toLocalDate().minusDays(29), dash.de());
    }

    // ------------------------------------------------------------------ perfil

    @Test
    void atualizarPerfil_soAlteraTelefoneENotificacoes() {
        alunoLogado();
        ana.setTipoContato("11 90000-0000");
        ana.setNotificacaoAtiva(true);
        when(alunoRepository.save(ana)).thenReturn(ana);

        PerfilAlunoDTO perfil = service.atualizarPerfil(EMAIL, new AtualizarPerfilAlunoDTO(" 11 98765-4321 ", false));

        assertEquals("11 98765-4321", perfil.telefone());
        assertEquals(Boolean.FALSE, perfil.notificacaoAtiva());
        assertEquals("Ana", perfil.nome());
        assertEquals(EMAIL, perfil.email());
        assertEquals(Boolean.TRUE, perfil.status());
    }

    @Test
    void atualizarPerfil_camposNaoEnviadosFicamComoEstao() {
        alunoLogado();
        ana.setTipoContato("11 90000-0000");
        ana.setNotificacaoAtiva(true);
        when(alunoRepository.save(ana)).thenReturn(ana);

        PerfilAlunoDTO perfil = service.atualizarPerfil(EMAIL, new AtualizarPerfilAlunoDTO(null, null));

        assertEquals("11 90000-0000", perfil.telefone());
        assertEquals(Boolean.TRUE, perfil.notificacaoAtiva());
    }

    @Test
    void tokenDeAlunoInexistenteRespondeNaoEncontrado() {
        when(alunoRepository.findByEmail("fantasma@teste.com")).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.perfil("fantasma@teste.com"));
    }

    @Test
    void detalhe_devolveAAulaDoAluno() {
        alunoLogado();
        AgendamentoAluno vinculo = aulaDeAna(100L, AGORA.plusDays(2));
        vinculo.confirmarPresenca();
        anaTemAAula(vinculo);

        AulaAlunoDTO aula = service.detalhe(EMAIL, 100L);

        assertEquals(100L, aula.agendamentoId());
        assertEquals("Sala Pilates 1", aula.sala());
        assertTrue(aula.presencaConfirmada());
    }
}
