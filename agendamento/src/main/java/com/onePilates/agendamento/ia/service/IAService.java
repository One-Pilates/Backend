package com.onePilates.agendamento.ia.service;

import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.exception.OperacaoInvalidaException;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.ia.client.GroqClient;
import com.onePilates.agendamento.ia.dto.IAConversaDetalheDTO;
import com.onePilates.agendamento.ia.dto.IAConversaResumoDTO;
import com.onePilates.agendamento.ia.dto.IARealizacaoDTO;
import com.onePilates.agendamento.ia.dto.IARequestDTO;
import com.onePilates.agendamento.ia.dto.IASolicitacaoDTO;
import com.onePilates.agendamento.ia.messaging.IARealizacaoCriadaEvent;
import com.onePilates.agendamento.ia.model.IAConversa;
import com.onePilates.agendamento.ia.model.IARealizacao;
import com.onePilates.agendamento.ia.model.StatusRealizacao;
import com.onePilates.agendamento.ia.repository.IAAcaoRepository;
import com.onePilates.agendamento.ia.repository.IAConversaRepository;
import com.onePilates.agendamento.ia.repository.IARealizacaoRepository;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.Funcionario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lado síncrono da IA: valida o usuário, transcreve áudio (quando enviado), registra a realização
 * e a envia para a fila (responde na hora); consulta o andamento e gerencia o histórico de conversas.
 * Quem executa o agente é o {@link IAProcessador}.
 */
@Service
public class IAService {

    private static final Logger log = LoggerFactory.getLogger(IAService.class);

    private final FuncionarioRepository funcionarioRepository;
    private final IARealizacaoRepository realizacaoRepository;
    private final IAAcaoRepository acaoRepository;
    private final IAConversaRepository conversaRepository;
    private final ConversaService conversaService;
    private final ApplicationEventPublisher eventos;
    private final GroqClient groqClient;
    private final AlunoRepository alunoRepository;

    @Autowired
    public IAService(FuncionarioRepository funcionarioRepository, IARealizacaoRepository realizacaoRepository,
                     IAAcaoRepository acaoRepository, IAConversaRepository conversaRepository,
                     ConversaService conversaService, ApplicationEventPublisher eventos, GroqClient groqClient,
                     AlunoRepository alunoRepository) {
        this.funcionarioRepository = funcionarioRepository;
        this.realizacaoRepository = realizacaoRepository;
        this.acaoRepository = acaoRepository;
        this.conversaRepository = conversaRepository;
        this.conversaService = conversaService;
        this.eventos = eventos;
        this.groqClient = groqClient;
        this.alunoRepository = alunoRepository;
    }

    public IAService(FuncionarioRepository funcionarioRepository, IARealizacaoRepository realizacaoRepository,
                     IAAcaoRepository acaoRepository, ConversaService conversaService,
                     ApplicationEventPublisher eventos, GroqClient groqClient) {
        this(funcionarioRepository, realizacaoRepository, acaoRepository, null, conversaService, eventos, groqClient, null);
    }

    public IAService(FuncionarioRepository funcionarioRepository, IARealizacaoRepository realizacaoRepository,
                     IAAcaoRepository acaoRepository, ConversaService conversaService,
                     ApplicationEventPublisher eventos) {
        this(funcionarioRepository, realizacaoRepository, acaoRepository, null, conversaService, eventos, null, null);
    }

    @Transactional
    public IASolicitacaoDTO solicitar(IARequestDTO dto, String emailAutenticado) {
        log.info("[IA] Pedido recebido: usuarioId={} dataRequisicao={} mensagem=\"{}\"",
                dto.usuarioId(), dto.dataRequisicao(), resumir(dto.mensagem()));

        Long usuarioId = validarUsuario(dto.usuarioId(), emailAutenticado);

        // pedido anterior preso (erro, timeout do front, worker caído) não pode travar o usuário:
        // o novo pedido vence e os anteriores em andamento são cancelados
        for (IARealizacao anterior : realizacaoRepository.findByUsuarioIdAndStatusIn(
                usuarioId, List.of(StatusRealizacao.PENDENTE, StatusRealizacao.PROCESSANDO))) {
            anterior.falhar("Cancelado: um novo pedido foi enviado.");
            realizacaoRepository.save(anterior);
            log.warn("[IA] Realização {} em andamento cancelada por novo pedido do usuário {}",
                    anterior.getId(), usuarioId);
        }

        boolean novaConversa = Boolean.TRUE.equals(dto.novaConversa());
        IAConversa conversa = dto.conversaId() != null
                ? conversaService.obterOuCriar(usuarioId, novaConversa, dto.conversaId())
                : conversaService.obterOuCriar(usuarioId, novaConversa);
        IARealizacao realizacao = realizacaoRepository.save(
                new IARealizacao(usuarioId, conversa.getId(), dto.mensagem()));

        // a fila só é acionada depois do commit (ver IAFilaPublisher)
        eventos.publishEvent(new IARealizacaoCriadaEvent(realizacao.getId()));
        log.info("[IA] Realização {} registrada para o usuário {} (conversa {})",
                realizacao.getId(), usuarioId, conversa.getId());
        return new IASolicitacaoDTO(realizacao.getId(), realizacao.getStatus(), null, conversa.getId());
    }

