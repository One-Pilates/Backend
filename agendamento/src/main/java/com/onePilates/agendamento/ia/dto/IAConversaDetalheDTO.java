package com.onePilates.agendamento.ia.dto;

import java.time.LocalDateTime;
import java.util.List;

public record IAConversaDetalheDTO(
        Long conversaId,
        String titulo,
        LocalDateTime criadaEm,
        LocalDateTime ultimoUso,
        List<IARealizacaoDTO> mensagens
) {}
