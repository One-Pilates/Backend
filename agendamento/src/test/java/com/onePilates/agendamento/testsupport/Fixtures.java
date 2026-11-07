package com.onePilates.agendamento.testsupport;

import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.AgendamentoAluno;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.model.Professor;
import com.onePilates.agendamento.model.Sala;
import com.onePilates.agendamento.model.enums.Role;

import java.time.LocalDateTime;
import java.util.HashSet;

/** Entidades prontas para os testes de pacote, portal do aluno e lembretes. */
public final class Fixtures {

    private Fixtures() {
    }

    public static Aluno aluno(Long id, String nome, String email) {
        Aluno aluno = new Aluno();
        aluno.setId(id);
        aluno.setNome(nome);
        aluno.setEmail(email);
        aluno.setStatus(true);
        aluno.setNotificacaoAtiva(true);
        return aluno;
    }

    public static Especialidade especialidade(Long id, String nome) {
        Especialidade especialidade = new Especialidade();
        especialidade.setId(id);
        especialidade.setNome(nome);
        return especialidade;
    }

    public static Professor professor(Long id, String nome) {
        Professor professor = new Professor();
        professor.setId(id);
        professor.setNome(nome);
        professor.setEmail("prof" + id + "@teste.com");
        professor.setRole(Role.PROFESSOR);
        professor.setNotificacaoAtiva(true);
        return professor;
    }

    public static Sala sala(Long id, String nome) {
        Sala sala = new Sala();
        sala.setId(id);
        sala.setNome(nome);
        return sala;
    }

    /** Aula sem alunos; use {@link #vincular} para colocar alunos nela. */
    public static Agendamento aula(Long id, LocalDateTime dataHora, Especialidade especialidade, Professor professor, Sala sala) {
        Agendamento aula = new Agendamento();
        aula.setId(id);
        aula.setDataHora(dataHora);
        aula.setEspecialidade(especialidade);
        aula.setProfessor(professor);
        aula.setSala(sala);
        aula.setAgendamentoAlunos(new HashSet<>());
        return aula;
    }

    public static AgendamentoAluno vincular(Agendamento aula, Aluno aluno) {
        AgendamentoAluno vinculo = new AgendamentoAluno(aula, aluno);
        aula.getAgendamentoAlunos().add(vinculo);
        return vinculo;
    }
}
