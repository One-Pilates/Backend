package com.onePilates.agendamento.ia.tool;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * As ferramentas rodam na thread do consumidor da fila, sem requisição HTTP e portanto sem open-in-view:
 * coleções lazy (ex.: alunos de um agendamento) só podem ser lidas dentro de uma transação.
 * Use para ler e converter entidades em dados simples sem envolver chamadas que já têm transação própria.
 */
@Component
public class LeituraTransacional {

    private final TransactionTemplate template;

    public LeituraTransacional(PlatformTransactionManager transactionManager) {
        this.template = new TransactionTemplate(transactionManager);
        this.template.setReadOnly(true);
    }

    public <T> T ler(Supplier<T> leitura) {
        return template.execute(status -> leitura.get());
    }
}
