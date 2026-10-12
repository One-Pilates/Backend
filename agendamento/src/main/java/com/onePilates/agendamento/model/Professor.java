package com.onePilates.agendamento.model;

import com.onePilates.agendamento.model.enums.Role;
import jakarta.persistence.Entity;
import java.time.LocalDate;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;



@Entity
public class Professor extends Funcionario{

    @ManyToMany
    @JoinTable(
            name = "professor_especialidade",
            joinColumns = @JoinColumn(name = "professor_id"),
            inverseJoinColumns = @JoinColumn(name = "especialidade_id")
    )
    private Set<Especialidade> especialidades = new HashSet<>();


    public Professor() {
        this.setRole(Role.PROFESSOR);
    }

    public Set<Especialidade> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(Set<Especialidade> especialidades) {
        this.especialidades = especialidades;
    }
}
