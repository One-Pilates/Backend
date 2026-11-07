package com.onePilates.agendamento.model.enums;

public enum MotivoMovimentoPacote {
    /** Secretaria ou administrador definiu o saldo manualmente. */
    AJUSTE_MANUAL,
    /** Aluno foi colocado em uma aula. */
    AGENDAMENTO,
    /** Aluno foi retirado da aula (ou a aula mudou de especialidade) pela equipe: aula devolvida. */
    DEVOLUCAO_EQUIPE,
    /** O aluno cancelou com 24h ou mais de antecedência: aula devolvida. */
    CANCELAMENTO_ALUNO_COM_DEVOLUCAO
}
