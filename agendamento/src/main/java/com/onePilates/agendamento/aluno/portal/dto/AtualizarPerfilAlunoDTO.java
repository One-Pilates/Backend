package com.onePilates.agendamento.aluno.portal.dto;

/** Únicos campos que o próprio aluno pode alterar no perfil. Campos não enviados ficam como estão. */
public record AtualizarPerfilAlunoDTO(String telefone, Boolean notificacaoAtiva) {
}
