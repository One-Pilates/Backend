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

    /** O aluno confirmou, pelo portal, que vai comparecer. Não é a chamada do professor. */
    private Boolean presencaConfirmada;

    private LocalDateTime dataConfirmacao;

    /** Quando o e-mail de lembrete (48h) foi enviado; evita reenvio. */
    private LocalDateTime lembreteEnviadoEm;

    /** Este vínculo consumiu 1 aula do pacote. Vínculos antigos (nulo) nunca devolvem aula. */
    private Boolean creditoDebitado;

    public AgendamentoAluno() {
    }

    public boolean isPresencaConfirmada() {
        return Boolean.TRUE.equals(presencaConfirmada);
    }

    public void confirmarPresenca() {
        this.presencaConfirmada = true;
        this.dataConfirmacao = LocalDateTime.now();
    }

    public LocalDateTime getDataConfirmacao() {
        return dataConfirmacao;
    }

    public LocalDateTime getLembreteEnviadoEm() {
        return lembreteEnviadoEm;
    }

    public void setLembreteEnviadoEm(LocalDateTime lembreteEnviadoEm) {
        this.lembreteEnviadoEm = lembreteEnviadoEm;
    }

    public boolean isCreditoDebitado() {
        return Boolean.TRUE.equals(creditoDebitado);
    }

    public void setCreditoDebitado(boolean creditoDebitado) {
        this.creditoDebitado = creditoDebitado;
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

