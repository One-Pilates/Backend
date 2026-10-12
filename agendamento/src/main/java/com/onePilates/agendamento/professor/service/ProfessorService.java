package com.onePilates.agendamento.professor.service;

import com.onePilates.agendamento.agendamento.dto.AgendamentoPorDiaDTO;
import com.onePilates.agendamento.agendamento.dto.AulaPorEspecialidadeDTO;
import com.onePilates.agendamento.agendamento.repository.AgendamentoRepository;
import com.onePilates.agendamento.agendamento.service.AgendamentoService;
import com.onePilates.agendamento.dashboard.dto.RespostaDashProfessoraDTO;
import com.onePilates.agendamento.email.dto.EmailRequestDTO;
import com.onePilates.agendamento.email.dto.PrimeiroAcessoEmailDTO;
import com.onePilates.agendamento.email.service.RabbitMQProducer;
import com.onePilates.agendamento.endereco.dto.EnderecoDTO;
import com.onePilates.agendamento.endereco.dto.EnderecoResponseDTO;
import com.onePilates.agendamento.especialidade.dto.EspecialidadeResponseDTO;
import com.onePilates.agendamento.especialidade.repository.EspecialidadeRepository;
import com.onePilates.agendamento.exception.BusinessException;
import com.onePilates.agendamento.exception.CampoObrigatorioException;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.imagem.service.ImagemService;
import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.AgendamentoAluno;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.Endereco;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.model.Professor;
import com.onePilates.agendamento.model.enums.DiaSemana;
import com.onePilates.agendamento.model.enums.Role;
import com.onePilates.agendamento.model.enums.TipoEmail;
import com.onePilates.agendamento.professor.dto.KPIsProfessorDTO;
import com.onePilates.agendamento.professor.dto.ProfessorDTO;
import com.onePilates.agendamento.professor.dto.ProfessorPaginadoResponseDTO;
import com.onePilates.agendamento.professor.dto.ProfessorResponseDTO;
import com.onePilates.agendamento.professor.repository.ProfessorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProfessorService {
    private static final Logger logger = LoggerFactory.getLogger(ProfessorService.class);

    private final ProfessorRepository professorRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final AgendamentoService agendamentoService;
    private final PasswordEncoder passwordEncoder;
    private final ImagemService imagemService;
    private final RabbitMQProducer rabbitMQ;


    public ProfessorService(ProfessorRepository professorRepository, EspecialidadeRepository especialidadeRepository, AgendamentoRepository agendamentoRepository, AgendamentoService agendamentoService, PasswordEncoder passwordEncoder, ImagemService imagemService, RabbitMQProducer rabbitMQ) {
        this.professorRepository = professorRepository;
        this.especialidadeRepository = especialidadeRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.agendamentoService = agendamentoService;
        this.passwordEncoder = passwordEncoder;
        this.imagemService = imagemService;
        this.rabbitMQ = rabbitMQ;
    }

    public String salvarFoto(Long id, MultipartFile file) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Professor não encontrado"));

        try {
            String fotoAnterior = professor.getFoto();

            // Salva a imagem localmente
            String key = imagemService.uploadFotoPerfil(file, id);

            // 💾 Atualiza no banco
            professor.setFoto(key);
            professorRepository.save(professor);

            // 🧹 Remove a foto antiga se existir e for diferente
            if (fotoAnterior != null && !fotoAnterior.isBlank() && !fotoAnterior.equals(key)) {
                imagemService.removerObjeto(fotoAnterior);
            }

            return key;

        } catch (Exception e) {
            logger.error("Erro ao salvar foto: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar foto");
        }
    }

    @Transactional
    public ProfessorResponseDTO criarProfessor(ProfessorDTO dto) {
        logger.info("Tentativa de criar professor: {}", dto.getNome());
        try {
            // fluxo padrão: aceita dto.role se presente, mas endpoint público deve forçar PROFESSOR
            Role roleToSet = dto.getRole() != null ? dto.getRole() : Role.PROFESSOR;
            ProfessorResponseDTO response = criarProfessorInterno(dto, roleToSet);
            logger.info("Professor criado com sucesso. ID: {}", response.getId());
            return response;
        } catch (BusinessException e) {
            logger.warn("Falha ao criar professor: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar professor", e);
            throw e;
        }
    }


    private ProfessorResponseDTO criarProfessorInterno(ProfessorDTO dto, Role roleToSet) {

        validateDto(dto);

        Professor professor = new Professor();
        professor.setNome(dto.getNome());
        professor.setEmail(dto.getEmail());
        professor.setCpf(dto.getCpf());
        professor.setDataNascimento(dto.getIdade());
        professor.setStatus(true);
        professor.setObservacoes(dto.getObservacoes());
        professor.setNotificacaoAtiva(dto.getNotificacaoAtiva() != null ? dto.getNotificacaoAtiva() : Boolean.FALSE);
        professor.setCargo(dto.getCargo());
        professor.setRole(roleToSet);
        professor.setTelefone(dto.getTelefone());
        professor.setPrimeiroAcesso(true);

        if (dto.getSenha() != null) {
            professor.setSenha(passwordEncoder.encode(dto.getSenha()));
        } else {
            throw new CampoObrigatorioException("Senha é obrigatória");
        }

        // 📍 Endereço
        EnderecoDTO enderecoDTO = dto.getEndereco();
        if (enderecoDTO != null) {
            Endereco endereco = new Endereco();
            endereco.setRua(enderecoDTO.getRua());
            endereco.setNumero(enderecoDTO.getNumero());
            endereco.setBairro(enderecoDTO.getBairro());
            endereco.setCidade(enderecoDTO.getCidade());
            endereco.setEstado(enderecoDTO.getEstado());
            endereco.setCep(enderecoDTO.getCep());
            endereco.setUf(enderecoDTO.getUf());
            professor.setEndereco(endereco);
        }

        // 📍 Especialidades
        Set<Especialidade> especialidades = Optional.ofNullable(dto.getEspecialidadeIds())
                .orElse(Collections.emptySet())
                .stream()
                .map(id -> especialidadeRepository.findById(id)
                        .orElseThrow(() -> new EntidadeNaoEncontradaException("Especialidade não encontrada: " + id)))
                .collect(Collectors.toSet());

        professor.setEspecialidades(especialidades);

        // 💾 Salva primeiro para gerar ID
        Professor salvo = professorRepository.save(professor);

        // Salva a imagem localmente
        if (dto.getImagem() != null && !dto.getImagem().isEmpty()) {
            try {
                // usa o ID já gerado
                String key = imagemService.uploadFotoPerfil(dto.getImagem(), salvo.getId());

                salvo.setFoto(key);
                salvo = professorRepository.save(salvo);

            } catch (Exception e) {
                logger.error("Erro ao salvar imagem do professor: {}", e.getMessage(), e);
            }
        }
        // fallback (caso venha string pronta)
        else if (dto.getFoto() != null && !dto.getFoto().isBlank()) {
            salvo.setFoto(dto.getFoto());
            salvo = professorRepository.save(salvo);
        }

        // 📧 Email de primeiro acesso
        PrimeiroAcessoEmailDTO primeiroAcessoEmailDTO = new PrimeiroAcessoEmailDTO();
        primeiroAcessoEmailDTO.setNomeFuncionario(professor.getNome());
        primeiroAcessoEmailDTO.setSenhaTemporaria(dto.getSenha());

        EmailRequestDTO emailRequestDTO = new EmailRequestDTO();
        emailRequestDTO.setTypeEmail(TipoEmail.PRIMEIRO_ACESSO);
        emailRequestDTO.setDestinatario(professor.getEmail());
        emailRequestDTO.setPayload(primeiroAcessoEmailDTO);

        rabbitMQ.enviarPraFilaDeEmails(emailRequestDTO);

        return toResponseDTO(salvo);
    }

        public ProfessorPaginadoResponseDTO listarTodosPaginadosDTO(Pageable pageable, String nome) {
        logger.debug("Listando professores com paginacao");
        boolean temNome = nome != null && !nome.isBlank();

        var pagina = temNome
            ? professorRepository.findByNomeContainingIgnoreCase(nome, pageable)
            : professorRepository.findAll(pageable);
        var paginaDto = pagina.map(this::toResponseDTO);

        logger.debug("Encontrados {} professores na pagina", paginaDto.getNumberOfElements());
        return new ProfessorPaginadoResponseDTO(
            paginaDto.getContent(),
            paginaDto.getTotalElements(),
            paginaDto.getTotalPages()
        );
        }

    public List<ProfessorResponseDTO> listarTodosDTO() {
        logger.debug("Listando todos os professores");
        List<ProfessorResponseDTO> professores = professorRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        logger.debug("Encontrados {} professores", professores.size());
        return professores;
    }

    public ProfessorResponseDTO buscarPorIdDTO(Long id) {
        logger.debug("Buscando professor por ID: {}", id);
        return toResponseDTO(buscarPorId(id));
    }

    @Transactional
    public ProfessorResponseDTO atualizarProfessor(Long id, ProfessorDTO dto) {
        logger.info("Tentativa de atualizar professor ID: {}", id);
        try {
            Professor existente = buscarPorId(id);

            if (dto.getNome() != null) existente.setNome(dto.getNome());
            if (dto.getEmail() != null) existente.setEmail(dto.getEmail());
            if (dto.getCpf() != null) existente.setCpf(dto.getCpf());
            if (dto.getIdade() != null) existente.setDataNascimento(dto.getIdade());
            if (dto.getFoto() != null) existente.setFoto(dto.getFoto());
            if (dto.getObservacoes() != null) existente.setObservacoes(dto.getObservacoes());
            if (dto.getNotificacaoAtiva() != null) existente.setNotificacaoAtiva(dto.getNotificacaoAtiva());
            if (dto.getStatus() != null) existente.setStatus(dto.getStatus());
            if (dto.getCargo() != null) existente.setCargo(dto.getCargo());
            if (dto.getTelefone() != null) existente.setTelefone(dto.getTelefone());

            if (dto.getSenha() != null) {
                existente.setSenha(passwordEncoder.encode(dto.getSenha()));
            }

            if (dto.getRole() != null) {
                existente.setRole(dto.getRole()); // se quiser proteger alteração de role, aplique checagem adicional
            }

            if (dto.getEndereco() != null) {
                EnderecoDTO e = dto.getEndereco();
                Endereco endereco = existente.getEndereco() != null ? existente.getEndereco() : new Endereco();
                if (e.getRua() != null) endereco.setRua(e.getRua());
                if (e.getNumero() != null) endereco.setNumero(e.getNumero());
                if (e.getBairro() != null) endereco.setBairro(e.getBairro());
                if (e.getCidade() != null) endereco.setCidade(e.getCidade());
                if (e.getEstado() != null) endereco.setEstado(e.getEstado());
                if (e.getCep() != null) endereco.setCep(e.getCep());
                if (e.getUf() != null) endereco.setUf(e.getUf());
                existente.setEndereco(endereco);
            }

            if (dto.getEspecialidadeIds() != null) {
                Set<Especialidade> especialidades = dto.getEspecialidadeIds().stream()
                        .map(idEsp -> especialidadeRepository.findById(idEsp)
                                .orElseThrow(() -> new EntidadeNaoEncontradaException("Especialidade não encontrada: " + idEsp)))
                        .collect(Collectors.toSet());
                existente.setEspecialidades(especialidades);
            }

            Professor atualizado = professorRepository.save(existente);
            ProfessorResponseDTO response = toResponseDTO(atualizado);
            logger.info("Professor atualizado com sucesso. ID: {}", id);
            return response;
        } catch (BusinessException e) {
            logger.warn("Falha ao atualizar professor ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar professor ID: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public void excluirProfessor(Long id) {
        logger.info("Tentativa de excluir professor ID: {}", id);

        try {
            Professor professor = professorRepository.findById(id)
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Professor não encontrado"));

            // Verifica se existem agendamentos
            List<Agendamento> agendamentos = agendamentoRepository.findByProfessorId(id);

            if (agendamentos != null && !agendamentos.isEmpty()) {
                logger.info("Existem {} agendamentos para o professor ID {}. Excluindo todos...", agendamentos.size(), id);
                agendamentoRepository.deleteAll(agendamentos);
            }

            // Remove a imagem se existir
            if (professor.getFoto() != null) {
                imagemService.removerObjeto(professor.getFoto());
            }

            professorRepository.deleteById(id);

            logger.info("Professor excluído com sucesso. ID: {}", id);

        } catch (BusinessException e) {
            logger.warn("Falha ao excluir professor ID {}: {}", id, e.getMessage());
            throw e;

        } catch (Exception e) {
            logger.error("Erro inesperado ao excluir professor ID: {}", id, e);
            throw e;
        }
    }

    private Professor buscarPorId(Long id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Professor não encontrado"));
    }

    public ProfessorResponseDTO toResponseDTO(Professor professor) {
        ProfessorResponseDTO dto = new ProfessorResponseDTO();
        dto.setId(professor.getId());
        dto.setNome(professor.getNome());
        dto.setEmail(professor.getEmail());
        dto.setCpf(professor.getCpf());
        dto.setIdade(professor.getDataNascimento());
        dto.setStatus(professor.getStatus());
        dto.setFoto(professor.getFoto());
        dto.setObservacoes(professor.getObservacoes());
        dto.setNotificacaoAtiva(professor.getNotificacaoAtiva());
        dto.setCargo(professor.getCargo());
        dto.setRole(professor.getRole() != null ? professor.getRole().name() : null);
        dto.setTelefone(professor.getTelefone());

        if (professor.getEndereco() != null) {
            EnderecoResponseDTO enderecoDTO = new EnderecoResponseDTO();
            enderecoDTO.setRua(professor.getEndereco().getRua());
            enderecoDTO.setCidade(professor.getEndereco().getCidade());
            enderecoDTO.setBairro(professor.getEndereco().getBairro());
            enderecoDTO.setUf(professor.getEndereco().getUf());
            enderecoDTO.setEstado(professor.getEndereco().getEstado());
            enderecoDTO.setCep(professor.getEndereco().getCep());
            enderecoDTO.setNumero(professor.getEndereco().getNumero());
            dto.setEndereco(enderecoDTO);
        }

        if (professor.getEspecialidades() != null && !professor.getEspecialidades().isEmpty()) {
            Set<EspecialidadeResponseDTO> especialidadesDTO = professor.getEspecialidades().stream()
                    .map(esp -> {
                        EspecialidadeResponseDTO espDTO = new EspecialidadeResponseDTO();
                        espDTO.setId(esp.getId());
                        espDTO.setNome(esp.getNome());
                        return espDTO;
                    })
                    .collect(Collectors.toSet());
            dto.setEspecialidades(especialidadesDTO);
        }


        return dto;
    }

    private void validateDto(ProfessorDTO dto) {
        if (dto == null) throw new CampoObrigatorioException("Payload inválido");
        if (dto.getNome() == null || dto.getNome().isBlank()) throw new CampoObrigatorioException("Nome é obrigatório");
        if (dto.getEmail() == null || dto.getEmail().isBlank())
            throw new CampoObrigatorioException("Email é obrigatório");
        if (dto.getSenha() == null || dto.getSenha().isBlank())
            throw new CampoObrigatorioException("Senha é obrigatória");
    }

    public RespostaDashProfessoraDTO respostaDashProfessora(Long id, Integer qtdUltimosDias) {
        if (!professorRepository.existsById(id)) {
            throw new EntidadeNaoEncontradaException("Professor não encontrado");
        }

        LocalDateTime inicio = LocalDate.now().minusDays(qtdUltimosDias).atStartOfDay();
        LocalDateTime fim = LocalDate.now().plusDays(1).atStartOfDay();

        Long totalAgendamentosNoPeriodo = agendamentoRepository.countByProfessorIdAndPeriod(id, inicio, fim);
        if (totalAgendamentosNoPeriodo == null || totalAgendamentosNoPeriodo == 0) {
            return new RespostaDashProfessoraDTO(new ArrayList<>(), new ArrayList<>(), new KPIsProfessorDTO());
        }

        // Query 1: Agendamentos por dia da semana
        List<AgendamentoPorDiaDTO> grafico1 = agendamentoRepository.buscarAgendamentosPorDiaSemanaRaw(id, inicio, fim)
                .stream()
                .map(row -> new AgendamentoPorDiaDTO(
                        row[0] != null ? row[0].toString() : null,
                        row[1] != null ? ((Number) row[1]).longValue() : 0L))
                .filter(dto -> dto.getDiaSemana() != null)
                .collect(Collectors.toList());

        // Query 2: Aulas por especialidade
        List<AulaPorEspecialidadeDTO> grafico2 = agendamentoRepository.buscarDistribuicaoAulasPorEspecialidadeRaw(
                        id, inicio, fim, totalAgendamentosNoPeriodo)
                .stream()
                .map(row -> new AulaPorEspecialidadeDTO(
                        id,
                        row[0] != null ? row[0].toString() : null,
                        row[1] != null ? ((Number) row[1]).doubleValue() : 0.0))
                .filter(dto -> dto.getEspecialidade() != null)
                .collect(Collectors.toList());

        // KPI: dia da semana com maior atendimento
        KPIsProfessorDTO dto = new KPIsProfessorDTO();
        AgendamentoPorDiaDTO diaDaSemanaComMaisAgendamentos = grafico1.stream()
                .max(Comparator.comparingLong(AgendamentoPorDiaDTO::getTotalAgendamentos))
                .orElse(null);

        if (diaDaSemanaComMaisAgendamentos != null) {
            switch (diaDaSemanaComMaisAgendamentos.getDiaSemana().toLowerCase()) {
                case "monday" -> dto.setDiaSemanaComMaiorAtendimento(DiaSemana.SEGUNDA);
                case "tuesday" -> dto.setDiaSemanaComMaiorAtendimento(DiaSemana.TERCA);
                case "wednesday" -> dto.setDiaSemanaComMaiorAtendimento(DiaSemana.QUARTA);
                case "thursday" -> dto.setDiaSemanaComMaiorAtendimento(DiaSemana.QUINTA);
                case "friday" -> dto.setDiaSemanaComMaiorAtendimento(DiaSemana.SEXTA);
                case "saturday" -> dto.setDiaSemanaComMaiorAtendimento(DiaSemana.SABADO);
                case "sunday" -> dto.setDiaSemanaComMaiorAtendimento(DiaSemana.DOMINGO);
            }
        }

        // Buscar agendamentos completos
        List<Agendamento> agendamentos = agendamentoRepository.findAgendamentosByProfessorAndPeriod(id, inicio, fim);

        // Filtrar alunos únicos
        Set<Long> idsUnicos = new HashSet<>();
        List<Aluno> alunosUnicos = new ArrayList<>();

        // Contador de especialidades
        Map<String, Long> contadorEspecialidades = new HashMap<>();

        for (Agendamento agendamento : agendamentos) {
            if (agendamento.getAgendamentoAlunos() != null) {
                for (AgendamentoAluno aa : agendamento.getAgendamentoAlunos()) {
                    Aluno aluno = aa.getAluno();
                    if (aluno != null && idsUnicos.add(aluno.getId())) {
                        alunosUnicos.add(aluno);
                    }
                }
            }

            if (agendamento.getEspecialidade() != null) {
                String nomeEsp = agendamento.getEspecialidade().getNome();
                contadorEspecialidades.put(nomeEsp, contadorEspecialidades.getOrDefault(nomeEsp, 0L) + 1);
            }
        }

        // Quantidade de alunos distintos atendidos
        Integer qtdAlunosAtendidos = alunosUnicos.size();
        dto.setQtdAlunosAtendidos(qtdAlunosAtendidos);

        // Especialidade mais requisitada
        String especialidadeMaisRequisitada = contadorEspecialidades.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        dto.setEspecialidadeMaisRequisitada(especialidadeMaisRequisitada);
        dto.setQtdTotalSessoesRealizadas(agendamentos.size());

        return new RespostaDashProfessoraDTO(grafico1, grafico2, dto);
    }



}
