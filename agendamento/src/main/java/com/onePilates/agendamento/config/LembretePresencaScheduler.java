package com.onePilates.agendamento.config;

import com.onePilates.agendamento.aluno.portal.service.LembretePresencaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Dispara periodicamente o lembrete de confirmação de presença (48h antes da aula).
 * Para desligar (por exemplo em um segundo servidor): {@code app.lembrete.ativo=false}.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.lembrete.ativo", havingValue = "true", matchIfMissing = true)
public class LembretePresencaScheduler {

    private static final Logger logger = LoggerFactory.getLogger(LembretePresencaScheduler.class);

    private final LembretePresencaService lembreteService;

    public LembretePresencaScheduler(LembretePresencaService lembreteService) {
        this.lembreteService = lembreteService;
    }

    @Scheduled(fixedDelayString = "${app.lembrete.intervalo-ms:600000}",
            initialDelayString = "${app.lembrete.atraso-inicial-ms:60000}")
    public void enviarLembretes() {
        try {
            lembreteService.enviarPendentes();
        } catch (Exception e) {
            logger.error("Falha ao processar lembretes de presença", e);
        }
    }
}
