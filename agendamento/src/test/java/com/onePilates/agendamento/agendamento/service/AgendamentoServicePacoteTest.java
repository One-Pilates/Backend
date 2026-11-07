package com.onePilates.agendamento.agendamento.service;

import com.onePilates.agendamento.agendamento.dto.AgendamentoDTO;
import com.onePilates.agendamento.agendamento.dto.AgendamentoResponseDTO;
import com.onePilates.agendamento.agendamento.observer.AgendamentoNotifier;
import com.onePilates.agendamento.agendamento.repository.AgendamentoAlunoRepository;
import com.onePilates.agendamento.agendamento.repository.AgendamentoRepository;
import com.onePilates.agendamento.agendamento.validator.AgendamentoValidator;
import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.email.dto.AulaCanceladaAlunoEmailDTO;
import com.onePilates.agendamento.email.dto.AulaMarcadaAlunoEmailDTO;
import com.onePilates.agendamento.email.dto.EmailRequestDTO;
import com.onePilates.agendamento.email.service.RabbitMQProducer;
import com.onePilates.agendamento.especialidade.repository.EspecialidadeRepository;
import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.AgendamentoAluno;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.model.Professor;
import com.onePilates.agendamento.model.Sala;
import com.onePilates.agendamento.model.enums.MotivoMovimentoPacote;
import com.onePilates.agendamento.model.enums.TipoEmail;
import com.onePilates.agendamento.pacote.service.PacoteAulaService;
import com.onePilates.agendamento.professor.repository.ProfessorRepository;
import com.onePilates.agendamento.sala.repository.SalaRepository;
import com.onePilates.agendamento.testsupport.Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** O pacote de aulas dentro do agendamento: débito ao marcar, devolução pela equipe e e-mails ao aluno. */
@ExtendWith(MockitoExtension.class)
class AgendamentoServicePacoteTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;
    @Mock
    private ProfessorRepository professorRepository;
    @Mock
    private SalaRepository salaRepository;
    @Mock
    private EspecialidadeRepository especialidadeRepository;
    @Mock
    private AlunoRepository alunoRepository;
    @Mock
    private AgendamentoNotifier notifier;
    @Mock
    private AgendamentoAlunoRepository agendamentoAlunoRepository;
    @Mock
    private AgendamentoValidator agendamentoValidator;
    @Mock
    private RabbitMQProducer rabbitMQ;
    @Mock
    private PacoteAulaService pacoteService;

    @InjectMocks
    private AgendamentoService service;

    private Aluno ana;
    private Aluno bia;
    private Especialidade pilates;
    private Especialidade fisio;
    private Professor professora;
    private Sala sala;
    private LocalDateTime dataAula;

    @BeforeEach
    void setUp() {
        ana = Fixtures.aluno(1L, "Ana", "ana@teste.com");
        bia = Fixtures.aluno(2L, "Bia", "bia@teste.com");
        pilates = Fixtures.especialidade(10L, "Pilates");
        fisio = Fixtures.especialidade(11L, "Fisioterapia");
        professora = Fixtures.professor(5L, "Mariana");
        sala = Fixtures.sala(7L, "Sala 1");
        dataAula = LocalDateTime.now().plusDays(5).withHour(9).withMinute(0).withSecond(0).withNano(0);
    }

    private List<EmailRequestDTO> emailsEnviados() {
        ArgumentCaptor<EmailRequestDTO> captor = ArgumentCaptor.forClass(EmailRequestDTO.class);
        verify(rabbitMQ, org.mockito.Mockito.atLeast(0)).enviarPraFilaDeEmails(captor.capture());
        return captor.getAllValues();
    }

    private AgendamentoDTO dtoCriacao(Long... alunoIds) {
        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setDataHora(dataAula);
        dto.setProfessorId(5L);
        dto.setSalaId(7L);
        dto.setEspecialidadeId(10L);
        dto.setAlunoIds(new HashSet<>(List.of(alunoIds)));
        return dto;
    }

    private void prepararCriacao(Aluno... alunos) {
        when(agendamentoValidator.validarCriacaoListandoErros(any())).thenReturn(List.of());
        when(professorRepository.findById(5L)).thenReturn(Optional.of(professora));
        when(salaRepository.findById(7L)).thenReturn(Optional.of(sala));
        when(especialidadeRepository.findById(10L)).thenReturn(Optional.of(pilates));
        when(alunoRepository.findAllById(any())).thenReturn(List.of(alunos));
        // o mock do repositório devolve o mesmo objeto que o serviço montou, como o banco faria
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(inv -> {
            Agendamento a = inv.getArgument(0);
            a.setId(100L);
            agendamentoSalvoPeloTeste = a;
            return a;
        });
        when(agendamentoRepository.findById(100L)).thenAnswer(inv -> Optional.of(agendamentoSalvoPeloTeste));
    }

    private Agendamento agendamentoSalvoPeloTeste;

    // ------------------------------------------------------------------ criar

    @Test
    void criar_debitaOPacoteDeCadaAlunoDaAula() {
        prepararCriacao(ana, bia);

        service.criarAgendamento(dtoCriacao(1L, 2L));

        ArgumentCaptor<AgendamentoAluno> captor = ArgumentCaptor.forClass(AgendamentoAluno.class);
        verify(pacoteService, times(2)).debitar(captor.capture());
        Set<Long> alunosDebitados = new HashSet<>();
        captor.getAllValues().forEach(v -> alunosDebitados.add(v.getAluno().getId()));
        assertEquals(Set.of(1L, 2L), alunosDebitados);
    }

    @Test
    void criar_enviaOEmailDeAulaMarcadaParaOAlunoComNotificacaoLigada() {
        prepararCriacao(ana);
        professora.setNotificacaoAtiva(false);

        service.criarAgendamento(dtoCriacao(1L));

        List<EmailRequestDTO> emails = emailsEnviados();
        assertEquals(1, emails.size());
        EmailRequestDTO email = emails.get(0);
        assertEquals(TipoEmail.AULA_MARCADA_ALUNO, email.getTypeEmail());
        assertEquals("ana@teste.com", email.getDestinatario());
        AulaMarcadaAlunoEmailDTO payload = (AulaMarcadaAlunoEmailDTO) email.getPayload();
        assertEquals("Ana", payload.getNomeAluno());
        assertEquals("Mariana", payload.getNomeProfessor());
        assertEquals("Pilates", payload.getNomeEspecialidade());
        assertEquals("Sala 1", payload.getNomeSala());
        assertEquals(dataAula.toString(), payload.getDataHoraAgendamento());
    }

    @Test
    void criar_alunoComNotificacaoDesligadaNaoRecebeEmailMasContinuaDebitado() {
        ana.setNotificacaoAtiva(false);
        prepararCriacao(ana);
        professora.setNotificacaoAtiva(false);

        service.criarAgendamento(dtoCriacao(1L));

        assertTrue(emailsEnviados().isEmpty());
        verify(pacoteService).debitar(any(AgendamentoAluno.class));
    }

    @Test
    void criar_falhaNaFilaDeEmailNaoImpedeOAgendamento() {
        prepararCriacao(ana);
        professora.setNotificacaoAtiva(false);
        doThrow(new RuntimeException("rabbit fora do ar")).when(rabbitMQ).enviarPraFilaDeEmails(any());

        Agendamento criado = service.criarAgendamento(dtoCriacao(1L));

        assertEquals(100L, criado.getId());
        verify(pacoteService).debitar(any(AgendamentoAluno.class));
    }

    @Test
    void criar_semAlunoValidoNaoDebitaNada() {
        when(agendamentoValidator.validarCriacaoListandoErros(any())).thenReturn(List.of("A aula deve possuir pelo menos um aluno."));

        try {
            service.criarAgendamento(dtoCriacao());
        } catch (RuntimeException esperado) {
            // validação recusou
        }

        verify(pacoteService, never()).debitar(any());
    }

    @Test
    void toResponseDTOComAvisos_trazOsAvisosDePacote() {
        Agendamento aula = Fixtures.aula(100L, dataAula, pilates, professora, sala);
        Fixtures.vincular(aula, ana);
        when(pacoteService.avisosDeExcedente(any())).thenReturn(List.of("Ana está além do pacote de Pilates (saldo: -1)."));

        AgendamentoResponseDTO dto = service.toResponseDTOComAvisos(aula);

        assertEquals(1, dto.getAvisos().size());
        assertTrue(dto.getAvisos().get(0).contains("Ana"));
    }

    // ---------------------------------------------------------------- excluir

    @Test
    void excluir_devolveAAulaAoPacoteDeQuemTinhaDebitoEAvisaOAluno() {
        Agendamento aula = Fixtures.aula(100L, dataAula, pilates, professora, sala);
        AgendamentoAluno vinculoAna = Fixtures.vincular(aula, ana);
        professora.setNotificacaoAtiva(false);
        when(agendamentoRepository.existsById(100L)).thenReturn(true);
        when(agendamentoRepository.findById(100L)).thenReturn(Optional.of(aula));
        when(pacoteService.devolver(vinculoAna, MotivoMovimentoPacote.DEVOLUCAO_EQUIPE)).thenReturn(true);

        service.excluirAgendamento(100L);

        verify(pacoteService).devolver(vinculoAna, MotivoMovimentoPacote.DEVOLUCAO_EQUIPE);
        verify(agendamentoRepository).deleteById(100L);
        List<EmailRequestDTO> emails = emailsEnviados();
        assertEquals(1, emails.size());
        assertEquals(TipoEmail.AULA_CANCELADA_ALUNO, emails.get(0).getTypeEmail());
        AulaCanceladaAlunoEmailDTO payload = (AulaCanceladaAlunoEmailDTO) emails.get(0).getPayload();
        assertEquals(Boolean.FALSE, payload.getCanceladoPeloAluno());
        assertEquals(Boolean.TRUE, payload.getAulaDevolvidaAoPacote());
    }

    @Test
    void excluir_alunoSemDebitoNaoGanhaAulaDeGraca() {
        Agendamento aula = Fixtures.aula(100L, dataAula, pilates, professora, sala);
        AgendamentoAluno vinculoAna = Fixtures.vincular(aula, ana);
        professora.setNotificacaoAtiva(false);
        when(agendamentoRepository.existsById(100L)).thenReturn(true);
        when(agendamentoRepository.findById(100L)).thenReturn(Optional.of(aula));
        when(pacoteService.devolver(vinculoAna, MotivoMovimentoPacote.DEVOLUCAO_EQUIPE)).thenReturn(false);

        service.excluirAgendamento(100L);

        List<EmailRequestDTO> emails = emailsEnviados();
        AulaCanceladaAlunoEmailDTO payload = (AulaCanceladaAlunoEmailDTO) emails.get(0).getPayload();
        assertEquals(Boolean.FALSE, payload.getAulaDevolvidaAoPacote());
        verify(agendamentoRepository).deleteById(100L);
    }

    // -------------------------------------------------------------- atualizar

    private Agendamento aulaExistenteComAnaDebitada() {
        Agendamento aula = Fixtures.aula(100L, dataAula, pilates, professora, sala);
        AgendamentoAluno vinculoAna = Fixtures.vincular(aula, ana);
        vinculoAna.setCreditoDebitado(true);
        when(agendamentoRepository.findById(100L)).thenReturn(Optional.of(aula));
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(inv -> inv.getArgument(0));
        professora.setNotificacaoAtiva(false);
        return aula;
    }

    @Test
    void atualizar_retirarUmAlunoDevolveAAulaNaEspecialidadeDaAula() {
        Agendamento aula = aulaExistenteComAnaDebitada();
        AgendamentoAluno vinculoAna = aula.getAgendamentoAlunos().iterator().next();
        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setAlunoIds(new HashSet<>(List.of(2L)));
        when(alunoRepository.findAllById(any())).thenReturn(List.of(bia));

        service.atualizarAgendamento(100L, dto);

        verify(pacoteService).devolver(vinculoAna, pilates, MotivoMovimentoPacote.DEVOLUCAO_EQUIPE);
        ArgumentCaptor<AgendamentoAluno> novos = ArgumentCaptor.forClass(AgendamentoAluno.class);
        verify(pacoteService).debitar(novos.capture());
        assertEquals(2L, novos.getValue().getAluno().getId());
    }

    @Test
    void atualizar_incluirAlunoDebitaSoOQueEntrouEAvisaPorEmail() {
        Agendamento aula = aulaExistenteComAnaDebitada();
        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setAlunoIds(new HashSet<>(List.of(1L, 2L)));
        when(alunoRepository.findAllById(any())).thenReturn(List.of(ana, bia));

        service.atualizarAgendamento(100L, dto);

        ArgumentCaptor<AgendamentoAluno> debitados = ArgumentCaptor.forClass(AgendamentoAluno.class);
        verify(pacoteService, times(1)).debitar(debitados.capture());
        assertEquals(2L, debitados.getValue().getAluno().getId());
        verify(pacoteService, never()).devolver(any(AgendamentoAluno.class), any(Especialidade.class), any());
        List<EmailRequestDTO> emails = emailsEnviados();
        assertEquals(1, emails.size());
        assertEquals("bia@teste.com", emails.get(0).getDestinatario());
        assertEquals(TipoEmail.AULA_MARCADA_ALUNO, emails.get(0).getTypeEmail());
        assertEquals(2, aula.getAgendamentoAlunos().size());
    }

    @Test
    void atualizar_trocarAEspecialidadeMoveODebitoDeQuemFicou() {
        Agendamento aula = aulaExistenteComAnaDebitada();
        AgendamentoAluno vinculoAna = aula.getAgendamentoAlunos().iterator().next();
        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setEspecialidadeId(11L);
        when(especialidadeRepository.findById(11L)).thenReturn(Optional.of(fisio));
        when(pacoteService.devolver(vinculoAna, pilates, MotivoMovimentoPacote.DEVOLUCAO_EQUIPE)).thenReturn(true);

        service.atualizarAgendamento(100L, dto);

        verify(pacoteService).devolver(vinculoAna, pilates, MotivoMovimentoPacote.DEVOLUCAO_EQUIPE);
        verify(pacoteService).debitar(vinculoAna);
        assertEquals(fisio, aula.getEspecialidade());
    }

    @Test
    void atualizar_mudarSoOHorarioNaoMexeNoPacote() {
        aulaExistenteComAnaDebitada();
        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setDataHora(dataAula.plusDays(1));

        service.atualizarAgendamento(100L, dto);

        verify(pacoteService, never()).debitar(any());
        verify(pacoteService, never()).devolver(any(AgendamentoAluno.class), any(Especialidade.class), any());
        verify(pacoteService, never()).devolver(any(AgendamentoAluno.class), any(MotivoMovimentoPacote.class));
    }

    @Test
    void atualizar_respostaTrazOsAvisosDePacote() {
        aulaExistenteComAnaDebitada();
        when(pacoteService.avisosDeExcedente(any())).thenReturn(List.of("Ana está além do pacote de Pilates (saldo: -1)."));

        AgendamentoResponseDTO resposta = service.atualizarAgendamento(100L, new AgendamentoDTO());

        assertEquals(1, resposta.getAvisos().size());
        verify(agendamentoValidator).validar(any(AgendamentoDTO.class), eq(100L));
    }
}
