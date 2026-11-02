package com.onePilates.agendamento.exception;

public class AulaAindaNaoRealizadaException extends BusinessException {

    public AulaAindaNaoRealizadaException() {
        super("A aula ainda não foi realizada.", "AULA_AINDA_NAO_REALIZADA");
    }
}
