package com.onePilates.agendamento.ia.dto;

import com.onePilates.agendamento.ia.model.StatusRealizacao;

/** Resposta imediata do POST: o front guarda o id e passa a consultar o andamento. */
public record IASolicitacaoDTO(Long realizacaoId, StatusRealizacao status) {}
