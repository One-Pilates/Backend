package com.onePilates.agendamento.pacote.dto;

/** Saldo de aulas do aluno em uma especialidade. Saldo negativo = aulas além do pacote. */
public record PacoteResponseDTO(Long especialidadeId, String especialidade, int saldo) {
}
