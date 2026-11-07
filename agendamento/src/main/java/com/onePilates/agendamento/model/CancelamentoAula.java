package com.onePilates.agendamento.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Cancelamento feito pelo próprio aluno. Guarda uma cópia dos dados da aula porque o vínculo
 * com o agendamento é removido; serve para o histórico do aluno e para saber se a aula foi devolvida.
 */
@Entity
@Table(name = "cancelamento_aula")
public class CancelamentoAula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    private Long agendamentoId;

    private LocalDateTime dataHoraAula;

    private String especialidade;

    private String professor;

    private String sala;

    private LocalDateTime canceladoEm;

    private boolean creditoDevolvido;

    /** Cancelou com menos de 24h de antecedência: a aula conta como usada. */
    private boolean foraDoPrazo;

    public CancelamentoAula() {
    }

    public boolean isForaDoPrazo() {
        return foraDoPrazo;
    }

    public CancelamentoAula(Aluno aluno, Agendamento agendamento, boolean creditoDevolvido, boolean foraDoPrazo) {
        this.foraDoPrazo = foraDoPrazo;
        this.aluno = aluno;
        this.agendamentoId = agendamento.getId();
        this.dataHoraAula = agendamento.getDataHora();
        this.especialidade = agendamento.getEspecialidade().getNome();
        this.professor = agendamento.getProfessor().getNome();
        this.sala = agendamento.getSala().getNome();
        this.canceladoEm = LocalDateTime.now();
        this.creditoDevolvido = creditoDevolvido;
    }

    public Long getId() {
        return id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public Long getAgendamentoId() {
        return agendamentoId;
    }

    public LocalDateTime getDataHoraAula() {
        return dataHoraAula;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public String getProfessor() {
        return professor;
    }

    public String getSala() {
        return sala;
    }

    public LocalDateTime getCanceladoEm() {
        return canceladoEm;
    }

    public boolean isCreditoDevolvido() {
        return creditoDevolvido;
    }
}
