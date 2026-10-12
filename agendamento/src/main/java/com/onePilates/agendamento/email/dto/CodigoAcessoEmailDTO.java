package com.onePilates.agendamento.email.dto;

public class CodigoAcessoEmailDTO {
    private String nomeFuncionario;
    private String codigo;

    public CodigoAcessoEmailDTO() {
    }

    public String getNomeFuncionario() {
        return nomeFuncionario;
    }

    public void setNomeFuncionario(String nomeFuncionario) {
        this.nomeFuncionario = nomeFuncionario;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
