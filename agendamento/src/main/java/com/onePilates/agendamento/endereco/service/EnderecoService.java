package com.onePilates.agendamento.endereco.service;

import com.onePilates.agendamento.endereco.dto.EnderecoDTO;
import com.onePilates.agendamento.endereco.repository.EnderecoRepository;
import com.onePilates.agendamento.model.Endereco;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnderecoService {

    private final EnderecoRepository repository;

    public EnderecoService(EnderecoRepository repository) {
        this.repository = repository;
    }

    public Endereco cadastrarEndereco(EnderecoDTO dto) {
        Endereco endereco = new Endereco(
                dto.getRua(),
                dto.getNumero(),
                dto.getBairro(),
                dto.getCidade(),
                dto.getEstado(),
                dto.getCep(),
                dto.getUf()
        );

        return repository.save(endereco);
    }

    public List<Endereco> verTodosOsEnderecos() {
        return repository.findAll();
    }
}



