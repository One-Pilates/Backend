package com.onePilates.agendamento.ia.dto;

import java.time.LocalDateTime;

public record IAConversaResumoDTO(
        Long conversaId,
        String titulo,
        LocalDateTime criadaEm,
        LocalDateTime ultimoUso,
        int totalMensagens
) {}

