package com.onePilates.agendamento.ia.service;

import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.ia.client.GroqClient;
import com.onePilates.agendamento.ia.messaging.IAMensagemFila;
import com.onePilates.agendamento.ia.model.IARealizacao;
import com.onePilates.agendamento.ia.model.StatusRealizacao;
import com.onePilates.agendamento.ia.repository.IAAcaoRepository;
import com.onePilates.agendamento.ia.repository.IARealizacaoRepository;
import com.onePilates.agendamento.ia.tool.ContextoUsuario;
import com.onePilates.agendamento.model.Funcionario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Consumidor da fila: lê a realização do banco, roda o agente registrando cada passo e grava a resposta final.
 * Nunca deixa a exceção escapar: um erro vira status ERRO (senão o RabbitMQ reentregaria a mensagem em loop).
 */
@Component
public class IAProcessador {

    private static final Logger log = LoggerFactory.getLogger(IAProcessador.class);

    private static final String PROMPT_MELHORAR =
            "Reescreva o pedido do usuário de forma clara e objetiva, sem mudar o sentido "
                    + "e sem inventar informações. Mantenha o TIPO do pedido: plano de aula (conteúdo e exercícios) "
                    + "não é agendamento (marcar data e horário); só use as palavras agendar, marcar ou criar aula "
                    + "se o usuário as disse. Mantenha nomes e textos exatamente como escritos. "
                    + "Responda somente com o texto reescrito.";

    private static final String ERRO_PADRAO = "Não foi possível processar o seu pedido agora. Tente novamente.";

    private final IARealizacaoRepository realizacaoRepository;
    private final IAAcaoRepository acaoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final ConversaService conversaService;
    private final GroqClient groqClient;
    private final AgentService agentService;

    public IAProcessador(IARealizacaoRepository realizacaoRepository, IAAcaoRepository acaoRepository,
                         FuncionarioRepository funcionarioRepository, ConversaService conversaService,
                         GroqClient groqClient, AgentService agentService) {
        this.realizacaoRepository = realizacaoRepository;
        this.acaoRepository = acaoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.conversaService = conversaService;
        this.groqClient = groqClient;
        this.agentService = agentService;
    }

    @RabbitListener(queues = "${app.rabbitmq.ia-queue}", concurrency = "${app.rabbitmq.ia-concurrency}")
    public void consumir(IAMensagemFila mensagem) {
        IARealizacao realizacao = realizacaoRepository.findById(mensagem.realizacaoId()).orElse(null);
        if (realizacao == null) {
            log.warn("[IA][worker] Realização {} não existe; mensagem descartada", mensagem.realizacaoId());
            return;
        }
        // idempotência: reentrega ou duplicidade não pode executar o mesmo pedido duas vezes
        if (realizacao.getStatus() != StatusRealizacao.PENDENTE) {
            log.warn("[IA][worker] Realização {} já está {}; ignorada", realizacao.getId(), realizacao.getStatus());
            return;
        }

        long inicio = System.currentTimeMillis();
        realizacao.iniciar();
        realizacaoRepository.save(realizacao);
        log.info("[IA][worker] Processando realização {} (usuário {})", realizacao.getId(), realizacao.getUsuarioId());

        try {
            processar(realizacao);
            log.info("[IA][worker] Realização {} concluída em {} ms", realizacao.getId(), System.currentTimeMillis() - inicio);
        } catch (Exception e) {
            log.error("[IA][worker] Realização {} falhou: {}", realizacao.getId(), e.getMessage(), e);
            realizacao.falhar(ERRO_PADRAO);
            realizacaoRepository.save(realizacao);
        }
    }

    private void processar(IARealizacao realizacao) {
        // sem JWT aqui: o dono vem gravado na realização, definido no endpoint a partir do token validado
        Funcionario funcionario = funcionarioRepository.findById(realizacao.getUsuarioId())
                .orElseThrow(() -> new IllegalStateException("Usuário " + realizacao.getUsuarioId() + " não existe mais"));
        ContextoUsuario usuario = new ContextoUsuario(funcionario.getId(), funcionario.getNome(),
                funcionario.getRole(), realizacao.getMensagem());

        ProgressoAgente progresso = new ProgressoRealizacao(realizacao.getId(), acaoRepository);
        List<Map<String, Object>> historico = conversaService.historico(realizacao.getConversaId());

        String promptMelhorado;
        if (historico.isEmpty()) {
            Long acao = progresso.iniciar("Entendendo o seu pedido");
            try {
                promptMelhorado = melhorarPrompt(realizacao.getMensagem());
                progresso.finalizar(acao, true);
            } catch (RuntimeException e) {
                progresso.finalizar(acao, false);
                throw e;
            }
        } else {
            // continuação ("sim", "confirmo", "e amanhã?"): reescrever sem contexto distorceria a mensagem
            promptMelhorado = realizacao.getMensagem();
        }
        realizacao.setPromptMelhorado(promptMelhorado);
        realizacaoRepository.save(realizacao);

        String resposta = agentService.executar(usuario, realizacao.getMensagem(), promptMelhorado, historico, progresso);

        // se um novo pedido do usuário cancelou esta realização enquanto o agente rodava, não sobrescreve
        StatusRealizacao atual = realizacaoRepository.findById(realizacao.getId())
                .map(IARealizacao::getStatus).orElse(StatusRealizacao.ERRO);
        if (atual != StatusRealizacao.PROCESSANDO) {
            log.warn("[IA][worker] Realização {} foi cancelada durante o processamento; resposta descartada", realizacao.getId());
            return;
        }
        realizacao.concluir(resposta);
        realizacaoRepository.save(realizacao);
        conversaService.renovar(realizacao.getConversaId());
    }

    private String melhorarPrompt(String mensagem) {
        return groqClient.chat(List.of(
                Map.of("role", "system", "content", PROMPT_MELHORAR),
                Map.of("role", "user", "content", mensagem)
        ), 0.2, 400);
    }
}
