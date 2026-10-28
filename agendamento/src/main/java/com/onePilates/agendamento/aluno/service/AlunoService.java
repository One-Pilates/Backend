package com.onePilates.agendamento.aluno.service;

import com.onePilates.agendamento.aluno.dto.AlunoDTO;
import com.onePilates.agendamento.aluno.dto.AlunoPaginadoResponseDTO;
import com.onePilates.agendamento.aluno.dto.AlunoResponseDTO;
import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.endereco.dto.EnderecoDTO;
import com.onePilates.agendamento.endereco.dto.EnderecoResponseDTO;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.Endereco;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class AlunoService {

    private static final Logger logger = LoggerFactory.getLogger(AlunoService.class);

    private final AlunoRepository alunoRepository;

    public AlunoService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    @Transactional
    public Aluno criarAluno(AlunoDTO dto) {
        logger.info("Tentativa de criar aluno: {}", dto.getNome());
        try {
            Aluno aluno = alunoRepository.save(mapDtoToEntity(dto));
            logger.info("Aluno criado com sucesso. ID: {}", aluno.getId());
            return aluno;
        } catch (Exception e) {
            logger.error("Erro ao criar aluno", e);
            throw e;
        }
    }

    public AlunoPaginadoResponseDTO listarTodosPaginadosDTO(Pageable pageable, String nome, Boolean status) {
        logger.debug("Listando alunos com paginacao");
        boolean temNome = nome != null && !nome.isBlank();
        boolean temStatus = status != null;

        var pagina = temNome && temStatus
                ? alunoRepository.findByNomeContainingIgnoreCaseAndStatus(nome, status, pageable)
                : temNome
                    ? alunoRepository.findByNomeContainingIgnoreCase(nome, pageable)
                    : temStatus
                        ? alunoRepository.findByStatus(status, pageable)
                        : alunoRepository.findAll(pageable);
        var paginaDto = pagina.map(this::toResponseDTO);
        logger.debug("Encontrados {} alunos na pagina", paginaDto.getNumberOfElements());
        return new AlunoPaginadoResponseDTO(
                paginaDto.getContent(),
                paginaDto.getTotalElements(),
                paginaDto.getTotalPages()
        );
    }

    public List<AlunoResponseDTO> listarTodosDTO() {
        logger.debug("Listando todos os alunos");
        List<AlunoResponseDTO> alunos = alunoRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        logger.debug("Encontrados {} alunos", alunos.size());
        return alunos;

    }

    public AlunoResponseDTO buscarPorIdDTO(Long id) {
        logger.debug("Buscando aluno por ID: {}", id);
        return toResponseDTO(buscarPorId(id));
    }

    @Transactional
    public AlunoResponseDTO atualizarAluno(Long id, AlunoDTO dto) {
        logger.info("Tentativa de atualizar aluno ID: {}", id);
        try {
            Aluno aluno = buscarPorId(id);

            if (dto.getNome() != null) aluno.setNome(dto.getNome());
            if (dto.getEmail() != null) aluno.setEmail(dto.getEmail());
            if (dto.getCpf() != null) aluno.setCpf(dto.getCpf());
            if (dto.getDataNascimento() != null) aluno.setDataNascimento(dto.getDataNascimento());
            if (dto.getStatus() != null) aluno.setStatus(dto.getStatus());
            if (dto.getAlunoComLimitacoesFisicas() != null) aluno.setAlunoComLimitacoesFisicas(dto.getAlunoComLimitacoesFisicas());
            if (dto.getTelefone() != null) aluno.setTipoContato(dto.getTelefone());
            if (dto.getNotificacaoAtiva() != null) aluno.setNotificacaoAtiva(dto.getNotificacaoAtiva());
            if (dto.getObservacao() != null) aluno.setObservacao(dto.getObservacao());

            if (dto.getEndereco() != null) {
                Endereco endereco = aluno.getEndereco() != null ? aluno.getEndereco() : new Endereco();
                EnderecoDTO e = dto.getEndereco();
                if (e.getRua() != null) endereco.setRua(e.getRua());
                if (e.getCidade() != null) endereco.setCidade(e.getCidade());
                if (e.getEstado() != null) endereco.setEstado(e.getEstado());
                if (e.getCep() != null) endereco.setCep(e.getCep());
                aluno.setEndereco(endereco);
            }

            AlunoResponseDTO response = toResponseDTO(alunoRepository.save(aluno));
            logger.info("Aluno atualizado com sucesso. ID: {}", id);
            return response;
        } catch (Exception e) {
            logger.error("Erro ao atualizar aluno ID: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public void excluirAluno(Long id) {
        logger.info("Tentativa de excluir aluno ID: {}", id);
        try {
            if (!alunoRepository.existsById(id)) {
                throw new EntidadeNaoEncontradaException("Aluno não encontrado");
            }
            alunoRepository.deleteById(id);
            logger.info("Aluno excluído com sucesso. ID: {}", id);
        } catch (Exception e) {
            logger.error("Erro ao excluir aluno ID: {}", id, e);
            throw e;
        }
    }

    private Aluno buscarPorId(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Aluno não encontrado"));
    }

    private Aluno mapDtoToEntity(AlunoDTO dto) {
        Aluno aluno = new Aluno();
        aluno.setNome(dto.getNome());
        aluno.setEmail(dto.getEmail());
        aluno.setCpf(dto.getCpf());
        aluno.setDataNascimento(dto.getDataNascimento());
        aluno.setStatus(dto.getStatus());
        aluno.setAlunoComLimitacoesFisicas(dto.getAlunoComLimitacoesFisicas());
        aluno.setTipoContato(dto.getTelefone());
        aluno.setNotificacaoAtiva(dto.getNotificacaoAtiva());
        aluno.setObservacao(dto.getObservacao());


        EnderecoDTO enderecoDTO = dto.getEndereco();
        Endereco endereco = new Endereco();
        endereco.setRua(enderecoDTO.getRua());
        endereco.setCidade(enderecoDTO.getCidade());
        endereco.setEstado(enderecoDTO.getEstado());
        endereco.setCep(enderecoDTO.getCep());
        endereco.setBairro(enderecoDTO.getBairro());
        endereco.setUf(enderecoDTO.getUf());
        endereco.setNumero(enderecoDTO.getNumero());
        endereco.setBairro(enderecoDTO.getBairro());
        endereco.setUf(enderecoDTO.getUf());
        endereco.setNumero(enderecoDTO.getNumero());
        aluno.setEndereco(endereco);

        return aluno;
    }

    public AlunoResponseDTO toResponseDTO(Aluno aluno) {
        AlunoResponseDTO dto = new AlunoResponseDTO();
        dto.setId(aluno.getId());
        dto.setNome(aluno.getNome());
        dto.setEmail(aluno.getEmail());
        dto.setCpf(aluno.getCpf());
        dto.setDataNascimento(aluno.getDataNascimento());
        dto.setStatus(aluno.getStatus());
        dto.setAlunoComLimitacoesFisicas(aluno.getAlunoComLimitacoesFisicas());
        dto.setTelefone(aluno.getTipoContato());
        dto.setNotificacaoAtiva(aluno.getNotificacaoAtiva());
        dto.setObservacao(aluno.getObservacao());

        EnderecoResponseDTO enderecoDTO = new EnderecoResponseDTO();
        enderecoDTO.setRua(aluno.getEndereco().getRua());
        enderecoDTO.setCidade(aluno.getEndereco().getCidade());
        enderecoDTO.setEstado(aluno.getEndereco().getEstado());
        enderecoDTO.setCep(aluno.getEndereco().getCep());
        enderecoDTO.setBairro(aluno.getEndereco().getBairro());
        enderecoDTO.setUf(aluno.getEndereco().getUf());
        enderecoDTO.setNumero(aluno.getEndereco().getNumero());
        dto.setEndereco(enderecoDTO);

        return dto;
    }
}
