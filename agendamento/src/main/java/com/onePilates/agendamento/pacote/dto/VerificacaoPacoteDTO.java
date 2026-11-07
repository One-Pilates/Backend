package com.onePilates.agendamento.pacote.dto;

/** Resultado da checagem de saldo feita antes de colocar o aluno em uma aula. */
public record VerificacaoPacoteDTO(Long alunoId, String aluno, int saldoAtual, boolean excedeUmPacote, String mensagem) {
}
