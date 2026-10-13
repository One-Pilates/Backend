package com.onePilates.agendamento.ia.service;

/** Callback pelo qual o agente informa cada passo; implementado gravando em ia_acoes. */
public interface ProgressoAgente {

    /** Registra o início de um passo e devolve um identificador para finalizá-lo. */
    Long iniciar(String descricao);

    void finalizar(Long acaoId, boolean sucesso);

    ProgressoAgente NENHUM = new ProgressoAgente() {
        @Override
        public Long iniciar(String descricao) {
            return null;
        }

        @Override
        public void finalizar(Long acaoId, boolean sucesso) {
        }
    };
}
