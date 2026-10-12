package com.onePilates.agendamento.model;

import com.onePilates.agendamento.model.enums.Role;
import jakarta.persistence.Entity;
import java.time.LocalDate;

@Entity
public class Administrador extends Funcionario{
    public Administrador() {
        this.setRole(Role.ADMINISTRADOR);
    }


}
