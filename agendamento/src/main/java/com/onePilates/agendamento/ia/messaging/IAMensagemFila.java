package com.onePilates.agendamento.ia.messaging;

/** Conteúdo da mensagem na fila: só o id; tudo o mais (pedido, dono) o consumidor lê do banco. */
public record IAMensagemFila(Long realizacaoId) {}
