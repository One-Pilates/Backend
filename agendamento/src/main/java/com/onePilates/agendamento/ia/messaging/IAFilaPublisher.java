package com.onePilates.agendamento.ia.messaging;

import com.onePilates.agendamento.ia.repository.IARealizacaoRepository;
import com.onePilates.agendamento.ia.service.IAProcessador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.CompletableFuture;

/** Publica na fila só depois do commit, para o consumidor nunca receber um id que ainda não existe no banco. */
@Component
public class IAFilaPublisher {

    private static final Logger log = LoggerFactory.getLogger(IAFilaPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final IARealizacaoRepository realizacaoRepository;
    private final String fila;
    private final IAProcessador iaProcessador;

    @Autowired
    public IAFilaPublisher(RabbitTemplate rabbitTemplate, IARealizacaoRepository realizacaoRepository,
                           @Value("${app.rabbitmq.ia-queue}") String fila,
                           IAProcessador iaProcessador) {
        this.rabbitTemplate = rabbitTemplate;
        this.realizacaoRepository = realizacaoRepository;
        this.fila = fila;
        this.iaProcessador = iaProcessador;
    }

    public IAFilaPublisher(RabbitTemplate rabbitTemplate, IARealizacaoRepository realizacaoRepository,
                           String fila) {
        this(rabbitTemplate, realizacaoRepository, fila, null);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publicar(IARealizacaoCriadaEvent evento) {
        IAMensagemFila mensagem = new IAMensagemFila(evento.realizacaoId());
        try {
            rabbitTemplate.convertAndSend(fila, mensagem);
            log.info("[IA][fila] Realização {} enviada para a fila {}", evento.realizacaoId(), fila);
        } catch (Exception e) {
            if (iaProcessador != null) {
                log.warn("[IA][fila] RabbitMQ indisponível ({}); executando realização {} de forma assíncrona local",
                        e.getMessage(), evento.realizacaoId());
                CompletableFuture.runAsync(() -> iaProcessador.consumir(mensagem));
                return;
            }
            log.error("[IA][fila] Falha ao enviar a realização {} para a fila: {}", evento.realizacaoId(), e.getMessage());
            realizacaoRepository.findById(evento.realizacaoId()).ifPresent(r -> {
                r.falhar("Não foi possível enfileirar o pedido. Tente novamente.");
                realizacaoRepository.save(r);
            });
        }
    }
}
