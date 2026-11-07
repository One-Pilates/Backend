package com.onePilates.agendamento.aluno.portal.service;

import com.onePilates.agendamento.agendamento.repository.AgendamentoAlunoRepository;
import com.onePilates.agendamento.email.dto.EmailRequestDTO;
import com.onePilates.agendamento.email.dto.LembretePresencaAlunoEmailDTO;
import com.onePilates.agendamento.email.service.RabbitMQProducer;
import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.AgendamentoAluno;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.enums.TipoEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lembrete por e-mail: quando faltam 48h (ou menos) para a aula e o aluno ainda não confirmou presença,
 * pede que ele confirme no aplicativo ou fale com a secretaria. Cada aula gera no máximo um lembrete.
 */
@Service
public class LembretePresencaService {

    private static final Logger logger = LoggerFactory.getLogger(LembretePresencaService.class);

    public static final Duration ANTECEDENCIA = Duration.ofHours(48);

    private final AgendamentoAlunoRepository agendamentoAlunoRepository;
    private final RabbitMQProducer rabbitMQ;

    public LembretePresencaService(AgendamentoAlunoRepository agendamentoAlunoRepository, RabbitMQProducer rabbitMQ) {
        this.agendamentoAlunoRepository = agendamentoAlunoRepository;
        this.rabbitMQ = rabbitMQ;
    }

    /** @return quantos lembretes foram enfileirados */
    @Transactional
    public int enviarPendentes() {
        return enviarPendentes(LocalDateTime.now());
    }

    int enviarPendentes(LocalDateTime agora) {
        List<AgendamentoAluno> pendentes =
                agendamentoAlunoRepository.pendentesDeLembrete(agora, agora.plus(ANTECEDENCIA));
        int enviados = 0;
        for (AgendamentoAluno vinculo : pendentes) {
            Aluno aluno = vinculo.getAluno();
            if (aluno.getEmail() == null || aluno.getEmail().isBlank()) {
                continue;
            }
            try {
                rabbitMQ.enviarPraFilaDeEmails(montarEmail(aluno, vinculo.getAgendamento()));
                // só marca como enviado se o e-mail entrou na fila; em falha, tenta de novo no próximo ciclo
                vinculo.setLembreteEnviadoEm(agora);
                agendamentoAlunoRepository.save(vinculo);
                enviados++;
            } catch (Exception e) {
                logger.warn("Lembrete de presença não enfileirado (aluno {}, aula {}): {}",
                        aluno.getId(), vinculo.getAgendamento().getId(), e.getMessage());
            }
        }
        if (enviados > 0) {
            logger.info("{} lembrete(s) de confirmação de presença enfileirado(s)", enviados);
        }
        return enviados;
    }

    private EmailRequestDTO montarEmail(Aluno aluno, Agendamento agendamento) {
        LembretePresencaAlunoEmailDTO payload = new LembretePresencaAlunoEmailDTO();
        payload.setNomeAluno(aluno.getNome());
        payload.setNomeProfessor(agendamento.getProfessor().getNome());
        payload.setNomeSala(agendamento.getSala().getNome());
        payload.setNomeEspecialidade(agendamento.getEspecialidade().getNome());
        payload.setDataHoraAgendamento(agendamento.getDataHora().toString());

        EmailRequestDTO email = new EmailRequestDTO();
        email.setTypeEmail(TipoEmail.LEMBRETE_PRESENCA_ALUNO);
        email.setDestinatario(aluno.getEmail());
        email.setPayload(payload);
        return email;
    }
}
