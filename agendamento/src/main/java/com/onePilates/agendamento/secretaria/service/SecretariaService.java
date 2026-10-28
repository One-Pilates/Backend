package com.onePilates.agendamento.secretaria.service;

import com.onePilates.agendamento.agendamento.dto.AgendamentoPorDiaDTO;
import com.onePilates.agendamento.agendamento.repository.AgendamentoRepository;
import com.onePilates.agendamento.dashboard.dto.ResponsDashSecretariaAdmDTO;
import com.onePilates.agendamento.email.dto.EmailRequestDTO;
import com.onePilates.agendamento.email.dto.PrimeiroAcessoEmailDTO;
import com.onePilates.agendamento.email.service.RabbitMQProducer;
import com.onePilates.agendamento.endereco.dto.EnderecoResponseDTO;
import com.onePilates.agendamento.endereco.repository.EnderecoRepository;
import com.onePilates.agendamento.exception.BusinessException;
import com.onePilates.agendamento.exception.CpfJaCadastradoException;
import com.onePilates.agendamento.exception.EmailJaCadastradoException;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.imagem.service.ImagemService;
import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.AgendamentoAluno;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.Endereco;
import com.onePilates.agendamento.model.Secretaria;
import com.onePilates.agendamento.model.enums.Role;
import com.onePilates.agendamento.model.enums.TipoEmail;
import com.onePilates.agendamento.professor.dto.AgendamentosPorProfessorDTO;
import com.onePilates.agendamento.secretaria.dto.SecretariaDTO;
import com.onePilates.agendamento.secretaria.dto.SecretariaResponseDTO;
import com.onePilates.agendamento.secretaria.repository.SecretariaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SecretariaService {

    private static final Logger logger = LoggerFactory.getLogger(SecretariaService.class);

    private final SecretariaRepository secretariaRepository;
    private final EnderecoRepository enderecoRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImagemService imagemService;
    private final RabbitMQProducer rabbitMQ;
    private final AgendamentoRepository agendamentoRepository;

    public SecretariaService(SecretariaRepository secretariaRepository, EnderecoRepository enderecoRepository, PasswordEncoder passwordEncoder, ImagemService imagemService, RabbitMQProducer rabbitMQ, AgendamentoRepository agendamentoRepository) {
        this.secretariaRepository = secretariaRepository;
        this.enderecoRepository = enderecoRepository;
        this.passwordEncoder = passwordEncoder;
        this.imagemService = imagemService;
        this.rabbitMQ = rabbitMQ;
        this.agendamentoRepository = agendamentoRepository;
    }

    @Transactional
    public Secretaria criarSecretaria(SecretariaDTO dto) {
        logger.info("Tentativa de criar secretária: {}", dto.getNome());
        try {
            if (secretariaRepository.existsByEmail(dto.getEmail())) {
                throw new EmailJaCadastradoException("Email já cadastrado");
            }
            if (secretariaRepository.existsByCpf(dto.getCpf())) {
                throw new CpfJaCadastradoException("CPF já cadastrado");
            }

            Secretaria secretaria = new Secretaria();
            secretaria.setNome(dto.getNome());
            secretaria.setEmail(dto.getEmail());
            secretaria.setCpf(dto.getCpf());
            secretaria.setDataNascimento(dto.getIdade());
            secretaria.setStatus(dto.getStatus());
            secretaria.setObservacoes(dto.getObservacoes());
            secretaria.setNotificacaoAtiva(dto.getNotificacaoAtiva());
            secretaria.setSenha(passwordEncoder.encode(dto.getSenha()));
            secretaria.setCargo(dto.getCargo());
            secretaria.setRole(dto.getRole() != null ? dto.getRole() : Role.SECRETARIA);
            secretaria.setTelefone(dto.getTelefone());
            secretaria.setPrimeiroAcesso(true);

            if (dto.getEndereco() != null) {
                Endereco endereco = new Endereco();
                endereco.setRua(dto.getEndereco().getRua());
                endereco.setNumero(dto.getEndereco().getNumero());
                endereco.setBairro(dto.getEndereco().getBairro());
                endereco.setCidade(dto.getEndereco().getCidade());
                endereco.setEstado(dto.getEndereco().getEstado());
                endereco.setCep(dto.getEndereco().getCep());
                endereco.setUf(dto.getEndereco().getUf());
                endereco = enderecoRepository.save(endereco);
                secretaria.setEndereco(endereco);
            }

            // Salva a secretária primeiro para obter o ID
            Secretaria saved = secretariaRepository.save(secretaria);

            // Processa imagem se fornecida (após salvar para ter o ID)
            if (dto.getImagem() != null && !dto.getImagem().isEmpty()) {
                try {
                    String caminhoFoto = imagemService.uploadFotoSecretaria(dto.getImagem(), saved.getId());
                    saved.setFoto(caminhoFoto);
                    saved = secretariaRepository.save(saved);
                } catch (Exception e) {
                    logger.error("Erro ao salvar imagem da secretária: {}", e.getMessage(), e);
                    // Se falhar ao salvar imagem, continua sem foto
                }
            } else if (dto.getFoto() != null && !dto.getFoto().isBlank()) {
                // Mantém compatibilidade com o campo foto (String) se imagem não for fornecida
                saved.setFoto(dto.getFoto());
                saved = secretariaRepository.save(saved);
            }

            PrimeiroAcessoEmailDTO primeiroAcessoEmailDTO = new PrimeiroAcessoEmailDTO();
            primeiroAcessoEmailDTO.setSenhaTemporaria(dto.getSenha());
            primeiroAcessoEmailDTO.setNomeFuncionario(secretaria.getNome());

            EmailRequestDTO emailRequestDTO = new EmailRequestDTO();
            emailRequestDTO.setTypeEmail(TipoEmail.PRIMEIRO_ACESSO);
            emailRequestDTO.setDestinatario(secretaria.getEmail());
            emailRequestDTO.setPayload(primeiroAcessoEmailDTO);

            rabbitMQ.enviarPraFilaDeEmails(emailRequestDTO);
            logger.info("Secretária criada com sucesso. ID: {}", saved.getId());
            return saved;
        } catch (BusinessException e) {
            logger.warn("Falha ao criar secretária: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar secretária", e);
            throw e;
        }
    }

    public List<SecretariaResponseDTO> listarTodosDTO() {
        logger.debug("Listando todas as secretárias");
        List<SecretariaResponseDTO> secretarias = secretariaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        logger.debug("Encontradas {} secretárias", secretarias.size());
        return secretarias;
    }

    public SecretariaResponseDTO buscarPorIdDTO(Long id) {
        logger.debug("Buscando secretária por ID: {}", id);
        Secretaria secretaria = secretariaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Secretária não encontrada"));
        return toResponseDTO(secretaria);
    }

    @Transactional
    public SecretariaResponseDTO atualizarSecretaria(Long id, SecretariaDTO dto) {
        logger.info("Tentativa de atualizar secretária ID: {}", id);
        try {
            Secretaria secretaria = secretariaRepository.findById(id)
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Secretária não encontrada"));

            if (dto.getNome() != null) {
                secretaria.setNome(dto.getNome());
            }
            if (dto.getEmail() != null && !dto.getEmail().equals(secretaria.getEmail())) {
                if (secretariaRepository.existsByEmail(dto.getEmail())) {
                    throw new EmailJaCadastradoException("Email já cadastrado");
                }
                secretaria.setEmail(dto.getEmail());
            }
            if (dto.getCpf() != null && !dto.getCpf().equals(secretaria.getCpf())) {
                if (secretariaRepository.existsByCpf(dto.getCpf())) {
                    throw new CpfJaCadastradoException("CPF já cadastrado");
                }
                secretaria.setCpf(dto.getCpf());
            }
            if (dto.getIdade() != null) {
                secretaria.setDataNascimento(dto.getIdade());
            }
            if (dto.getStatus() != null) {
                secretaria.setStatus(dto.getStatus());
            }
            if (dto.getFoto() != null) {
                secretaria.setFoto(dto.getFoto());
            }
            if (dto.getObservacoes() != null) {
                secretaria.setObservacoes(dto.getObservacoes());
            }
            if (dto.getNotificacaoAtiva() != null) {
                secretaria.setNotificacaoAtiva(dto.getNotificacaoAtiva());
            }
            if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
                secretaria.setSenha(passwordEncoder.encode(dto.getSenha()));
            }
            if (dto.getCargo() != null) {
                secretaria.setCargo(dto.getCargo());
            }
            if (dto.getTelefone() != null) {
                secretaria.setTelefone(dto.getTelefone());
            }
            if (dto.getRole() != null) {
                secretaria.setRole(dto.getRole());
            }

            if (dto.getEndereco() != null) {
                Endereco endereco = secretaria.getEndereco();
                if (endereco == null) {
                    endereco = new Endereco();
                }
                if (dto.getEndereco().getRua() != null) {
                    endereco.setRua(dto.getEndereco().getRua());
                }
                if (dto.getEndereco().getNumero() != null) {
                    endereco.setNumero(dto.getEndereco().getNumero());
                }
                if (dto.getEndereco().getBairro() != null) {
                    endereco.setBairro(dto.getEndereco().getBairro());
                }
                if (dto.getEndereco().getCidade() != null) {
                    endereco.setCidade(dto.getEndereco().getCidade());
                }
                if (dto.getEndereco().getEstado() != null) {
                    endereco.setEstado(dto.getEndereco().getEstado());
                }
                if (dto.getEndereco().getCep() != null) {
                    endereco.setCep(dto.getEndereco().getCep());
                }
                if (dto.getEndereco().getUf() != null) {
                    endereco.setUf(dto.getEndereco().getUf());
                }
                endereco = enderecoRepository.save(endereco);
                secretaria.setEndereco(endereco);
            }

            SecretariaResponseDTO response = toResponseDTO(secretariaRepository.save(secretaria));
            logger.info("Secretária atualizada com sucesso. ID: {}", id);
            return response;
        } catch (BusinessException e) {
            logger.warn("Falha ao atualizar secretária ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar secretária ID: {}", id, e);
            throw e;
        }
    }

    public String salvarFoto(Long id, MultipartFile file) {
        Secretaria secretaria = secretariaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Secretária não encontrada"));

        String fotoAntiga = secretaria.getFoto();
        String caminhoNovaFoto = imagemService.uploadFotoSecretaria(file, id);

        secretaria.setFoto(caminhoNovaFoto);
        secretariaRepository.save(secretaria);

        if (fotoAntiga != null && !fotoAntiga.isBlank() && !fotoAntiga.equals(caminhoNovaFoto)) {
            imagemService.removerObjeto(fotoAntiga);
        }

        return caminhoNovaFoto;
    }

    @Transactional
    public void excluirSecretaria(Long id) {
        logger.info("Tentativa de excluir secretária ID: {}", id);
        try {
            Secretaria secretaria = secretariaRepository.findById(id)
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Secretária não encontrada"));

            // Remove a imagem se existir
            if (secretaria.getFoto() != null) {
                imagemService.removerObjeto(secretaria.getFoto());
            }

            secretariaRepository.deleteById(id);
            logger.info("Secretária excluída com sucesso. ID: {}", id);
        } catch (BusinessException e) {
            logger.warn("Falha ao excluir secretária ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao excluir secretária ID: {}", id, e);
            throw e;
        }
    }

    public SecretariaResponseDTO toResponseDTO(Secretaria secretaria) {
        SecretariaResponseDTO dto = new SecretariaResponseDTO();
        dto.setId(secretaria.getId());
        dto.setNome(secretaria.getNome());
        dto.setEmail(secretaria.getEmail());
        dto.setCpf(secretaria.getCpf());
        dto.setIdade(secretaria.getDataNascimento());
        dto.setStatus(secretaria.getStatus());
        dto.setFoto(secretaria.getFoto());
        dto.setObservacoes(secretaria.getObservacoes());
        dto.setNotificacaoAtiva(secretaria.getNotificacaoAtiva());
        dto.setCargo(secretaria.getCargo());
        dto.setRole(secretaria.getRole() != null ? secretaria.getRole().name() : null);
        dto.setTelefone(secretaria.getTelefone());

        if (secretaria.getEndereco() != null) {
            EnderecoResponseDTO enderecoDTO = new EnderecoResponseDTO();
            enderecoDTO.setRua(secretaria.getEndereco().getRua());
            enderecoDTO.setCidade(secretaria.getEndereco().getCidade());
            enderecoDTO.setEstado(secretaria.getEndereco().getEstado());
            enderecoDTO.setCep(secretaria.getEndereco().getCep());
            enderecoDTO.setUf(secretaria.getEndereco().getUf());
            enderecoDTO.setBairro(secretaria.getEndereco().getBairro());
            enderecoDTO.setNumero(secretaria.getEndereco().getNumero());
            dto.setEndereco(enderecoDTO);
        }

        return dto;
    }

    @Transactional
    public ResponsDashSecretariaAdmDTO respostaDashSecretariaAdm(Integer qtdUltimosDias) {

        LocalDateTime inicio = LocalDate.now().minusDays(qtdUltimosDias).atStartOfDay();
        LocalDateTime fim = LocalDate.now().plusDays(1).atStartOfDay();


        List<Agendamento> agendamentos = agendamentoRepository.buscarAgendamentosPorIntervalo(inicio, fim);

        List<AgendamentoPorDiaDTO> grafico1 = agendamentoRepository.buscarAgendamentosPorDiaSemana(inicio, fim)
                .stream()
                .map(row -> new AgendamentoPorDiaDTO(
                        row[0] != null ? row[0].toString() : null,
                        row[1] != null ? ((Number) row[1]).longValue() : 0L))
                .filter(dto -> dto.getDiaSemana() != null)
                .collect(Collectors.toList());


        List<Agendamento> ordenados = agendamentos.stream()
                .sorted(Comparator.comparing(a -> a.getProfessor().getNome()))
                .toList();

        if (ordenados.isEmpty()) {
            ResponsDashSecretariaAdmDTO resposta = new ResponsDashSecretariaAdmDTO();
            resposta.setAgendamentosPorDias(grafico1);
            resposta.setQtdSessoesPorProfessor(new ArrayList<>());
            resposta.setQtdDeAlunosAtendidos(0);
            return resposta;
        }

        List<AgendamentosPorProfessorDTO> professoresObservados = new ArrayList<>();

        AgendamentosPorProfessorDTO professorObservadoAtual = new AgendamentosPorProfessorDTO();
        professorObservadoAtual.setProfessorId(ordenados.get(0).getProfessor().getId());
        professorObservadoAtual.setNomeProfessor(ordenados.get(0).getProfessor().getNome());
        professorObservadoAtual.setTotalAgendamentosPorProfessor(0L);

        for (Agendamento ag : ordenados) {

            if (ag.getProfessor().getNome().equals(professorObservadoAtual.getNomeProfessor())) {


                professorObservadoAtual.setTotalAgendamentosPorProfessor(
                        professorObservadoAtual.getTotalAgendamentosPorProfessor() + 1
                );

            } else {


                professoresObservados.add(professorObservadoAtual);


                professorObservadoAtual = new AgendamentosPorProfessorDTO();
                professorObservadoAtual.setProfessorId(ag.getProfessor().getId());
                professorObservadoAtual.setNomeProfessor(ag.getProfessor().getNome());
                professorObservadoAtual.setTotalAgendamentosPorProfessor(1L); // começa com 1
            }
        }

        Set<Long> idsUnicos = new HashSet<>();
        List<Aluno> alunosUnicos = new ArrayList<>();

        for (Agendamento agendamento : agendamentos) {
            if (agendamento.getAgendamentoAlunos() != null) {
                for (AgendamentoAluno aa : agendamento.getAgendamentoAlunos()) {
                    Aluno aluno = aa.getAluno();
                    if (aluno != null && idsUnicos.add(aluno.getId())) {
                        alunosUnicos.add(aluno);
                    }
                }
            }
        }

        Integer qtdAlunosAtendidos = alunosUnicos.size();




        professoresObservados.add(professorObservadoAtual);


        ResponsDashSecretariaAdmDTO resposta = new ResponsDashSecretariaAdmDTO();
        resposta.setAgendamentosPorDias(grafico1);
        resposta.setQtdSessoesPorProfessor(professoresObservados);
        resposta.setQtdDeAlunosAtendidos(qtdAlunosAtendidos);

        return resposta;
    }

}

