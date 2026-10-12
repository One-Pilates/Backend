package com.onePilates.agendamento.ia.dto;

import java.time.LocalDateTime;

public record IAResponseDTO(
        Long usuarioId,
        String mensagemOriginal,
        String promptMelhorado,
        String resposta,
        LocalDateTime dataResposta
) {}
