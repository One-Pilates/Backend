package com.onePilates.agendamento.aluno.portal.dto;

/** Perfil do aluno logado. Nome e e-mail são somente leitura: só a secretaria altera o cadastro. */
public record PerfilAlunoDTO(Long id, String nome, String email, String telefone, Boolean status,
                             Boolean notificacaoAtiva) {
}
