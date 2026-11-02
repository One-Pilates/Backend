package com.onePilates.agendamento.exception;

public class FeedbackJaExistenteException extends BusinessException {

    public FeedbackJaExistenteException() {
        super("Já existe um feedback para esta aula.", "FEEDBACK_JA_EXISTENTE");
    }
}
