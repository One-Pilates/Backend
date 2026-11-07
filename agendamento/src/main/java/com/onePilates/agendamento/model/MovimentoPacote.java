package com.onePilates.agendamento.model;

import com.onePilates.agendamento.model.enums.MotivoMovimentoPacote;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Registro de cada alteração no saldo do pacote: quem, quando, quanto e por quê. */
@Entity
@Table(name = "movimento_pacote")
public class MovimentoPacote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "especialidade_id")
    private Especialidade especialidade;

    /** Variação aplicada ao saldo (negativa debita, positiva credita). */
    private int variacao;

    private int saldoApos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MotivoMovimentoPacote motivo;

    /** Agendamento que originou o movimento, quando houver (sem FK: a aula pode ser excluída). */
    private Long agendamentoId;

    /** E-mail de quem fez a alteração (secretaria/administrador) ou do próprio aluno. */
    private String responsavel;

    private LocalDateTime criadoEm;

    public MovimentoPacote() {
    }

    public MovimentoPacote(Aluno aluno, Especialidade especialidade, int variacao, int saldoApos,
                           MotivoMovimentoPacote motivo, Long agendamentoId, String responsavel) {
        this.aluno = aluno;
        this.especialidade = especialidade;
        this.variacao = variacao;
        this.saldoApos = saldoApos;
        this.motivo = motivo;
        this.agendamentoId = agendamentoId;
        this.responsavel = responsavel;
        this.criadoEm = LocalDateTime.now();
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

    public int getVariacao() {
        return variacao;
    }

    public int getSaldoApos() {
        return saldoApos;
    }

    public MotivoMovimentoPacote getMotivo() {
        return motivo;
    }

    public Long getAgendamentoId() {
        return agendamentoId;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
