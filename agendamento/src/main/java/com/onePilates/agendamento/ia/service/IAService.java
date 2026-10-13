package com.onePilates.agendamento.ia.service;

import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.exception.OperacaoInvalidaException;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.ia.dto.IARealizacaoDTO;
import com.onePilates.agendamento.ia.dto.IARequestDTO;
import com.onePilates.agendamento.ia.dto.IASolicitacaoDTO;
import com.onePilates.agendamento.ia.messaging.IARealizacaoCriadaEvent;
import com.onePilates.agendamento.ia.model.IAConversa;
import com.onePilates.agendamento.ia.model.IARealizacao;
import com.onePilates.agendamento.ia.model.StatusRealizacao;
import com.onePilates.agendamento.ia.repository.IAAcaoRepository;
import com.onePilates.agendamento.ia.repository.IARealizacaoRepository;
import com.onePilates.agendamento.model.Funcionario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lado síncrono da IA: valida o usuário, registra a realização e a envia para a fila (responde na hora);
 * e consulta o andamento. Quem executa o agente é o {@link IAProcessador}.
 */
@Service
public class IAService {

    private static final Logger log = LoggerFactory.getLogger(IAService.class);

    /** Realização "em andamento" mais antiga que isso é considerada travada e não bloqueia novos pedidos. */
    private static final Duration PRAZO_EM_ANDAMENTO = Duration.ofMinutes(5);

    private final FuncionarioRepository funcionarioRepository;
    private final IARealizacaoRepository realizacaoRepository;
    private final IAAcaoRepository acaoRepository;
    private final ConversaService conversaService;
    private final ApplicationEventPublisher eventos;

    public IAService(FuncionarioRepository funcionarioRepository, IARealizacaoRepository realizacaoRepository,
                     IAAcaoRepository acaoRepository, ConversaService conversaService,
                     ApplicationEventPublisher eventos) {
        this.funcionarioRepository = funcionarioRepository;
        this.realizacaoRepository = realizacaoRepository;
        this.acaoRepository = acaoRepository;
        this.conversaService = conversaService;
        this.eventos = eventos;
    }

    @Transactional
    public IASolicitacaoDTO solicitar(IARequestDTO dto, String emailAutenticado) {
        log.info("[IA] Pedido recebido: usuarioId={} dataRequisicao={} mensagem=\"{}\"",
                dto.usuarioId(), dto.dataRequisicao(), resumir(dto.mensagem()));

        Funcionario usuario = validarUsuario(dto.usuarioId(), emailAutenticado);

        boolean emAndamento = realizacaoRepository.existsByUsuarioIdAndStatusInAndCriadoEmAfter(
                usuario.getId(), List.of(StatusRealizacao.PENDENTE, StatusRealizacao.PROCESSANDO),
                LocalDateTime.now().minus(PRAZO_EM_ANDAMENTO));
        if (emAndamento) {
            throw new OperacaoInvalidaException("Você já tem um pedido em andamento. Aguarde a resposta antes de enviar outro.");
        }

        IAConversa conversa = conversaService.obterOuCriar(usuario.getId(), Boolean.TRUE.equals(dto.novaConversa()));
        IARealizacao realizacao = realizacaoRepository.save(
                new IARealizacao(usuario.getId(), conversa.getId(), dto.mensagem()));

        // a fila só é acionada depois do commit (ver IAFilaPublisher)
        eventos.publishEvent(new IARealizacaoCriadaEvent(realizacao.getId()));
        log.info("[IA] Realização {} registrada para o usuário {} (conversa {})",
                realizacao.getId(), usuario.getId(), conversa.getId());
        return new IASolicitacaoDTO(realizacao.getId(), realizacao.getStatus());
    }

    @Transactional(readOnly = true)
    public IARealizacaoDTO consultar(Long realizacaoId, String emailAutenticado) {
        Funcionario usuario = funcionarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário do token não encontrado"));
        // de outro usuário = mesma resposta de "não existe", para não revelar ids alheios
        IARealizacao r = realizacaoRepository.findById(realizacaoId)
                .filter(x -> x.getUsuarioId().equals(usuario.getId()))
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Realização não encontrada"));

        List<IARealizacaoDTO.Acao> acoes = acaoRepository.findByRealizacaoIdOrderByOrdemAsc(r.getId()).stream()
                .map(a -> new IARealizacaoDTO.Acao(a.getOrdem(), a.getDescricao(), a.getStatus(),
                        a.getIniciadaEm(), a.getFinalizadaEm()))
                .toList();
        return new IARealizacaoDTO(r.getId(), r.getStatus(), r.getMensagem(), r.getPromptMelhorado(),
                r.getResposta(), r.getErro(), acoes, r.getCriadoEm(), r.getAtualizadoEm());
    }

    /** Garante que o usuário autenticado (e-mail vindo do JWT do header) é o usuarioId informado. */
    private Funcionario validarUsuario(Long usuarioId, String email) {
        Funcionario funcionario = funcionarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("[IA] Pedido recusado: usuário do token não existe");
                    return new IllegalArgumentException("Usuário do token não encontrado");
                });
        if (!funcionario.getId().equals(usuarioId)) {
            log.warn("[IA] Pedido recusado: usuarioId {} não pertence ao dono do JWT (id real {})",
                    usuarioId, funcionario.getId());
            throw new IllegalArgumentException("O usuarioId não corresponde ao dono do JWT");
        }
        return funcionario;
    }

    private String resumir(String texto) {
        if (texto == null) return "null";
        String limpo = texto.replace("\n", " ").trim();
        return limpo.length() <= 300 ? limpo : limpo.substring(0, 300) + "...(" + limpo.length() + " caracteres)";
    }
}
