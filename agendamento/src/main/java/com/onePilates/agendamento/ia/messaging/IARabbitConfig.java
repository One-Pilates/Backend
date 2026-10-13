package com.onePilates.agendamento.ia.messaging;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IARabbitConfig {

    @Bean
    public Queue iaQueue(@Value("${app.rabbitmq.ia-queue}") String nome) {
        return new Queue(nome, true);
    }

    /** JSON no producer e no consumidor (o Spring Boot aplica este bean ao RabbitTemplate e aos @RabbitListener). */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
