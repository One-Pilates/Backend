package com.onePilates.agendamento.sala.service;

import com.onePilates.agendamento.especialidade.repository.EspecialidadeRepository;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.model.Sala;
import com.onePilates.agendamento.sala.dto.SalaDTO;
import com.onePilates.agendamento.sala.dto.SalaResponseDTO;
import com.onePilates.agendamento.sala.repository.SalaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalaService {

    private static final Logger logger = LoggerFactory.getLogger(SalaService.class);

    private final SalaRepository salaRepository;
    private final EspecialidadeRepository especialidadeRepository;

    public SalaService(SalaRepository salaRepository, EspecialidadeRepository especialidadeRepository) {
        this.salaRepository = salaRepository;
        this.especialidadeRepository = especialidadeRepository;
    }

    @Transactional
    public Sala criarSala(SalaDTO dto) {
        logger.info("Tentativa de criar sala: {}", dto.getNome());
        try {
            Sala sala = mapDtoToEntity(dto);
            logger.info("Sala criada com sucesso. ID: {}", sala.getId());
            return sala;
        } catch (Exception e) {
            logger.error("Erro ao criar sala", e);
            throw e;
        }
    }

    public List<SalaResponseDTO> listarTodasDTO() {
        logger.debug("Listando todas as salas");
        List<SalaResponseDTO> salas = salaRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        logger.debug("Encontradas {} salas", salas.size());
        return salas;
    }

    public SalaResponseDTO buscarPorIdDTO(Long id) {
        logger.debug("Buscando sala por ID: {}", id);
        return toResponseDTO(buscarPorId(id));
    }

    @Transactional
    public SalaResponseDTO atualizarSala(Long id, SalaDTO dto) {
        logger.info("Tentativa de atualizar sala ID: {}", id);
        try {
            Sala sala = buscarPorId(id);
            if (dto.getNome() != null) sala.setNome(dto.getNome());
            if (dto.getQuantidadeMaximaAlunos() != null) sala.setQuantidadeMaximaAlunos(dto.getQuantidadeMaximaAlunos());
            if (dto.getQuantidadeEquipamentosPCD() != null) sala.setQuantidadeEquipamentosPCD(dto.getQuantidadeEquipamentosPCD());
            if (dto.getEspecialidadeIds() != null) {
                Set<Especialidade> especialidades = dto.getEspecialidadeIds().stream()
                        .map(idEsp -> especialidadeRepository.findById(idEsp)
                                .orElseThrow(() -> new EntidadeNaoEncontradaException("Especialidade não encontrada: " + idEsp)))
                        .collect(Collectors.toSet());
                sala.setEspecialidades(especialidades);
            }
            SalaResponseDTO response = toResponseDTO(salaRepository.save(sala));
            logger.info("Sala atualizada com sucesso. ID: {}", id);
            return response;
        } catch (Exception e) {
            logger.error("Erro ao atualizar sala ID: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public void excluirSala(Long id) {
        logger.info("Tentativa de excluir sala ID: {}", id);
        try {
            if (!salaRepository.existsById(id)) {
                throw new EntidadeNaoEncontradaException("Sala não encontrada");
            }
            salaRepository.deleteById(id);
            logger.info("Sala excluída com sucesso. ID: {}", id);
        } catch (Exception e) {
            logger.error("Erro ao excluir sala ID: {}", id, e);
            throw e;
        }
    }

    private Sala buscarPorId(Long id) {
        return salaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Sala não encontrada"));
    }

    private Sala mapDtoToEntity(SalaDTO dto) {
        Sala sala = new Sala();
        sala.setNome(dto.getNome());
        sala.setQuantidadeMaximaAlunos(dto.getQuantidadeMaximaAlunos());
        sala.setQuantidadeEquipamentosPCD(dto.getQuantidadeEquipamentosPCD());

        Set<Especialidade> especialidades = dto.getEspecialidadeIds().stream()
                .map(id -> especialidadeRepository.findById(id)
                        .orElseThrow(() -> new EntidadeNaoEncontradaException("Especialidade não encontrada: " + id)))
                .collect(Collectors.toSet());
        sala.setEspecialidades(especialidades);

        return salaRepository.save(sala);
    }

    public SalaResponseDTO toResponseDTO(Sala sala) {
        SalaResponseDTO dto = new SalaResponseDTO();
        dto.setId(sala.getId());
        dto.setNome(sala.getNome());
        dto.setQuantidadeMaximaAlunos(sala.getQuantidadeMaximaAlunos());
        dto.setQuantidadeEquipamentosPCD(sala.getQuantidadeEquipamentosPCD());
        dto.setEspecialidades(sala.getEspecialidades()
                .stream()
                .map(Especialidade::getNome)
                .collect(Collectors.toSet()));
        return dto;
    }
}