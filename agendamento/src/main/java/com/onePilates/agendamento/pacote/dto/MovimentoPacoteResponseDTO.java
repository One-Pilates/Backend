package com.onePilates.agendamento.pacote.dto;

import com.onePilates.agendamento.model.enums.MotivoMovimentoPacote;

import java.time.LocalDateTime;

public record MovimentoPacoteResponseDTO(Long especialidadeId, String especialidade, int variacao, int saldoApos,
                                         MotivoMovimentoPacote motivo, String responsavel, LocalDateTime criadoEm) {
}
