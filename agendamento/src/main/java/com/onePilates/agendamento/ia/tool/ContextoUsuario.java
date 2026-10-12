package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.model.enums.Role;

/** Quem está pedindo a ação; vem do JWT já validado e do cadastro do funcionário. */
public record ContextoUsuario(Long usuarioId, String nome, Role role) {}
