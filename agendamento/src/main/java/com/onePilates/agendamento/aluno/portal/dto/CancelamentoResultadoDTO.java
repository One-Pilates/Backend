package com.onePilates.agendamento.aluno.portal.dto;

/**
 * Resultado do cancelamento feito pelo aluno.
 *
 * @param aulaDevolvida true se a aula voltou ao pacote
 * @param foraDoPrazo   true se cancelou com menos de 24h (a aula continua descontada)
 */
public record CancelamentoResultadoDTO(boolean aulaDevolvida, boolean foraDoPrazo, String mensagem) {
}
