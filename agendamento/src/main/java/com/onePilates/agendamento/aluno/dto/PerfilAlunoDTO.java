package com.onePilates.agendamento.aluno.dto;

public class PerfilAlunoDTO {
    private String nome;
    private String cpf;
    private Boolean status;
    private Boolean notificacaoAtiva;
    private String tipoContato;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
    public Boolean getNotificacaoAtiva() { return notificacaoAtiva; }
    public void setNotificacaoAtiva(Boolean notificacaoAtiva) { this.notificacaoAtiva = notificacaoAtiva; }
    public String getTipoContato() { return tipoContato; }
    public void setTipoContato(String tipoContato) { this.tipoContato = tipoContato; }
}
