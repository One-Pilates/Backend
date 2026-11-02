package com.onePilates.agendamento.exception;

public class AulaJaRealizadaException extends BusinessException {

    public AulaJaRealizadaException() {
        super("A aula já foi realizada.", "AULA_JA_REALIZADA");
    }
}
