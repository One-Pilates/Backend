package com.onePilates.agendamento.aluno.service;

import com.onePilates.agendamento.agendamento.repository.AgendamentoAlunoRepository;
import com.onePilates.agendamento.agendamento.service.AgendamentoService;
import com.onePilates.agendamento.aluno.dto.AgendaItemDTO;
import com.onePilates.agendamento.aluno.dto.PerfilAlunoDTO;
import com.onePilates.agendamento.aluno.dto.RespostaDashAlunoDTO;
import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.aluno.repository.CancelamentoAulaRepository;
import com.onePilates.agendamento.aluno.repository.FeedbackAulaRepository;
import com.onePilates.agendamento.email.service.RabbitMQProducer;
import com.onePilates.agendamento.exception.AulaAindaNaoRealizadaException;
import com.onePilates.agendamento.exception.AulaJaRealizadaException;
import com.onePilates.agendamento.exception.BusinessException;
import com.onePilates.agendamento.exception.CancelamentoForaDoPrazoException;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.exception.FeedbackJaExistenteException;
import com.onePilates.agendamento.imagem.service.ImagemService;
import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.AgendamentoAluno;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.CancelamentoAula;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.model.FeedbackAula;
import com.onePilates.agendamento.model.Professor;
import com.onePilates.agendamento.model.Sala;
import com.onePilates.agendamento.model.enums.StatusPresenca;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
class PortalAlunoServiceTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 6, 10, 12, 0);

    @Mock private AlunoRepository alunoRepository;
    @Mock private AgendamentoAlunoRepository agendamentoAlunoRepository;
    @Mock private CancelamentoAulaRepository cancelamentoAulaRepository;
    @Mock private FeedbackAulaRepository feedbackAulaRepository;
    @Mock private PacoteAulaService pacoteAulaService;
    @Mock private AgendamentoService agendamentoService;
    @Mock private RabbitMQProducer rabbitMQProducer;
    @Mock private ImagemService imagemService;

    private PortalAlunoService service;
    private Aluno aluno;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-10T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        service = new PortalAlunoService(alunoRepository, agendamentoAlunoRepository, cancelamentoAulaRepository,
                feedbackAulaRepository, pacoteAulaService, agendamentoService, rabbitMQProducer, imagemService, clock);
        aluno = new Aluno();
        aluno.setId(1L);
        aluno.setNome("Ana");
        aluno.setCpf("111");
        aluno.setStatus(true);
    }

    @Test
    void listarAgenda_DeveDevolverSoAulasFuturasDesseAluno() {
        AgendamentoAluno ontem = vinculo(10L, aluno, AGORA.minusDays(1));
        AgendamentoAluno futura = vinculo(11L, aluno, AGORA.plusDays(2));
        Aluno outro = new Aluno();
        outro.setId(9L);
        AgendamentoAluno deOutro = vinculo(12L, outro, AGORA.plusDays(3));
        when(agendamentoAlunoRepository.findByAlunoId(1L)).thenReturn(List.of(ontem, futura, deOutro));

        List<AgendaItemDTO> agenda = service.listarAgenda(1L, null, null);

        assertEquals(1, agenda.size());
        assertEquals(11L, agenda.get(0).getAgendamentoId());
    }

    @Test
    void confirmarPresenca_DeveGravarFlagEData_QuandoAulaFutura() {
        AgendamentoAluno vinculo = vinculo(11L, aluno, AGORA.plusDays(2));
        when(agendamentoAlunoRepository.findByAlunoId(1L)).thenReturn(List.of(vinculo));
        when(agendamentoAlunoRepository.save(vinculo)).thenReturn(vinculo);

        AgendamentoAluno salvo = service.confirmarPresenca(1L, 11L);

        assertTrue(salvo.getPresencaConfirmada());
        assertEquals(AGORA, salvo.getDataConfirmacao());
    }

    @Test
    void confirmarPresenca_DeveLancarAulaJaRealizada_QuandoAulaPassada() {
        when(agendamentoAlunoRepository.findByAlunoId(1L)).thenReturn(List.of(vinculo(10L, aluno, AGORA.minusDays(1))));

        AulaJaRealizadaException ex = assertThrows(AulaJaRealizadaException.class, () -> service.confirmarPresenca(1L, 10L));

        assertEquals("AULA_JA_REALIZADA", ex.getCodigoErro());
        verify(agendamentoAlunoRepository, never()).save(any());
    }

    @Test
    void confirmarPresenca_DeveLancarNaoEncontrada_QuandoAulaNaoEhDoAluno() {
        when(agendamentoAlunoRepository.findByAlunoId(1L)).thenReturn(List.of());

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.confirmarPresenca(1L, 99L));
    }

    @Test
    void cancelar_DeveRemoverVinculoGravarSnapshotENotificar_QuandoFaltam24hOuMais() {
        AgendamentoAluno vinculo = vinculo(11L, aluno, AGORA.plusHours(24));
        vinculo.getAgendamento().getProfessor().setNotificacaoAtiva(true);
        when(agendamentoAlunoRepository.findByAlunoId(1L)).thenReturn(List.of(vinculo));
        when(agendamentoAlunoRepository.findByAgendamentoId(11L)).thenReturn(List.of(vinculo, vinculo(11L, new Aluno(), AGORA.plusHours(24))));

        service.cancelar(1L, 11L);

        ArgumentCaptor<CancelamentoAula> captor = ArgumentCaptor.forClass(CancelamentoAula.class);
        verify(cancelamentoAulaRepository).save(captor.capture());
        CancelamentoAula snapshot = captor.getValue();
        assertEquals(1L, snapshot.getAlunoId());
        assertEquals(AGORA.plusHours(24), snapshot.getDataHora());
        assertEquals("Pilates", snapshot.getEspecialidade());
        assertEquals("Professora", snapshot.getProfessor());
        assertEquals("Sala 1", snapshot.getSala());
        assertEquals(AGORA, snapshot.getCanceladoEm());
        verify(agendamentoAlunoRepository).delete(vinculo);
        verify(rabbitMQProducer).enviarPraFilaDeEmails(any());
    }

    @Test
    void cancelar_DeveExcluirAgendamento_QuandoForOUltimoAluno() {
        AgendamentoAluno vinculo = vinculo(11L, aluno, AGORA.plusDays(2));
        when(agendamentoAlunoRepository.findByAlunoId(1L)).thenReturn(List.of(vinculo));
        when(agendamentoAlunoRepository.findByAgendamentoId(11L)).thenReturn(List.of(vinculo));

        service.cancelar(1L, 11L);

        verify(cancelamentoAulaRepository).save(any(CancelamentoAula.class));
        verify(agendamentoService).excluirAgendamento(11L);
        verify(agendamentoAlunoRepository, never()).delete(any());
    }

    @Test
    void cancelar_DeveLancarForaDoPrazo_QuandoFaltaMenosDe24h() {
        when(agendamentoAlunoRepository.findByAlunoId(1L)).thenReturn(List.of(vinculo(11L, aluno, AGORA.plusHours(23))));

        CancelamentoForaDoPrazoException ex = assertThrows(CancelamentoForaDoPrazoException.class, () -> service.cancelar(1L, 11L));

        assertEquals("CANCELAMENTO_FORA_DO_PRAZO", ex.getCodigoErro());
        assertEquals("Não é possível cancelar a aula com menos de 24 horas de antecedência.", ex.getMessage());
        nadaFoiAlterado();
    }

    @Test
    void cancelar_NaoAlteraNada_QuandoAulaJaPassou() {
        when(agendamentoAlunoRepository.findByAlunoId(1L)).thenReturn(List.of(vinculo(11L, aluno, AGORA.minusHours(1))));

        assertThrows(CancelamentoForaDoPrazoException.class, () -> service.cancelar(1L, 11L));
        nadaFoiAlterado();
    }

    @Test
    void criarFeedback_DeveLancarAulaAindaNaoRealizada_QuandoAulaFutura() {
        when(agendamentoAlunoRepository.findByAlunoId(1L)).thenReturn(List.of(vinculo(11L, aluno, AGORA.plusHours(1))));

        AulaAindaNaoRealizadaException ex = assertThrows(AulaAindaNaoRealizadaException.class,
                () -> service.criarFeedback(1L, 11L, 5, null));
        assertEquals("AULA_AINDA_NAO_REALIZADA", ex.getCodigoErro());
    }

    @Test
    void criarFeedback_DeveCriar_QuandoAulaJaIniciadaENaoCancelada() {
        when(agendamentoAlunoRepository.findByAlunoId(1L)).thenReturn(List.of(vinculo(11L, aluno, AGORA.minusHours(1))));
        when(feedbackAulaRepository.findByAgendamentoIdAndAlunoId(11L, 1L)).thenReturn(Optional.empty());
        when(feedbackAulaRepository.save(any(FeedbackAula.class))).thenAnswer(inv -> inv.getArgument(0));

        FeedbackAula feedback = service.criarFeedback(1L, 11L, 4, null);

        assertEquals(4, feedback.getNota());
        assertEquals(null, feedback.getComentario());
    }

    @Test
    void criarFeedback_DeveConflitar_QuandoJaExiste() {
        when(agendamentoAlunoRepository.findByAlunoId(1L)).thenReturn(List.of(vinculo(11L, aluno, AGORA)));
        when(feedbackAulaRepository.findByAgendamentoIdAndAlunoId(11L, 1L)).thenReturn(Optional.of(new FeedbackAula()));

        assertThrows(FeedbackJaExistenteException.class, () -> service.criarFeedback(1L, 11L, 3, "ok"));
    }

    @Test
    void atualizarFeedback_DeveAlterarSoOProprio() {
        FeedbackAula feedback = new FeedbackAula();
        feedback.setNota(2);
        feedback.setAlunoId(1L);
        when(feedbackAulaRepository.findByAgendamentoIdAndAlunoId(11L, 1L)).thenReturn(Optional.of(feedback));
        when(feedbackAulaRepository.save(feedback)).thenReturn(feedback);

        assertEquals(5, service.atualizarFeedback(1L, 11L, 5, "melhor").getNota());
    }

    @Test
    void atualizarFeedback_DeveLancarNaoEncontrada_QuandoNaoHaFeedback() {
        when(feedbackAulaRepository.findByAgendamentoIdAndAlunoId(11L, 1L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.atualizarFeedback(1L, 11L, 5, null));
    }

    @Test
    void atualizarPerfil_DeveAlterarNotificacaoETipoContato() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(alunoRepository.save(aluno)).thenReturn(aluno);
        PerfilAlunoDTO dto = new PerfilAlunoDTO();
        dto.setNotificacaoAtiva(false);
        dto.setTipoContato("whatsapp");

        Aluno salvo = service.atualizarPerfil(1L, dto);

        assertFalse(salvo.getNotificacaoAtiva());
        assertEquals("whatsapp", salvo.getTipoContato());
    }

    @Test
    void atualizarPerfil_NaoDeveAlterarNomeCpfNemStatus() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(alunoRepository.save(aluno)).thenReturn(aluno);
        PerfilAlunoDTO dto = new PerfilAlunoDTO();
        dto.setNome("Outra");
        dto.setCpf("999");
        dto.setStatus(false);
        dto.setNotificacaoAtiva(false);

        Aluno salvo = service.atualizarPerfil(1L, dto);

        assertEquals("Ana", salvo.getNome());
        assertEquals("111", salvo.getCpf());
        assertTrue(salvo.getStatus());
    }

    @Test
    void uploadFoto_DeveGuardarCaminhoNoAluno() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(imagemService.uploadFotoAluno(any(), org.mockito.ArgumentMatchers.eq(1L))).thenReturn("alunos/1/foto.jpg");

        String caminho = service.uploadFoto(1L, new MockMultipartFile("foto", "a.jpg", "image/jpeg", new byte[]{1}));

        assertEquals("alunos/1/foto.jpg", caminho);
        assertEquals("alunos/1/foto.jpg", aluno.getFoto());
        verify(alunoRepository).save(aluno);
    }

    @Test
    void dashboard_DeveDevolverListasVaziasEKpisZerados_QuandoNaoHaAula() {
        when(agendamentoAlunoRepository.findByAlunoId(1L)).thenReturn(List.of());
        when(cancelamentoAulaRepository.findByAlunoId(1L)).thenReturn(List.of());

        RespostaDashAlunoDTO dash = service.dashboard(1L, 30);

        assertTrue(dash.getHistorico().isEmpty());
        assertTrue(dash.getAulasPorDiaSemana().isEmpty());
        assertTrue(dash.getAulasPorEspecialidade().isEmpty());
        assertEquals(0, dash.getKpis().getAulasNoPeriodo());
        assertEquals(0d, dash.getKpis().getTaxaPresenca());
        assertTrue(dash.getKpis().getAulasRemanescentes().isEmpty());
    }

    @Test
    void dashboard_DeveCalcularTaxaEspecialidadeDiaEIncluirCancelamento() {
        AgendamentoAluno presente = vinculo(1L, aluno, LocalDateTime.of(2026, 6, 9, 10, 0));
        presente.setStatusPresenca(StatusPresenca.PRESENTE);
        AgendamentoAluno falta = vinculo(2L, aluno, LocalDateTime.of(2026, 6, 8, 10, 0));
        falta.getAgendamento().getEspecialidade().setNome("Fisio");
        falta.setStatusPresenca(StatusPresenca.FALTA);
        AgendamentoAluno futura = vinculo(3L, aluno, AGORA.plusDays(3));
        CancelamentoAula cancelamento = new CancelamentoAula();
        cancelamento.setDataHora(LocalDateTime.of(2026, 6, 8, 11, 0));
        cancelamento.setEspecialidade("Pilates");
        cancelamento.setProfessor("Professora");
        cancelamento.setSala("Sala 1");
        when(agendamentoAlunoRepository.findByAlunoId(1L)).thenReturn(List.of(presente, falta, futura));
        when(cancelamentoAulaRepository.findByAlunoId(1L)).thenReturn(List.of(cancelamento));

        RespostaDashAlunoDTO dash = service.dashboard(1L, 30);

        assertEquals(0.5d, dash.getKpis().getTaxaPresenca());
        assertEquals("Pilates", dash.getKpis().getEspecialidadeMaisFrequentada());
        assertEquals("TERCA", dash.getKpis().getDiaSemanaMaisFrequente());
        assertTrue(dash.getHistorico().stream().anyMatch(h -> "CANCELADA".equals(h.getSituacao())));
        assertFalse(dash.getHistorico().stream().anyMatch(h -> h.getDataHora().equals(futura.getAgendamento().getDataHora())));
    }

    @Test
    void dashboard_DeveRejeitarPeriodo_QuandoForaDe1a365() {
        assertThrows(BusinessException.class, () -> service.dashboard(1L, 0));
        assertThrows(BusinessException.class, () -> service.dashboard(1L, 366));
    }

    private void nadaFoiAlterado() {
        verify(agendamentoAlunoRepository, never()).delete(any());
        verify(cancelamentoAulaRepository, never()).save(any());
        verify(rabbitMQProducer, never()).enviarPraFilaDeEmails(any());
        verify(agendamentoService, never()).excluirAgendamento(any());
    }

    private AgendamentoAluno vinculo(Long agendamentoId, Aluno dono, LocalDateTime quando) {
        if (dono.getId() == null) {
            dono.setId(99L);
        }
        Especialidade especialidade = new Especialidade();
        especialidade.setId(2L);
        especialidade.setNome("Pilates");
        Professor professor = new Professor();
        professor.setNome("Professora");
        professor.setEmail("prof@teste.com");
        Sala sala = new Sala();
        sala.setNome("Sala 1");
        Agendamento agendamento = new Agendamento();
        agendamento.setId(agendamentoId);
        agendamento.setDataHora(quando);
        agendamento.setEspecialidade(especialidade);
        agendamento.setProfessor(professor);
        agendamento.setSala(sala);
        return new AgendamentoAluno(agendamento, dono);
    }
}
