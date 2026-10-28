package com.onePilates.agendamento.email.service;

import com.onePilates.agendamento.email.dto.EmailRequestDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;
    private final String queue;

    public RabbitMQProducer(
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbitmq.email-queue}") String queue
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.queue = queue;


        this.rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    public void enviarPraFilaDeEmails(EmailRequestDTO dto) {
        try {
            rabbitTemplate.convertAndSend(queue, dto);
            System.out.println("📤 Enviado para fila: " + queue);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao enviar pedido de email " + dto.getTypeEmail(),
                    e
            );
        }
    }
}