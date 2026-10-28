package com.onePilates.agendamento.email.dto;

public class PrimeiroAcessoEmailDTO {
    private String nomeFuncionario;
    private String senhaTemporaria;

    public PrimeiroAcessoEmailDTO(){

    }

    public String getNomeFuncionario() {
        return nomeFuncionario;
    }

    public void setNomeFuncionario(String nomeFuncionario) {
        this.nomeFuncionario = nomeFuncionario;
    }

    public String getSenhaTemporaria() {
        return senhaTemporaria;
    }

    public void setSenhaTemporaria(String senhaTemporaria) {
        this.senhaTemporaria = senhaTemporaria;
    }
}
