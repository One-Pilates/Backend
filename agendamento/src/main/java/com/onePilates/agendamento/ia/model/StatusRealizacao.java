package com.onePilates.agendamento.ia.model;

public enum StatusRealizacao {
    PENDENTE,
    PROCESSANDO,
    CONCLUIDO,
    ERRO;

    public boolean emAndamento() {
        return this == PENDENTE || this == PROCESSANDO;
    }
}
