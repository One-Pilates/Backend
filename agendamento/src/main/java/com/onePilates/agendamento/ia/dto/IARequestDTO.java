package com.onePilates.agendamento.ia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record IARequestDTO(
        @NotNull Long usuarioId,
        @NotNull LocalDateTime dataRequisicao,
        @NotBlank String jwt,
        @NotBlank String mensagem
) {}
