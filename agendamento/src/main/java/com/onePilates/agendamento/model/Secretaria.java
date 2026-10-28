package com.onePilates.agendamento.model;

import com.onePilates.agendamento.model.enums.Role;

import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class Secretaria extends Funcionario{


    public Secretaria() {
        this.setRole(Role.SECRETARIA);
    }

}
