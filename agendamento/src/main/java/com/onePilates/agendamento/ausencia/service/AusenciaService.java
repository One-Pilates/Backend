package com.onePilates.agendamento.ausencia.service;

import com.onePilates.agendamento.ausencia.dto.AusenciaDTO;
import com.onePilates.agendamento.ausencia.dto.AusenciaResponseDTO;
import com.onePilates.agendamento.ausencia.repository.AusenciaRepository;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.model.Ausencia;
import com.onePilates.agendamento.model.Professor;
import com.onePilates.agendamento.professor.repository.ProfessorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AusenciaService {

    private static final Logger logger = LoggerFactory.getLogger(AusenciaService.class);

    private final AusenciaRepository ausenciaRepository;
    private final ProfessorRepository professorRepository;

    public AusenciaService(AusenciaRepository ausenciaRepository, ProfessorRepository professorRepository) {
        this.ausenciaRepository = ausenciaRepository;
        this.professorRepository = professorRepository;
    }

    @Transactional
    public AusenciaResponseDTO registrarAusencia(AusenciaDTO dto) {
        logger.info("Tentativa de registrar ausência para professor ID: {}", dto.getProfessorId());
        try {
            Professor professor = professorRepository.findById(dto.getProfessorId())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Professor não encontrado"));

            Ausencia ausencia = new Ausencia();
            ausencia.setProfessor(professor);
            ausencia.setDataInicio(dto.getDataInicio());
            ausencia.setDataFim(dto.getDataFim());
            ausencia.setDiaSemanaInicio(dto.getDiaSemanaInicio());
            ausencia.setDiaSemanaFim(dto.getDiaSemanaFim());
            ausencia.setMotivo(dto.getMotivo());

            Ausencia saved = ausenciaRepository.save(ausencia);
            logger.info("Ausência registrada com sucesso. ID: {}", saved.getId());
            return toResponseDTO(saved);
        } catch (Exception e) {
            logger.error("Erro ao registrar ausência", e);
            throw e;
        }
    }

    public List<AusenciaResponseDTO> listarPorProfessor(Long professorId) {
        logger.debug("Listando ausências para professor ID: {}", professorId);
        List<AusenciaResponseDTO> ausencias = ausenciaRepository.findByProfessorId(professorId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        logger.debug("Encontradas {} ausências para professor ID: {}", ausencias.size(), professorId);
        return ausencias;
    }

    private AusenciaResponseDTO toResponseDTO(Ausencia ausencia) {
        AusenciaResponseDTO dto = new AusenciaResponseDTO();
        dto.setId(ausencia.getId());
        dto.setNomeProfessor(ausencia.getProfessor().getNome());
        dto.setDataInicio(ausencia.getDataInicio());
        dto.setDataFim(ausencia.getDataFim());
        dto.setDiaSemanaInicio(ausencia.getDiaSemanaInicio());
        dto.setDiaSemanaFim(ausencia.getDiaSemanaFim());
        dto.setMotivo(ausencia.getMotivo());
        return dto;
    }

    @Transactional
    public AusenciaResponseDTO atualizarAusencia(Integer id, AusenciaDTO dto) {
        logger.info("Tentativa de atualizar ausência ID: {}", id);
        try {
            Ausencia ausencia = ausenciaRepository.findById(id)
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Ausência não encontrada"));
            if (dto.getProfessorId() != null) {
                Professor professor = professorRepository.findById(dto.getProfessorId())
                        .orElseThrow(() -> new EntidadeNaoEncontradaException("Professor não encontrado"));
                ausencia.setProfessor(professor);
            }
            if (dto.getDataInicio() != null) ausencia.setDataInicio(dto.getDataInicio());
            if (dto.getDataFim() != null) ausencia.setDataFim(dto.getDataFim());
            if (dto.getDiaSemanaInicio() != null) ausencia.setDiaSemanaInicio(dto.getDiaSemanaInicio());
            if (dto.getDiaSemanaFim() != null) ausencia.setDiaSemanaFim(dto.getDiaSemanaFim());
            if (dto.getMotivo() != null) ausencia.setMotivo(dto.getMotivo());
            AusenciaResponseDTO response = toResponseDTO(ausenciaRepository.save(ausencia));
            logger.info("Ausência atualizada com sucesso. ID: {}", id);
            return response;
        } catch (Exception e) {
            logger.error("Erro ao atualizar ausência ID: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public void deletarAusencia(Integer id) {
        logger.info("Tentativa de deletar ausência ID: {}", id);
        try {
            Ausencia ausencia = ausenciaRepository.findById(id)
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Ausência não encontrada"));
            ausenciaRepository.delete(ausencia);
            logger.info("Ausência deletada com sucesso. ID: {}", id);
        } catch (Exception e) {
            logger.error("Erro ao deletar ausência ID: {}", id, e);
            throw e;
        }
    }


}
