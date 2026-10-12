package com.onePilates.agendamento.administrador.dto;

import com.onePilates.agendamento.endereco.dto.EnderecoDTO;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public class AdministradorDTO {
    private String nome;
    private String email;
    private String cpf;
    private LocalDate idade;
    private Boolean status;
    private String foto;
    private String observacoes;
    private Boolean notificacaoAtiva;
    private Boolean primeiro_acesso;
    private String senha;
    private String cargo;
    private EnderecoDTO endereco;
    private MultipartFile imagem;

    public AdministradorDTO() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getIdade() {
        return idade;
    }

    public void setIdade(LocalDate idade) {
        this.idade = idade;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Boolean getNotificacaoAtiva() {
        return notificacaoAtiva;
    }

    public void setNotificacaoAtiva(Boolean notificacaoAtiva) {
        this.notificacaoAtiva = notificacaoAtiva;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public EnderecoDTO getEndereco() {
        return endereco;
    }

    public void setEndereco(EnderecoDTO endereco) {
        this.endereco = endereco;
    }

    public MultipartFile getImagem() {
        return imagem;
    }

    public void setImagem(MultipartFile imagem) {
        this.imagem = imagem;
    }

    public Boolean getPrimeiro_acesso() {
        return primeiro_acesso;
    }

    public void setPrimeiro_acesso(Boolean primeiro_acesso) {
        this.primeiro_acesso = primeiro_acesso;
    }
}