    /**
     * Recebe um arquivo de áudio, realiza a transcrição via Groq Whisper e encaminha o texto transcrito
     * diretamente para o fluxo do Agente IA.
     */
    @Transactional
    public IASolicitacaoDTO solicitarPorAudio(Long usuarioId, MultipartFile audio, Boolean novaConversa,
                                              String emailAutenticado) {
        return solicitarPorAudio(usuarioId, audio, novaConversa, null, emailAutenticado);
    }

    @Transactional
    public IASolicitacaoDTO solicitarPorAudio(Long usuarioId, MultipartFile audio, Boolean novaConversa,
                                              Long conversaId, String emailAutenticado) {
        validarUsuario(usuarioId, emailAutenticado);
        String textoTranscrito = transcreverAudio(audio);

        IARequestDTO requestDTO = new IARequestDTO(
                usuarioId,
                LocalDateTime.now(),
                textoTranscrito,
                novaConversa,
                conversaId
        );
        IASolicitacaoDTO solicitacao = solicitar(requestDTO, emailAutenticado);
        return new IASolicitacaoDTO(
                solicitacao.realizacaoId(),
                solicitacao.status(),
                textoTranscrito,
                solicitacao.conversaId()
        );
    }

    /**
     * Transcreve um arquivo de áudio enviado pelo usuário para texto em português.
     */
    public String transcreverAudio(MultipartFile audio) {
        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException("Nenhum arquivo de áudio foi enviado ou o áudio está vazio.");
        }
        if (groqClient == null) {
            throw new IllegalStateException("Cliente de transcrição da IA não configurado.");
        }
        try {
            String transcricao = groqClient.transcreverAudio(
                    audio.getBytes(),
                    audio.getOriginalFilename(),
                    audio.getContentType()
            );
            if (transcricao == null || transcricao.isBlank()) {
                throw new OperacaoInvalidaException(
                        "Não foi possível identificar fala no áudio enviado. Tente gravar novamente falando mais próximo ao microfone."
                );
            }
            return transcricao.trim();
        } catch (IOException e) {
            log.error("[IA] Erro ao ler bytes do arquivo de áudio: {}", e.getMessage());
            throw new RuntimeException("Não foi possível ler o arquivo de áudio enviado.", e);
        }
    }

    @Transactional(readOnly = true)
    public IARealizacaoDTO consultar(Long realizacaoId, String emailAutenticado) {
        Long usuarioId = idDoToken(emailAutenticado);
        // de outro usuário = mesma resposta de "não existe", para não revelar ids alheios
        IARealizacao r = realizacaoRepository.findById(realizacaoId)
                .filter(x -> x.getUsuarioId().equals(usuarioId))
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Realização não encontrada"));

        return mapearRealizacaoDTO(r);
    }

    /** Lista todas as conversas do usuário autenticado que possuem ao menos uma mensagem. */
    @Transactional(readOnly = true)
    public List<IAConversaResumoDTO> listarConversas(String emailAutenticado) {
        if (conversaRepository == null) {
            return List.of();
        }
        Long usuarioId = idDoToken(emailAutenticado);

        List<IAConversa> conversas = conversaRepository.findByUsuarioIdOrderByUltimoUsoDesc(usuarioId);
        List<IAConversaResumoDTO> resultado = new ArrayList<>();
        for (IAConversa c : conversas) {
            List<IARealizacao> msgs = realizacaoRepository.findByConversaIdOrderByIdAsc(c.getId());
            if (msgs.isEmpty()) {
                continue;
            }
            String titulo = gerarTituloConversa(msgs.get(0).getMensagem());
            resultado.add(new IAConversaResumoDTO(
                    c.getId(),
                    titulo,
                    c.getCriadaEm(),
                    c.getUltimoUso(),
                    msgs.size()
            ));
        }
        return resultado;
    }

    /** Retorna o histórico completo de mensagens e etapas de uma conversa específica do usuário. */
    @Transactional(readOnly = true)
    public IAConversaDetalheDTO obterConversa(Long conversaId, String emailAutenticado) {
        if (conversaRepository == null) {
            throw new EntidadeNaoEncontradaException("Conversa não encontrada");
        }
        Long usuarioId = idDoToken(emailAutenticado);

        IAConversa conversa = conversaRepository.findById(conversaId)
                .filter(c -> c.getUsuarioId().equals(usuarioId))
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Conversa não encontrada"));

        List<IARealizacao> realizacoes = realizacaoRepository.findByConversaIdOrderByIdAsc(conversa.getId());
        String titulo = realizacoes.isEmpty()
                ? "Nova conversa"
                : gerarTituloConversa(realizacoes.get(0).getMensagem());

        List<IARealizacaoDTO> mensagens = realizacoes.stream()
                .map(this::mapearRealizacaoDTO)
                .toList();

        return new IAConversaDetalheDTO(
                conversa.getId(),
                titulo,
                conversa.getCriadaEm(),
                conversa.getUltimoUso(),
                mensagens
        );
    }

    /** Exclui uma conversa específica e todas as suas mensagens/ações associadas. */
    @Transactional
    public void excluirConversa(Long conversaId, String emailAutenticado) {
        if (conversaRepository == null) {
            return;
        }
        Long usuarioId = idDoToken(emailAutenticado);

        IAConversa conversa = conversaRepository.findById(conversaId)
                .filter(c -> c.getUsuarioId().equals(usuarioId))
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Conversa não encontrada"));

        List<IARealizacao> realizacoes = realizacaoRepository.findByConversaIdOrderByIdAsc(conversa.getId());
        if (!realizacoes.isEmpty()) {
            List<Long> ids = realizacoes.stream().map(IARealizacao::getId).toList();
            acaoRepository.deleteByRealizacaoIdIn(ids);
            realizacaoRepository.deleteByConversaId(conversa.getId());
        }
        conversaRepository.delete(conversa);
    }

    private IARealizacaoDTO mapearRealizacaoDTO(IARealizacao r) {
        List<IARealizacaoDTO.Acao> acoes = acaoRepository.findByRealizacaoIdOrderByOrdemAsc(r.getId()).stream()
                .map(a -> new IARealizacaoDTO.Acao(a.getOrdem(), a.getDescricao(), a.getStatus(),
                        a.getIniciadaEm(), a.getFinalizadaEm()))
                .toList();
        return new IARealizacaoDTO(r.getId(), r.getStatus(), r.getMensagem(), r.getPromptMelhorado(),
                r.getResposta(), r.getErro(), acoes, r.getCriadoEm(), r.getAtualizadoEm());
    }

    private String gerarTituloConversa(String primeiraMensagem) {
        if (primeiraMensagem == null || primeiraMensagem.isBlank()) {
            return "Conversa com OneIA";
        }
        String limpo = primeiraMensagem.replace("\n", " ").replaceAll("\\s+", " ").trim();
        return limpo.length() <= 48 ? limpo : limpo.substring(0, 48).trim() + "...";
    }

    /** Garante que o usuário autenticado (e-mail vindo do JWT do header) é o usuarioId informado. */
    private Long validarUsuario(Long usuarioId, String email) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("O usuarioId é obrigatório");
        }
        Long idReal;
        try {
            idReal = idDoToken(email);
        } catch (EntidadeNaoEncontradaException e) {
            log.warn("[IA] Pedido recusado: usuário do token não existe");
            throw new IllegalArgumentException("Usuário do token não encontrado");
        }
        if (!idReal.equals(usuarioId)) {
            log.warn("[IA] Pedido recusado: usuarioId {} não pertence ao dono do JWT (id real {})",
                    usuarioId, idReal);
            throw new IllegalArgumentException("O usuarioId não corresponde ao dono do JWT");
        }
        return idReal;
    }

    private Long idDoToken(String email) {
        Optional<Funcionario> funcionario = funcionarioRepository.findByEmail(email);
        if (funcionario != null && funcionario.isPresent()) {
            return funcionario.get().getId();
        }
        if (alunoRepository != null) {
            Optional<Aluno> aluno = alunoRepository.findByEmail(email);
            if (aluno != null && aluno.isPresent()) {
                return aluno.get().getId();
            }
        }
        throw new EntidadeNaoEncontradaException("Usuário do token não encontrado");
    }

    private String resumir(String texto) {
        if (texto == null) return "null";
        String limpo = texto.replace("\n", " ").trim();
        return limpo.length() <= 300 ? limpo : limpo.substring(0, 300) + "...(" + limpo.length() + " caracteres)";
    }
}
