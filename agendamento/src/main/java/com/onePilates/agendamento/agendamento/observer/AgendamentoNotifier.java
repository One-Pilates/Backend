package com.onePilates.agendamento.agendamento.observer;

import com.onePilates.agendamento.model.Agendamento;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgendamentoNotifier {

    private final List<AgendamentoObserver> observers;

    public AgendamentoNotifier(List<AgendamentoObserver> observers) {
        this.observers = observers;
    }

    public void notificarTodos(Agendamento agendamento) {
        observers.forEach(o -> o.notificar(agendamento));
    }
}