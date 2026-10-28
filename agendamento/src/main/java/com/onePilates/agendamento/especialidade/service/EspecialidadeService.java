package com.onePilates.agendamento.especialidade.service;

import com.onePilates.agendamento.especialidade.dto.EspecialidadeDTO;
import com.onePilates.agendamento.especialidade.dto.EspecialidadeResponseDTO;
import com.onePilates.agendamento.especialidade.repository.EspecialidadeRepository;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.model.Professor;
import com.onePilates.agendamento.model.Sala;
import com.onePilates.agendamento.professor.dto.ProfessorPorEspecialidadeResponseDTO;
import com.onePilates.agendamento.professor.repository.ProfessorRepository;
import com.onePilates.agendamento.sala.dto.SalasPorEspecialidadeResponseDTO;
import com.onePilates.agendamento.sala.repository.SalaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EspecialidadeService {

    private static final Logger logger = LoggerFactory.getLogger(EspecialidadeService.class);

    private final EspecialidadeRepository especialidadeRepository;
    private final ProfessorRepository professorRepository;
    private final SalaRepository salaRepository;

    public EspecialidadeService(
            EspecialidadeRepository especialidadeRepository,
            ProfessorRepository professorRepository,
            SalaRepository salaRepository
    ) {
        this.especialidadeRepository = especialidadeRepository;
        this.professorRepository = professorRepository;
        this.salaRepository = salaRepository;
    }


    @Transactional
    public Especialidade criarEspecialidade(EspecialidadeDTO dto) {
        logger.info("Tentativa de criar especialidade: {}", dto.getNome());
        try {
            Especialidade especialidade = new Especialidade();
            especialidade.setNome(dto.getNome());
            Especialidade saved = especialidadeRepository.save(especialidade);
            logger.info("Especialidade criada com sucesso. ID: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            logger.error("Erro ao criar especialidade", e);
            throw e;
        }
    }

    public List<EspecialidadeResponseDTO> listarTodasDTO() {
        logger.debug("Listando todas as especialidades");
        List<EspecialidadeResponseDTO> especialidades = especialidadeRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        logger.debug("Encontradas {} especialidades", especialidades.size());
        return especialidades;
    }

    public EspecialidadeResponseDTO buscarPorIdDTO(Long id) {
        logger.debug("Buscando especialidade por ID: {}", id);
        return toResponseDTO(buscarPorId(id));
    }

    @Transactional
    public EspecialidadeResponseDTO atualizarEspecialidade(Long id, EspecialidadeDTO dto) {
        logger.info("Tentativa de atualizar especialidade ID: {}", id);
        try {
            Especialidade especialidade = buscarPorId(id);
            if (dto.getNome() != null) especialidade.setNome(dto.getNome());
            EspecialidadeResponseDTO response = toResponseDTO(especialidadeRepository.save(especialidade));
            logger.info("Especialidade atualizada com sucesso. ID: {}", id);
            return response;
        } catch (Exception e) {
            logger.error("Erro ao atualizar especialidade ID: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public void excluirEspecialidade(Long id) {
        logger.info("Tentativa de excluir especialidade ID: {}", id);
        try {
            if (!especialidadeRepository.existsById(id)) {
                throw new EntidadeNaoEncontradaException("Especialidade não encontrada");
            }
            especialidadeRepository.deleteById(id);
            logger.info("Especialidade excluída com sucesso. ID: {}", id);
        } catch (Exception e) {
            logger.error("Erro ao excluir especialidade ID: {}", id, e);
            throw e;
        }
    }

    private Especialidade buscarPorId(Long id) {
        return especialidadeRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Especialidade não encontrada"));
    }

    public EspecialidadeResponseDTO toResponseDTO(Especialidade especialidade) {
        EspecialidadeResponseDTO dto = new EspecialidadeResponseDTO();
        dto.setId(especialidade.getId());
        dto.setNome(especialidade.getNome());
        return dto;
    }

    public List<ProfessorPorEspecialidadeResponseDTO> BuscarProfessor(Long id) {
        List<Professor> professores = professorRepository.findByEspecialidadesId(id);

        return professores.stream()
                .map(professor -> new ProfessorPorEspecialidadeResponseDTO(professor.getId(), professor
                        .getNome()))
                .collect(Collectors.toList());
    }

    public List<SalasPorEspecialidadeResponseDTO>buscarSalasPorEspecialidade(Long id) {

        List<Sala> salas = salaRepository.findByEspecialidadesId(id);

        return salas.stream()
                .map(sala -> new SalasPorEspecialidadeResponseDTO(sala.getId(), sala
                        .getNome()))
                .collect(Collectors.toList());
    }

}
