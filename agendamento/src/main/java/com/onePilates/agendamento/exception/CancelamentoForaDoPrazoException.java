package com.onePilates.agendamento.exception;

public class CancelamentoForaDoPrazoException extends BusinessException {

    public CancelamentoForaDoPrazoException() {
        super("Não é possível cancelar a aula com menos de 24 horas de antecedência.", "CANCELAMENTO_FORA_DO_PRAZO");
    }
}
