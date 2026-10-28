package com.onePilates.agendamento.ia.messaging;

/** Disparado dentro da transação que grava a realização; a fila só é acionada depois do commit. */
public record IARealizacaoCriadaEvent(Long realizacaoId) {}
