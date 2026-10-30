package com.onePilates.agendamento.ia.service;

import com.onePilates.agendamento.ia.model.IAConversa;
import com.onePilates.agendamento.ia.model.IARealizacao;
import com.onePilates.agendamento.ia.model.StatusRealizacao;
import com.onePilates.agendamento.ia.repository.IAConversaRepository;
import com.onePilates.agendamento.ia.repository.IARealizacaoRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Memória curta e histórico de conversas de cada usuário no banco.
 * O histórico enviado ao modelo é montado a partir das últimas realizações CONCLUIDAS: só o texto do usuário
 * e a resposta final do agente, nunca resultados de ferramentas, para não pesar nos tokens.
 */
@Service
public class ConversaService {

    private static final Duration VALIDADE = Duration.ofMinutes(20);
    private static final int MAX_CARACTERES = 800;

    private final IAConversaRepository conversaRepository;
    private final IARealizacaoRepository realizacaoRepository;

    public ConversaService(IAConversaRepository conversaRepository, IARealizacaoRepository realizacaoRepository) {
        this.conversaRepository = conversaRepository;
        this.realizacaoRepository = realizacaoRepository;
    }

    /** Reaproveita a conversa recente do usuário; abre outra se pediu nova, não tem ou passou o tempo de validade. */
    public IAConversa obterOuCriar(Long usuarioId, boolean nova) {
        return obterOuCriar(usuarioId, nova, null);
    }

    /**
     * Se conversaId for informado e pertencer ao usuário (e nova=false), continua exatamente essa conversa.
     * Caso contrário, aplica a regra padrão de reaproveitamento ou criação.
     */
    public IAConversa obterOuCriar(Long usuarioId, boolean nova, Long conversaId) {
        if (!nova && conversaId != null) {
            IAConversa especifica = conversaRepository.findById(conversaId)
                    .filter(c -> c.getUsuarioId().equals(usuarioId))
                    .orElse(null);
            if (especifica != null) {
                especifica.setUltimoUso(LocalDateTime.now());
                return conversaRepository.save(especifica);
            }
        }
        if (!nova) {
            LocalDateTime limite = LocalDateTime.now().minus(VALIDADE);
            IAConversa atual = conversaRepository.findTopByUsuarioIdOrderByUltimoUsoDesc(usuarioId).orElse(null);
            if (atual != null && atual.getUltimoUso().isAfter(limite)) {
                atual.setUltimoUso(LocalDateTime.now());
                return conversaRepository.save(atual);
            }
        }
        return conversaRepository.save(new IAConversa(usuarioId));
    }

    public void renovar(Long conversaId) {
        conversaRepository.findById(conversaId).ifPresent(c -> {
            c.setUltimoUso(LocalDateTime.now());
            conversaRepository.save(c);
        });
    }

    /** Mensagens anteriores da conversa, da mais antiga para a mais recente, no formato do modelo. */
    public List<Map<String, Object>> historico(Long conversaId) {
        List<IARealizacao> ultimas = new ArrayList<>(
                realizacaoRepository.findTop3ByConversaIdAndStatusOrderByIdDesc(conversaId, StatusRealizacao.CONCLUIDO));
        Collections.reverse(ultimas);
        List<Map<String, Object>> mensagens = new ArrayList<>();
        for (IARealizacao r : ultimas) {
            mensagens.add(Map.of("role", "user", "content", cortar(r.getMensagem())));
            mensagens.add(Map.of("role", "assistant", "content", cortar(r.getResposta())));
        }
        return mensagens;
    }

    private String cortar(String texto) {
        if (texto == null) return "";
        return texto.length() <= MAX_CARACTERES ? texto : texto.substring(0, MAX_CARACTERES) + "...";
    }
}
