package com.onePilates.agendamento.aluno.portal.dto;

import java.time.LocalDateTime;

/**
 * Uma aula do ponto de vista do aluno.
 *
 * @param situacao            AGENDADA (ainda vai acontecer), REALIZADA (a data/hora já passou) ou CANCELADA (pelo aluno)
 * @param podeCancelar        só aulas futuras podem ser canceladas
 * @param cancelamentoSemPerda true se cancelar agora devolve a aula ao pacote (24h ou mais de antecedência)
 * @param aulaDevolvida       só em aulas canceladas: a aula voltou ao pacote?
 * @param foraDoPrazo         só em aulas canceladas: o cancelamento foi com menos de 24h?
 */
public record AulaAlunoDTO(Long agendamentoId,
                           LocalDateTime dataHora,
                           int duracaoMinutos,
                           String especialidade,
                           String professor,
                           Long professorId,
                           String professorFoto,
                           String sala,
                           SituacaoAulaAluno situacao,
                           boolean presencaConfirmada,
                           boolean podeCancelar,
                           boolean cancelamentoSemPerda,
                           Boolean aulaDevolvida,
                           Boolean foraDoPrazo,
                           LocalDateTime canceladoEm) {
}
