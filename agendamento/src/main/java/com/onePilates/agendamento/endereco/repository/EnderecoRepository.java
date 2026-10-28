package com.onePilates.agendamento.endereco.repository;

import com.onePilates.agendamento.model.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnderecoRepository  extends JpaRepository<Endereco,Long> {


}
