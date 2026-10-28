package com.onePilates.agendamento.model;

import com.onePilates.agendamento.model.enums.StatusPresenca;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "agendamento_aluno")
public class AgendamentoAluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "agendamento_id", nullable = false)
    private Agendamento agendamento;

    @ManyToOne
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPresenca statusPresenca = StatusPresenca.PENDENTE;

    private LocalDateTime dataRegistroPresenca;

    private String observacao;

    public AgendamentoAluno() {
    }

    public AgendamentoAluno(Agendamento agendamento, Aluno aluno) {
        this.agendamento = agendamento;
        this.aluno = aluno;
        this.statusPresenca = StatusPresenca.PENDENTE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Agendamento getAgendamento() {
        return agendamento;
    }

    public void setAgendamento(Agendamento agendamento) {
        this.agendamento = agendamento;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public StatusPresenca getStatusPresenca() {
        return statusPresenca;
    }

    public void setStatusPresenca(StatusPresenca statusPresenca) {
        this.statusPresenca = statusPresenca;
        if (statusPresenca != StatusPresenca.PENDENTE && this.dataRegistroPresenca == null) {
            this.dataRegistroPresenca = LocalDateTime.now();
        }
    }

    public LocalDateTime getDataRegistroPresenca() {
        return dataRegistroPresenca;
    }

    public void setDataRegistroPresenca(LocalDateTime dataRegistroPresenca) {
        this.dataRegistroPresenca = dataRegistroPresenca;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}

