package com.onePilates.agendamento.model;

import jakarta.persistence.*;

/**
 * Saldo de aulas de um aluno em uma especialidade. O saldo é debitado ao agendar e pode ficar
 * negativo: o sistema avisa quando o aluno excede o pacote, mas não bloqueia o agendamento.
 */
@Entity
@Table(name = "pacote_aula",
        uniqueConstraints = @UniqueConstraint(columnNames = {"aluno_id", "especialidade_id"}))
public class PacoteAula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "especialidade_id")
    private Especialidade especialidade;

    private int saldo;

    public PacoteAula() {
    }

    public PacoteAula(Aluno aluno, Especialidade especialidade, int saldo) {
        this.aluno = aluno;
        this.especialidade = especialidade;
        this.saldo = saldo;
    }

    public Long getId() {
        return id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public Especialidade getEspecialidade() {
        return especialidade;
    }

    public int getSaldo() {
        return saldo;
    }

    public void setSaldo(int saldo) {
        this.saldo = saldo;
    }
}
