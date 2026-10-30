package com.onePilates.agendamento.ia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record IARequestDTO(
        @NotNull Long usuarioId,
        @NotNull LocalDateTime dataRequisicao,
        @NotBlank String mensagem,
        /** Opcional: true descarta a memória da conversa do usuário antes deste pedido. */
        Boolean novaConversa,
        /** Opcional: id de uma conversa existente para continuar exatamente aquele chat do histórico. */
        Long conversaId
) {
    public IARequestDTO(Long usuarioId, LocalDateTime dataRequisicao, String mensagem, Boolean novaConversa) {
        this(usuarioId, dataRequisicao, mensagem, novaConversa, null);
    }
}
