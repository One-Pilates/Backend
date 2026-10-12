package com.onePilates.agendamento.especialidade.repository;

import com.onePilates.agendamento.model.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EspecialidadeRepository extends JpaRepository<Especialidade, Long> {}
