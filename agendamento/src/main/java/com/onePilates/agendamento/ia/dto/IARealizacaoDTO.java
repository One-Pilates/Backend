package com.onePilates.agendamento.ia.dto;

import com.onePilates.agendamento.ia.model.StatusAcao;
import com.onePilates.agendamento.ia.model.StatusRealizacao;

import java.time.LocalDateTime;
import java.util.List;

public record IARealizacaoDTO(
        Long realizacaoId,
        StatusRealizacao status,
        String mensagemOriginal,
        String promptMelhorado,
        /** Preenchida só quando status = CONCLUIDO. */
        String resposta,
        /** Preenchido só quando status = ERRO. */
        String erro,
        List<Acao> acoes,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public record Acao(Integer ordem, String descricao, StatusAcao status,
                       LocalDateTime iniciadaEm, LocalDateTime finalizadaEm) {}
}
