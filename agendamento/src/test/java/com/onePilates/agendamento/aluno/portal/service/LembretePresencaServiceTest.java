package com.onePilates.agendamento.aluno.portal.service;

import com.onePilates.agendamento.agendamento.repository.AgendamentoAlunoRepository;
import com.onePilates.agendamento.email.dto.EmailRequestDTO;
import com.onePilates.agendamento.email.dto.LembretePresencaAlunoEmailDTO;
import com.onePilates.agendamento.email.service.RabbitMQProducer;
import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.AgendamentoAluno;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.enums.TipoEmail;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LembretePresencaServiceTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 9, 10, 0);

    @Mock
    private AgendamentoAlunoRepository agendamentoAlunoRepository;
    @Mock
    private RabbitMQProducer rabbitMQ;

    @InjectMocks
    private LembretePresencaService service;

    private Aluno ana;
    private AgendamentoAluno vinculo;

    @BeforeEach
    void setUp() {
        ana = Fixtures.aluno(1L, "Ana", "ana@teste.com");
        Agendamento aula = Fixtures.aula(100L, AGORA.plusHours(40), Fixtures.especialidade(10L, "Pilates"),
                Fixtures.professor(5L, "Mariana"), Fixtures.sala(7L, "Sala 1"));
        vinculo = Fixtures.vincular(aula, ana);
    }

    @Test
    void procuraAulasDasProximas48Horas() {
        when(agendamentoAlunoRepository.pendentesDeLembrete(AGORA, AGORA.plusHours(48))).thenReturn(List.of());

        int enviados = service.enviarPendentes(AGORA);

        assertEquals(0, enviados);
        verify(agendamentoAlunoRepository).pendentesDeLembrete(AGORA, AGORA.plusHours(48));
        verify(rabbitMQ, never()).enviarPraFilaDeEmails(any());
    }

    @Test
    void enfileiraOLembreteEMarcaComoEnviado() {
        when(agendamentoAlunoRepository.pendentesDeLembrete(any(), any())).thenReturn(List.of(vinculo));

        int enviados = service.enviarPendentes(AGORA);

        assertEquals(1, enviados);
        ArgumentCaptor<EmailRequestDTO> captor = ArgumentCaptor.forClass(EmailRequestDTO.class);
        verify(rabbitMQ).enviarPraFilaDeEmails(captor.capture());
        EmailRequestDTO email = captor.getValue();
        assertEquals(TipoEmail.LEMBRETE_PRESENCA_ALUNO, email.getTypeEmail());
        assertEquals("ana@teste.com", email.getDestinatario());
        LembretePresencaAlunoEmailDTO payload = (LembretePresencaAlunoEmailDTO) email.getPayload();
        assertEquals("Ana", payload.getNomeAluno());
        assertEquals("Mariana", payload.getNomeProfessor());
        assertEquals("Pilates", payload.getNomeEspecialidade());
        assertEquals("Sala 1", payload.getNomeSala());
        assertEquals(AGORA.plusHours(40).toString(), payload.getDataHoraAgendamento());
        assertEquals(AGORA, vinculo.getLembreteEnviadoEm());
        verify(agendamentoAlunoRepository).save(vinculo);
    }

    @Test
    void naoMarcaComoEnviadoSeAFilaFalharParaTentarDeNovoNoProximoCiclo() {
        when(agendamentoAlunoRepository.pendentesDeLembrete(any(), any())).thenReturn(List.of(vinculo));
        doThrow(new RuntimeException("rabbit fora do ar")).when(rabbitMQ).enviarPraFilaDeEmails(any());

        int enviados = service.enviarPendentes(AGORA);

        assertEquals(0, enviados);
        assertNull(vinculo.getLembreteEnviadoEm());
        verify(agendamentoAlunoRepository, never()).save(any());
    }

    @Test
    void umaFalhaNaoImpedeOsLembretesDosDemaisAlunos() {
        Aluno bia = Fixtures.aluno(2L, "Bia", "bia@teste.com");
        AgendamentoAluno vinculoBia = Fixtures.vincular(vinculo.getAgendamento(), bia);
        when(agendamentoAlunoRepository.pendentesDeLembrete(any(), any())).thenReturn(List.of(vinculo, vinculoBia));
        doThrow(new RuntimeException("falha só no primeiro")).doNothing().when(rabbitMQ).enviarPraFilaDeEmails(any());

        int enviados = service.enviarPendentes(AGORA);

        assertEquals(1, enviados);
        assertNull(vinculo.getLembreteEnviadoEm());
        assertNotNull(vinculoBia.getLembreteEnviadoEm());
    }

    @Test
    void ignoraAlunoSemEmail() {
        ana.setEmail(" ");
        when(agendamentoAlunoRepository.pendentesDeLembrete(any(), any())).thenReturn(List.of(vinculo));

        int enviados = service.enviarPendentes(AGORA);

        assertEquals(0, enviados);
        verify(rabbitMQ, never()).enviarPraFilaDeEmails(any());
    }
}
