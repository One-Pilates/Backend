package com.onePilates.agendamento.ia.tool;

import java.util.Map;

/** Conversões dos argumentos que o modelo envia (números podem chegar como Integer, Long ou texto). */
final class ArgumentosTool {

    private ArgumentosTool() {}

    /** Map.of não aceita null: nomes ausentes viram "-" em vez de derrubar a ferramenta com NullPointerException. */
    static String texto(String valor) {
        return valor == null ? "-" : valor;
    }

    static Long longOpcional(Map<String, Object> args, String chave) {
        Object v = args.get(chave);
        if (v == null || v.toString().isBlank()) return null;
        try {
            return v instanceof Number n ? n.longValue() : Long.parseLong(v.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("O argumento " + chave + " deve ser um número");
        }
    }

    static Long longObrigatorio(Map<String, Object> args, String chave) {
        Long v = longOpcional(args, chave);
        if (v == null) throw new IllegalArgumentException("O argumento " + chave + " é obrigatório");
        return v;
    }
}
