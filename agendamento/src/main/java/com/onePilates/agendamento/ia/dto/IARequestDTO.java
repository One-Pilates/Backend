package com.onePilates.agendamento.ia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record IARequestDTO(
        @NotNull Long usuarioId,
        @NotNull LocalDateTime dataRequisicao,
        @NotBlank String mensagem,
        /** Opcional: true descarta a memória da conversa do usuário antes deste pedido. */
        Boolean novaConversa
) {}
