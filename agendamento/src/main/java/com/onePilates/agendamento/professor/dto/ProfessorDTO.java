package com.onePilates.agendamento.professor.dto;

import com.onePilates.agendamento.endereco.dto.EnderecoDTO;
import com.onePilates.agendamento.model.enums.Role;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Set;

public class ProfessorDTO {

    @NotBlank(message = "O campo nome não pode ser nulo nem vazio")
    private String nome;


    @Email(message = "Coloque um email válido")
    @NotBlank(message = "O campo email é obrigatório")
    private String email;

    @CPF(message = "Coloque um CPF válido")
    @NotBlank(message = "O campo CPF é obrigatório")
    private String cpf;

    @Past(message = "A data de nascimento deve ser no passado")
    @NotNull(message = "O campo idade é obrigatório")
    private LocalDate idade;

    @NotNull(message = "O campo status é obrigatório")
    private Boolean status;

    private String foto;

    @Size(max = 500, message = "O campo observações deve ter no máximo 500 caracteres")
    private String observacoes;

    @NotNull(message = "O campo de notificação ativa é obrigatório")
    private Boolean notificacaoAtiva;

    @NotBlank(message = "O campo senha é obrigatório")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    private String senha;

    @NotBlank(message = "O campo cargo é obrigatório")
    private String cargo;

    @NotNull(message = "O campo role é obrigatório")
    private Role role;

    private EnderecoDTO endereco;

    private String telefone;

    private MultipartFile imagem;

    @NotEmpty(message = "É necessário informar ao menos uma especialidade")
    private Set<@NotNull(message = "ID de especialidade não pode ser nulo") Long> especialidadeIds;



    public ProfessorDTO() {
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

    public Set<Long> getEspecialidadeIds() {
        return especialidadeIds;
    }

    public void setEspecialidadeIds(Set<Long> especialidadeIds) {
        this.especialidadeIds = especialidadeIds;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public MultipartFile getImagem() {
        return imagem;
    }

    public void setImagem(MultipartFile imagem) {
        this.imagem = imagem;
    }
}
