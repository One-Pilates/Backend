package com.onePilates.agendamento.endereco.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class EnderecoDTO {

    @NotBlank(message = "O campo logradouro não pode estar vazio")
    private String rua;
    @NotBlank(message = "O campo numero não pode estar vazio ")
    private String numero;
    @NotBlank(message = "O Campo bairro não pode estar vazio")
    private String bairro;
    @NotBlank(message = "o campo cidade não pode estar vazio")
    private String cidade;
    @NotBlank(message = "O estado não pode estar vazio")
    private String estado;
    @NotBlank(message = "O codigo não pode estar vazio")
    private String cep;
    @NotBlank(message = "O campo UF não pode estar vazio")
    @Pattern(
            regexp = "^[A-Z]{2}$",
            message = "O UF deve conter exatamente 2 letras maiúsculas (ex: SP, RJ, MG)"
    )
    private String uf;


    public EnderecoDTO(String rua, String numero, String bairro, String cidade, String estado, String cep, String uf) {
        this.rua = rua;
        this.numero = numero;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
        this.uf = uf;
    }

    public EnderecoDTO() {
    }


    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }
}
