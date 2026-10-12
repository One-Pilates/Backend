package com.onePilates.agendamento.sala.dto;

import java.util.Set;


public class SalaResponseDTO {
        private Long id;
        private String nome;
        private Set<String> especialidades;
        private Integer quantidadeMaximaAlunos;
        private Integer quantidadeEquipamentosPCD;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public Set<String> getEspecialidades() {
            return especialidades;
        }

        public void setEspecialidades(Set<String> especialidades) {
            this.especialidades = especialidades;
        }

        public Integer getQuantidadeMaximaAlunos() {
            return quantidadeMaximaAlunos;
        }

        public void setQuantidadeMaximaAlunos(Integer quantidadeMaximaAlunos) {
            this.quantidadeMaximaAlunos = quantidadeMaximaAlunos;
        }

        public Integer getQuantidadeEquipamentosPCD() {
            return quantidadeEquipamentosPCD;
        }

        public void setQuantidadeEquipamentosPCD(Integer quantidadeEquipamentosPCD) {
            this.quantidadeEquipamentosPCD = quantidadeEquipamentosPCD;
        }
}

