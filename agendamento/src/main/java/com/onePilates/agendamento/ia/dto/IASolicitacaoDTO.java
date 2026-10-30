package com.onePilates.agendamento.ia.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.onePilates.agendamento.ia.model.StatusRealizacao;

/** Resposta imediata do POST: o front guarda o id e passa a consultar o andamento. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record IASolicitacaoDTO(Long realizacaoId, StatusRealizacao status, String textoTranscrito) {
    public IASolicitacaoDTO(Long realizacaoId, StatusRealizacao status) {
        this(realizacaoId, status, null);
    }
}
