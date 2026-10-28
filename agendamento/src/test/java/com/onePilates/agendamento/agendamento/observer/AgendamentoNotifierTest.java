package com.onePilates.agendamento.agendamento.observer;

import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.model.Professor;
import com.onePilates.agendamento.model.Sala;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoNotifierTest {

    @Mock
    private AgendamentoObserver observer1;

    @Mock
    private AgendamentoObserver observer2;

    @Mock
    private AgendamentoObserver observer3;

    private AgendamentoNotifier notifier;
    private Agendamento agendamento;

    @BeforeEach
    void setUp() {
        List<AgendamentoObserver> observers = new ArrayList<>();
        observers.add(observer1);
        observers.add(observer2);
        observers.add(observer3);
        notifier = new AgendamentoNotifier(observers);

        Professor professor = new Professor();
        professor.setNome("Professor Teste");

        Sala sala = new Sala();
        sala.setNome("Sala 1");

        Especialidade especialidade = new Especialidade();
        especialidade.setNome("Pilates Clássico");

        agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setProfessor(professor);
        agendamento.setSala(sala);
        agendamento.setEspecialidade(especialidade);
        agendamento.setDataHora(LocalDateTime.now());
    }

    @Test
    void notificarTodos_DeveNotificarTodosObservers() {
        notifier.notificarTodos(agendamento);

        verify(observer1, times(1)).notificar(agendamento);
        verify(observer2, times(1)).notificar(agendamento);
        verify(observer3, times(1)).notificar(agendamento);
    }

    @Test
    void notificarTodos_DeveFuncionar_QuandoListaObserversVazia() {
        AgendamentoNotifier notifierVazio = new AgendamentoNotifier(new ArrayList<>());

        notifierVazio.notificarTodos(agendamento);

        // Não deve lançar exceção
        verifyNoInteractions(observer1, observer2, observer3);
    }

    @Test
    void notificarTodos_DeveContinuar_QuandoUmObserverLancaExcecao() {
        doThrow(new RuntimeException("Erro no observer"))
                .when(observer2).notificar(agendamento);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            notifier.notificarTodos(agendamento);
        });

        assertEquals("Erro no observer", exception.getMessage());
        // Verifica que observer1 foi chamado antes da exceção
        verify(observer1, times(1)).notificar(agendamento);
        // Verifica que observer2 tentou ser chamado (mesmo que tenha lançado exceção)
        verify(observer2, times(1)).notificar(agendamento);
        // Verifica que observer3 não foi chamado porque a exceção interrompeu o loop
        verifyNoInteractions(observer3);
    }

    @Test
    void notificarTodos_DeveNotificar_QuandoApenasUmObserver() {
        List<AgendamentoObserver> observers = new ArrayList<>();
        observers.add(observer1);
        AgendamentoNotifier notifierUnico = new AgendamentoNotifier(observers);

        notifierUnico.notificarTodos(agendamento);

        verify(observer1, times(1)).notificar(agendamento);
        verifyNoInteractions(observer2, observer3);
    }

    @Test
    void notificarTodos_DevePassarMesmoAgendamentoParaTodos() {
        notifier.notificarTodos(agendamento);

        verify(observer1).notificar(same(agendamento));
        verify(observer2).notificar(same(agendamento));
        verify(observer3).notificar(same(agendamento));
    }
}

