package com.onePilates.agendamento.agendamento.observer;

import com.onePilates.agendamento.email.dto.AulaCriadaEmailDTO;
import com.onePilates.agendamento.email.dto.EmailRequestDTO;
import com.onePilates.agendamento.email.service.RabbitMQProducer;
import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.enums.TipoEmail;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacaoProfessorObserver implements AgendamentoObserver {


    private final RabbitMQProducer rabbitMQProducer;

    public NotificacaoProfessorObserver(RabbitMQProducer rabbitMQProducer) {
        this.rabbitMQProducer = rabbitMQProducer;
    }

    @Override
    public void notificar(Agendamento agendamento) {
        // Usar agendamentoAlunos diretamente para evitar problemas de lazy loading
        List<String> nomesAlunos = agendamento.getAgendamentoAlunos().stream()
                .map(aa -> aa.getAluno().getNome())
                .toList();

        System.out.println("🔔 Notificando professor " + agendamento.getProfessor().getNome() +
                " sobre novo agendamento com os alunos: " + String.join(", ", nomesAlunos));

        AulaCriadaEmailDTO aulaCriadaEmailDTO = new AulaCriadaEmailDTO();
        aulaCriadaEmailDTO.setNomeEspecialidade(agendamento.getEspecialidade().getNome());
        aulaCriadaEmailDTO.setNomeProfessor(agendamento.getProfessor().getNome());
        aulaCriadaEmailDTO.setNomeSala(agendamento.getSala().getNome());
        aulaCriadaEmailDTO.setDataHoraAgendamento(agendamento.getDataHora().toString());
        aulaCriadaEmailDTO.setNomesDosAlunos(nomesAlunos);
        EmailRequestDTO emailRequestDTO = new EmailRequestDTO();
        emailRequestDTO.setDestinatario(agendamento.getProfessor().getEmail());
        emailRequestDTO.setTypeEmail(TipoEmail.AULA_CRIADA);
        emailRequestDTO.setPayload(aulaCriadaEmailDTO);


        rabbitMQProducer.enviarPraFilaDeEmails(emailRequestDTO);
    }


}
