package com.onePilates.agendamento.service;

import com.onePilates.agendamento.dto.AgendamentoDTO;
import com.onePilates.agendamento.dto.rabbitMQDTOs.*;
import com.onePilates.agendamento.dto.response.AgendamentoResponseDTO;
import com.onePilates.agendamento.dto.response.AlunoAgendamentoResponseDTO;
import com.onePilates.agendamento.exception.*;
import com.onePilates.agendamento.model.*;
import com.onePilates.agendamento.observer.AgendamentoNotifier;
import com.onePilates.agendamento.repository.*;
import com.onePilates.agendamento.validator.AgendamentoValidator;
import jakarta.validation.constraints.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AgendamentoService {

    private static final Logger logger = LoggerFactory.getLogger(AgendamentoService.class);

    private final AgendamentoRepository agendamentoRepository;
    private final ProfessorRepository professorRepository;
    private final SalaRepository salaRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final AlunoRepository alunoRepository;
    private final AgendamentoNotifier notifier;
    private final AgendamentoAlunoRepository agendamentoAlunoRepository;
    private final AgendamentoValidator agendamentoValidator;
    private final RabbitMQProducer rabbitMQ;

    public AgendamentoService(AgendamentoRepository agendamentoRepository, ProfessorRepository professorRepository, SalaRepository salaRepository, EspecialidadeRepository especialidadeRepository, AlunoRepository alunoRepository, AgendamentoNotifier notifier, AgendamentoAlunoRepository agendamentoAlunoRepository, AgendamentoValidator agendamentoValidator, RabbitMQProducer rabbitMQ) {
        this.agendamentoRepository = agendamentoRepository;
        this.professorRepository = professorRepository;
        this.salaRepository = salaRepository;
        this.especialidadeRepository = especialidadeRepository;
        this.alunoRepository = alunoRepository;
        this.notifier = notifier;
        this.agendamentoAlunoRepository = agendamentoAlunoRepository;
        this.agendamentoValidator = agendamentoValidator;
        this.rabbitMQ = rabbitMQ;
    }

    /**
     * Cria um novo agendamento após validar todas as regras de negócio.
     *
     * @param dto DTO contendo os dados do agendamento (data/hora, sala, professor, especialidade e alunos)
     * @return Agendamento criado e salvo no banco de dados
     * @throws BusinessException se alguma validação de regra de negócio falhar
     */
    @Transactional
    public Agendamento criarAgendamento(AgendamentoDTO dto) {
        if (dto.getDataHora() != null) {
            dto.setDataHora(normalizarDataHora(dto.getDataHora()));
        }

        logger.info("Tentativa de criar agendamento para data/hora: {}", dto.getDataHora());

        try {
            // Validação acumulativa (já inclui conflitos)
            List<String> erros = agendamentoValidator.validarCriacaoListandoErros(dto);

            if (!erros.isEmpty()) {
                logger.warn("Falha na validação múltipla do agendamento: {}", erros);
                throw new ValidacaoMultiplaException(erros);
            }

            Agendamento agendamento = mapDtoToEntity(dto);

            agendamento = agendamentoRepository.save(agendamento);
            logger.debug("Agendamento criado com ID: {}", agendamento.getId());

            agendamento = agendamentoRepository.findById(agendamento.getId())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Erro ao recarregar agendamento"));

            Professor professor = agendamento.getProfessor();
            if (Boolean.TRUE.equals(professor.getNotificacaoAtiva())) {
                notifier.notificarTodos(agendamento);
            }

            logger.info("Agendamento criado com sucesso. ID: {}", agendamento.getId());
            return agendamento;

        } catch (ValidacaoMultiplaException e) {
            throw e;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }




    @Transactional
    public Agendamento criarAgendamentoAbsoluto(AgendamentoDTO dto) {
        // Normalizar data/hora para hora cheia (zerar minutos, segundos e nanossegundos)
        if (dto.getDataHora() != null) {
            dto.setDataHora(normalizarDataHora(dto.getDataHora()));
        }


        try {
            // Primeira validação (completa) - feita no validator dentro de mapDtoToEntity
            Agendamento agendamento = mapDtoToEntity(dto);

            agendamento = agendamentoRepository.save(agendamento);

            logger.debug("Agendamento criado com ID: {}", agendamento.getId());

            // Recarregar o agendamento com todas as relações para o observer
            agendamento = agendamentoRepository.findById(agendamento.getId())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Erro ao recarregar agendamento"));

            Professor professor = agendamento.getProfessor();

            if (professor.getNotificacaoAtiva() != null && professor.getNotificacaoAtiva()) {
                logger.debug("Enviando notificação para professor: {}", professor.getNome());
                notifier.notificarTodos(agendamento);
            }

            logger.info("Agendamento criado com sucesso. ID: {}", agendamento.getId());
            return agendamento;
        } catch (BusinessException e) {
            logger.warn("Falha ao criar agendamento: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar agendamento", e);
            throw e;
        }
    }
    /**
     * Busca todos os agendamentos de um professor específico.
     *
     * @param id ID do professor
     * @return Lista de agendamentos do professor
     */
    public List<AgendamentoResponseDTO> buscarAgendamentosPorIdProfessor(Long id) {
        logger.debug("Buscando agendamentos para professor ID: {}", id);
        List<Agendamento> agendamentos = agendamentoRepository.findByProfessorId(id);
        logger.debug("Encontrados {} agendamentos para professor ID: {}", agendamentos.size(), id);
        return agendamentos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<AgendamentoResponseDTO> buscarAgendamentosPorIdSala(Long id) {


        if (salaRepository.findById(id).isEmpty()) {
            throw new RuntimeException("A sala informada não é válida.");
        }


        List<Agendamento> agendamentos = agendamentoRepository.findBySalaId(id);

        if (agendamentos.isEmpty()) {
            throw new RuntimeException("Nenhum agendamento encontrado para a sala informada.");
        }


        return agendamentos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }


    public List<AgendamentoResponseDTO> buscarAgendamentosPorIdsDeSalaEProfessor(Long idSala, Long idProfessor) {

        if (idSala == null || idProfessor == null) {
            throw new IllegalArgumentException("Dados fornecidos inválidos.");
        }
        if(salaRepository.findById(idSala).isEmpty()) {
            throw new RuntimeException("A sala informada não é valida");
        }
        if(idProfessor !=0){
            if(professorRepository.findById(idProfessor).isEmpty()) {
                throw new RuntimeException("Professor informado não existente");
            }
        }

        List<Agendamento> agendamentos;


        if (idProfessor != 0) {
            agendamentos = agendamentoRepository.findByProfessorId(idProfessor);

            if (agendamentos.isEmpty()) {
                throw new RuntimeException("O professor selecionado não possui agendamentos.");
            }

        } else {

            agendamentos = agendamentoRepository.findAll();

            if (agendamentos.isEmpty()) {
                throw new RuntimeException("Nenhum agendamento encontrado.");
            }
        }

        List<Agendamento> filtrados = agendamentos.stream()
                .filter(a -> a.getSala().getId().equals(idSala))
                .collect(Collectors.toList());

        if (filtrados.isEmpty() && idProfessor != 0) {
            throw new RuntimeException("Nenhum agendamento encontrado para o(a) professor(a) " + agendamentos.getFirst().getProfessor().getNome() + " na sala informada.");
        }
        if(filtrados.isEmpty() && idProfessor == 0) {
            throw new RuntimeException("Nenhum agendamento encontrado na sala informada.");
        }

        return filtrados.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }



    private Agendamento mapDtoToEntity(AgendamentoDTO dto) {
        Agendamento agendamento = new Agendamento();
        agendamento.setDataHora(dto.getDataHora());

        Professor professor = professorRepository.findById(dto.getProfessorId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Professor não encontrado"));
        agendamento.setProfessor(professor);

        Sala sala = salaRepository.findById(dto.getSalaId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Sala não encontrada"));
        agendamento.setSala(sala);

        Especialidade especialidade = especialidadeRepository.findById(dto.getEspecialidadeId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Especialidade não encontrada"));
        agendamento.setEspecialidade(especialidade);

        // Buscar alunos e criar AgendamentoAluno
        List<Aluno> alunos = alunoRepository.findAllById(dto.getAlunoIds());
        if (alunos.size() != dto.getAlunoIds().size()) {
            throw new EntidadeNaoEncontradaException("Um ou mais alunos não foram encontrados");
        }

        Set<AgendamentoAluno> agendamentoAlunos = alunos.stream()
                .map(aluno -> new AgendamentoAluno(agendamento, aluno))
                .collect(Collectors.toSet());

        agendamento.setAgendamentoAlunos(agendamentoAlunos);

        return agendamento;
    }


    /**
     * Valida conflitos críticos imediatamente antes de salvar o agendamento.
     * Esta validação dupla (double-check) reduz significativamente a janela de race condition
     * entre a validação inicial e o save no banco de dados.
     * 
     * @param dto DTO contendo os dados do agendamento a ser validado
     * @throws ConflitoHorarioException se houver conflito de professor ou sala no horário
     */
    private void validarConflitosAntesDeSalvar(AgendamentoDTO dto) {
        logger.debug("Validação dupla (double-check) de conflitos antes de salvar agendamento");

        // Validação rápida de conflitos críticos (professor, sala e alunos)
        if (agendamentoRepository.existsByProfessorIdAndDataHora(dto.getProfessorId(), dto.getDataHora())) {
            logger.warn("Conflito detectado na validação dupla: Professor {} já possui agendamento em {}", 
                    dto.getProfessorId(), dto.getDataHora());
            throw new ConflitoHorarioException("Professor indisponível para o horário agendado.");
        }
        
        if (agendamentoRepository.existsBySalaIdAndDataHora(dto.getSalaId(), dto.getDataHora())) {
            logger.warn("Conflito detectado na validação dupla: Sala {} já possui agendamento em {}", 
                    dto.getSalaId(), dto.getDataHora());
            throw new ConflitoHorarioException("Sala indisponível para o horário agendado.");
        }
        
        // Validação de conflito de alunos (completando a validação dupla)
        if (dto.getAlunoIds() != null && !dto.getAlunoIds().isEmpty()) {
            List<Aluno> alunos = alunoRepository.findAllById(dto.getAlunoIds());
            List<String> nomesIndisponiveis = alunos.stream()
                .filter(aluno -> !agendamentoRepository.findAgendamentosByAlunoAndDataHora(aluno, dto.getDataHora()).isEmpty())
                .map(Aluno::getNome)
                .toList();
            
            if (!nomesIndisponiveis.isEmpty()) {
                logger.warn("Conflito detectado na validação dupla: Alunos {} já possuem agendamento em {}", 
                        String.join(", ", nomesIndisponiveis), dto.getDataHora());
                String dataHoraFormatada = dto.getDataHora().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));
                throw new ConflitoHorarioException(
                    String.format("Os seguintes alunos estão indisponíveis para o horário %s: %s",
                        dataHoraFormatada,
                        String.join(", ", nomesIndisponiveis))
                );
            }
        }

        logger.debug("Validação dupla concluída sem conflitos");
    }

    /**
     * Lista todos os agendamentos cadastrados no sistema.
     *
     * @return Lista de todos os agendamentos
     */
    public List<AgendamentoResponseDTO> listarTodosDTO() {
        logger.debug("Listando todos os agendamentos");
        List<Agendamento> agendamentos = agendamentoRepository.findAll();
        logger.debug("Encontrados {} agendamentos", agendamentos.size());
        return agendamentos.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca um agendamento específico pelo ID.
     *
     * @param id ID do agendamento
     * @return DTO com os dados do agendamento
     * @throws EntidadeNaoEncontradaException se o agendamento não for encontrado
     */
    public AgendamentoResponseDTO buscarPorIdDTO(Long id) {
        logger.debug("Buscando agendamento por ID: {}", id);
        return toResponseDTO(buscarPorId(id));
    }

    /**
     * Atualiza um agendamento existente. Apenas os campos fornecidos no DTO serão atualizados.
     *
     * @param agendamentoId ID do agendamento a ser atualizado
     * @param dto DTO contendo os campos a serem atualizados
     * @return DTO com os dados atualizados do agendamento
     * @throws EntidadeNaoEncontradaException se o agendamento não for encontrado
     * @throws BusinessException se alguma validação de regra de negócio falhar
     */
    @Transactional
    public AgendamentoResponseDTO atualizarAgendamento(Long agendamentoId, AgendamentoDTO dto) {
        logger.info("Tentativa de atualizar agendamento ID: {}", agendamentoId);
        
        // Normalizar data/hora para hora cheia (zerar minutos, segundos e nanossegundos)
        if (dto.getDataHora() != null) {
            dto.setDataHora(normalizarDataHora(dto.getDataHora()));
        }
        
        try {
            Agendamento agendamento = buscarPorId(agendamentoId);
            
            // Guardar professor antigo para detectar troca
            Professor professorAntigo = agendamento.getProfessor();
            Long professorIdAntigo = professorAntigo.getId();
            
            // Criar DTO temporário com dados do agendamento existente para validação
            AgendamentoDTO dtoValidacao = new AgendamentoDTO();
            LocalDateTime dataHoraValidacao = dto.getDataHora() != null ? dto.getDataHora() : agendamento.getDataHora();
            // Garantir que a data/hora do agendamento existente também esteja normalizada
            if (dataHoraValidacao != null) {
                dataHoraValidacao = normalizarDataHora(dataHoraValidacao);
            }
            dtoValidacao.setDataHora(dataHoraValidacao);
            dtoValidacao.setProfessorId(dto.getProfessorId() != null ? dto.getProfessorId() : agendamento.getProfessor().getId());
            dtoValidacao.setSalaId(dto.getSalaId() != null ? dto.getSalaId() : agendamento.getSala().getId());
            dtoValidacao.setEspecialidadeId(dto.getEspecialidadeId() != null ? dto.getEspecialidadeId() : agendamento.getEspecialidade().getId());
            dtoValidacao.setAlunoIds(dto.getAlunoIds() != null ? dto.getAlunoIds() : 
                agendamento.getAgendamentoAlunos().stream()
                    .map(aa -> aa.getAluno().getId())
                    .collect(Collectors.toSet()));
            
            // Validar com os novos dados usando o validator, excluindo o agendamento atual das verificações de conflito
            agendamentoValidator.validar(dtoValidacao, agendamentoId);
            
            if (dto.getDataHora() != null) {
                agendamento.setDataHora(normalizarDataHora(dto.getDataHora()));
            }
            
            Professor professorNovo = null;
            boolean professorFoiTrocado = false;
            
            if (dto.getProfessorId() != null) {
                professorNovo = professorRepository.findById(dto.getProfessorId())
                        .orElseThrow(() -> new EntidadeNaoEncontradaException("Professor não encontrado"));
                
                // Verificar se houve troca de professor
                if (!professorIdAntigo.equals(professorNovo.getId())) {
                    professorFoiTrocado = true;
                    agendamento.setProfessor(professorNovo);
                }
            } else {
                professorNovo = professorAntigo; // Mantém o mesmo professor
            }
            if (dto.getSalaId() != null) {
                Sala sala = salaRepository.findById(dto.getSalaId())
                        .orElseThrow(() -> new EntidadeNaoEncontradaException("Sala não encontrada"));
                agendamento.setSala(sala);
            }
            if (dto.getEspecialidadeId() != null) {
                Especialidade especialidade = especialidadeRepository.findById(dto.getEspecialidadeId())
                        .orElseThrow(() -> new EntidadeNaoEncontradaException("Especialidade não encontrada"));
                agendamento.setEspecialidade(especialidade);
            }
            if (dto.getAlunoIds() != null) {
                // Atualizar alunos mantendo a mesma instância da coleção (importante para orphanRemoval)
                Set<Long> novosAlunoIds = dto.getAlunoIds();
                Set<AgendamentoAluno> agendamentoAlunosAtuais = agendamento.getAgendamentoAlunos();
                
                // Criar um mapa dos alunos atuais por ID para facilitar a busca
                Map<Long, AgendamentoAluno> alunosAtuaisPorId = agendamentoAlunosAtuais.stream()
                        .collect(Collectors.toMap(aa -> aa.getAluno().getId(), aa -> aa));
                
                // Remover alunos que não estão mais na lista
                agendamentoAlunosAtuais.removeIf(aa -> !novosAlunoIds.contains(aa.getAluno().getId()));
                
                // Buscar todos os alunos novos
                List<Aluno> alunos = alunoRepository.findAllById(novosAlunoIds);
                
                // Adicionar novos alunos que ainda não estão na coleção
                for (Aluno aluno : alunos) {
                    if (!alunosAtuaisPorId.containsKey(aluno.getId())) {
                        AgendamentoAluno novoAgendamentoAluno = new AgendamentoAluno(agendamento, aluno);
                        agendamentoAlunosAtuais.add(novoAgendamentoAluno);
                    }
                }
            }
            
            // Salvar o agendamento antes de notificar
            Agendamento agendamentoSalvo = agendamentoRepository.save(agendamento);
            
            // Recarregar o agendamento com todas as relações para as notificações
            agendamentoSalvo = agendamentoRepository.findById(agendamentoSalvo.getId())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Erro ao recarregar agendamento"));
            
            // Notificações de atualização
            if (professorFoiTrocado) {
                // Caso especial: troca de professor - notificar ambos
                logger.debug("Troca de professor detectada. Notificando professor antigo e novo");
                
                // Notificar professor antigo sobre remoção
                if (professorAntigo.getNotificacaoAtiva() != null && professorAntigo.getNotificacaoAtiva()) {
                    logger.debug("Enviando notificação de remoção para professor: {}", professorAntigo.getNome());
                    AulaCanceladaEmailDTO aulaCanceladaEmailDTO = new AulaCanceladaEmailDTO();
                    aulaCanceladaEmailDTO.setNomeSala(agendamentoSalvo.getSala().getNome());
                    aulaCanceladaEmailDTO.setDataHoraAgendamento(agendamentoSalvo.getDataHora().toString());
                    aulaCanceladaEmailDTO.setNomeEspecialidade(agendamentoSalvo.getEspecialidade().getNome());
                    aulaCanceladaEmailDTO.setNomeProfessor(professorAntigo.getNome());

                    EmailRequestDTO emailRequestDTO = new EmailRequestDTO();
                    emailRequestDTO.setDestinatario(professorAntigo.getEmail());
                    emailRequestDTO.setTypeEmail(TipoEmail.AULA_CANCELADA);
                    emailRequestDTO.setPayload(aulaCanceladaEmailDTO);

                  rabbitMQ.enviarPraFilaDeEmails(emailRequestDTO);
                  logger.info("Enviando solicitação para notificar o professor antigo que a aula foi cancelada");
                }
                
                // Notificar professor novo sobre novo agendamento
                if (professorNovo.getNotificacaoAtiva() != null && professorNovo.getNotificacaoAtiva()) {
                    logger.debug("Enviando notificação de novo agendamento para professor: {}", professorNovo.getNome());
                    List<String> nomesAlunos = agendamentoSalvo.getAgendamentoAlunos().stream()
                            .map(aa -> aa.getAluno().getNome())
                            .collect(Collectors.toList());

                    AulaCriadaEmailDTO aulaCriadaEmailDTO = new AulaCriadaEmailDTO();
                    aulaCriadaEmailDTO.setNomesDosAlunos(nomesAlunos);
                    aulaCriadaEmailDTO.setDataHoraAgendamento(agendamentoSalvo.getDataHora().toString());
                    aulaCriadaEmailDTO.setNomeSala(agendamentoSalvo.getSala().getNome());
                    aulaCriadaEmailDTO.setNomeProfessor(professorNovo.getNome());
                    aulaCriadaEmailDTO.setNomeEspecialidade(agendamentoSalvo.getEspecialidade().getNome());

                    EmailRequestDTO emailRequestDTO = new EmailRequestDTO();
                    emailRequestDTO.setPayload(aulaCriadaEmailDTO);
                    emailRequestDTO.setTypeEmail(TipoEmail.AULA_CRIADA);
                    emailRequestDTO.setDestinatario(professorNovo.getEmail());

                     rabbitMQ.enviarPraFilaDeEmails(emailRequestDTO);
                }
            } else {
                // Caso normal: atualização sem troca de professor - notificar apenas o professor atual
                if (professorNovo.getNotificacaoAtiva() != null && professorNovo.getNotificacaoAtiva()) {
                    logger.debug("Enviando notificação de atualização para professor: {}", professorNovo.getNome());
                    List<String> nomesAlunos = agendamentoSalvo.getAgendamentoAlunos().stream()
                            .map(aa -> aa.getAluno().getNome())
                            .collect(Collectors.toList());

                    AulaAtualizadaEmailDTO aulaAtualizadaEmailDTO = new AulaAtualizadaEmailDTO();
                    aulaAtualizadaEmailDTO.setDataHoraAgendamento(agendamentoSalvo.getDataHora().toString());
                    aulaAtualizadaEmailDTO.setDestinatario(professorNovo.getEmail());
                    aulaAtualizadaEmailDTO.setNomeEspecialidade(agendamentoSalvo.getEspecialidade().getNome());
                    aulaAtualizadaEmailDTO.setNomeSala(agendamentoSalvo.getSala().getNome());
                    aulaAtualizadaEmailDTO.setNomesAlunos(nomesAlunos);
                    aulaAtualizadaEmailDTO.setNomeProfessor(professorNovo.getNome());

                    EmailRequestDTO emailRequestDTO = new EmailRequestDTO();
                    emailRequestDTO.setDestinatario(professorNovo.getEmail());
                    emailRequestDTO.setPayload(aulaAtualizadaEmailDTO);
                    emailRequestDTO.setTypeEmail(TipoEmail.AULA_ATUALIZADA);
                    rabbitMQ.enviarPraFilaDeEmails(emailRequestDTO);
                }
            }
            
            AgendamentoResponseDTO response = toResponseDTO(agendamentoSalvo);
            logger.info("Agendamento atualizado com sucesso. ID: {}", agendamentoId);
            return response;
        } catch (BusinessException e) {
            logger.warn("Falha ao atualizar agendamento ID {}: {}", agendamentoId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar agendamento ID: {}", agendamentoId, e);
            throw e;
        }
    }

    /**
     * Exclui um agendamento do sistema.
     *
     * @param id ID do agendamento a ser excluído
     * @throws EntidadeNaoEncontradaException se o agendamento não for encontrado
     */
    @Transactional
    public void excluirAgendamento(Long id) {
        logger.info("Tentativa de excluir agendamento ID: {}", id);
        
        try {
            if (!agendamentoRepository.existsById(id)) {
                throw new EntidadeNaoEncontradaException("Agendamento não encontrado");
            }
           Agendamento agendamento = agendamentoRepository.findById(id).orElseThrow();
            
            // Notificar professor sobre cancelamento (se tiver notificação ativa)
            Professor professor = agendamento.getProfessor();
            if (professor.getNotificacaoAtiva() != null && professor.getNotificacaoAtiva()) {
                logger.debug("Enviando notificação de cancelamento para professor: {}", professor.getNome());

                AulaCanceladaEmailDTO aulaCanceladaEmailDTO = new AulaCanceladaEmailDTO();
                aulaCanceladaEmailDTO.setDataHoraAgendamento(agendamento.getDataHora().toString());
                aulaCanceladaEmailDTO.setNomeProfessor(professor.getNome());
                aulaCanceladaEmailDTO.setNomeEspecialidade(agendamento.getEspecialidade().getNome());
                aulaCanceladaEmailDTO.setNomeSala(agendamento.getSala().getNome());

                EmailRequestDTO emailRequestDTO = new EmailRequestDTO();
                emailRequestDTO.setTypeEmail(TipoEmail.AULA_CANCELADA);
                emailRequestDTO.setDestinatario(professor.getEmail());
                emailRequestDTO.setPayload(aulaCanceladaEmailDTO);

                rabbitMQ.enviarPraFilaDeEmails(emailRequestDTO);
            }

            for (Aluno aluno : agendamento.getAlunos()) {
                System.out.println("Verificando notificação do aluno: " + aluno.getNome());

                if (aluno.getNotificacaoAtiva() != null && aluno.getNotificacaoAtiva()) {
                    logger.debug("Enviando notificação de cancelamento para aluno: {}", aluno.getNome());

                    AulaCanceladaAlunoEmailDTO aulaCanceladaAlunoEmailDTO = new AulaCanceladaAlunoEmailDTO();
                    aulaCanceladaAlunoEmailDTO.setNomeAluno(aluno.getNome());
                    aulaCanceladaAlunoEmailDTO.setDataHoraAgendamento(agendamento.getDataHora().toString());
                    aulaCanceladaAlunoEmailDTO.setNomeEspecialidade(agendamento.getEspecialidade().getNome());
                    aulaCanceladaAlunoEmailDTO.setNomeSala(agendamento.getSala().getNome());

                    EmailRequestDTO emailRequestDTO = new EmailRequestDTO();
                    emailRequestDTO.setTypeEmail(TipoEmail.AULA_CANCELADA_ALUNO);
                    emailRequestDTO.setDestinatario(aluno.getEmail());
                    emailRequestDTO.setPayload(aulaCanceladaAlunoEmailDTO);

                    rabbitMQ.enviarPraFilaDeEmails(emailRequestDTO);
                }
            }
            
            agendamentoRepository.deleteById(id);
            logger.info("Agendamento excluído com sucesso. ID: {}", id);
        } catch (BusinessException e) {
            logger.warn("Falha ao excluir agendamento ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao excluir agendamento ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Registra a presença dos alunos em um agendamento.
     * Apenas pode ser executado após a data/hora do agendamento.
     *
     * @param agendamentoId ID do agendamento
     * @param presencas Mapa com o ID do aluno como chave e o status de presença como valor
     * @throws EntidadeNaoEncontradaException se o agendamento ou aluno não for encontrado
     * @throws OperacaoInvalidaException se tentar registrar presença antes da data/hora do agendamento
     */
    @Transactional
    public void registrarPresencas(Long agendamentoId, Map<Long, StatusPresenca> presencas) {
        logger.info("Tentativa de registrar presenças para agendamento ID: {}", agendamentoId);
        
        try {
            Agendamento agendamento = buscarPorId(agendamentoId);
            
            // Validar se a aula já aconteceu
            LocalDateTime agora = LocalDateTime.now();
            if (agendamento.getDataHora().isAfter(agora)) {
                throw new OperacaoInvalidaException("Não é possível registrar presença antes da data/hora da aula.");
            }
            
            // Validar se todos os alunos pertencem ao agendamento usando agendamentoAlunos diretamente
            Set<Long> alunoIdsAgendamento = agendamento.getAgendamentoAlunos().stream()
                    .map(aa -> aa.getAluno().getId())
                    .collect(Collectors.toSet());
            
            for (Long alunoId : presencas.keySet()) {
                if (!alunoIdsAgendamento.contains(alunoId)) {
                    throw new OperacaoInvalidaException("Aluno com ID " + alunoId + " não pertence a este agendamento.");
                }
            }
            
            // Atualizar presenças
            for (Map.Entry<Long, StatusPresenca> entry : presencas.entrySet()) {
                Long alunoId = entry.getKey();
                StatusPresenca status = entry.getValue();
                
                AgendamentoAluno agendamentoAluno = agendamento.getAgendamentoAlunos().stream()
                        .filter(aa -> aa.getAluno().getId().equals(alunoId))
                        .findFirst()
                        .orElseThrow(() -> new EntidadeNaoEncontradaException("Aluno não encontrado no agendamento"));
                
                agendamentoAluno.setStatusPresenca(status);
                agendamentoAlunoRepository.save(agendamentoAluno);
                logger.debug("Presença registrada: Aluno ID {} - Status {}", alunoId, status);
            }
            
            logger.info("Presenças registradas com sucesso para agendamento ID: {}", agendamentoId);
        } catch (BusinessException e) {
            logger.warn("Falha ao registrar presenças para agendamento ID {}: {}", agendamentoId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao registrar presenças para agendamento ID: {}", agendamentoId, e);
            throw e;
        }
    }

    /**
     * Normaliza a data/hora para hora cheia, zerando minutos, segundos e nanossegundos.
     * Garante que todos os agendamentos sejam criados com horas cheias (ex: 10:00:00.000).
     * 
     * @param dataHora Data/hora a ser normalizada
     * @return Data/hora normalizada com minutos, segundos e nanossegundos zerados
     */
    private LocalDateTime normalizarDataHora(LocalDateTime dataHora) {
        if (dataHora == null) {
            return null;
        }
        return dataHora.withMinute(0).withSecond(0).withNano(0);
    }

    private Agendamento buscarPorId(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Agendamento não encontrado"));
    }

    public AgendamentoResponseDTO toResponseDTO(Agendamento agendamento) {
        AgendamentoResponseDTO dto = new AgendamentoResponseDTO();
        dto.setId(agendamento.getId());
        dto.setDataHora(agendamento.getDataHora());
        dto.setProfessor(agendamento.getProfessor().getNome());
        dto.setSala(agendamento.getSala().getNome());
        dto.setEspecialidade(agendamento.getEspecialidade().getNome());

        // Usar agendamentoAlunos diretamente para incluir informações de presença
        Set<AlunoAgendamentoResponseDTO> alunosDTO = agendamento.getAgendamentoAlunos()
                .stream()
                .map(aa -> {
                    AlunoAgendamentoResponseDTO alunoDTO = new AlunoAgendamentoResponseDTO();
                    alunoDTO.setId(aa.getAluno().getId());
                    alunoDTO.setNome(aa.getAluno().getNome());
                    String observacao = (aa.getObservacao() != null && !aa.getObservacao().trim().isEmpty())
                        ? aa.getObservacao()
                        : (aa.getAluno().getObservacao() != null ? aa.getAluno().getObservacao() : null);
                    alunoDTO.setObservacao(observacao);
                    alunoDTO.setAlunoComLimitacoesFisicas(
                        aa.getAluno().getAlunoComLimitacoesFisicas() != null
                            ? aa.getAluno().getAlunoComLimitacoesFisicas()
                            : false
                    );
                    alunoDTO.setStatus(aa.getAluno().getStatus());
                    // Status de presença pode ser adicionado ao DTO se necessário
                    return alunoDTO;
                })
                .collect(Collectors.toSet());

        dto.setAlunos(alunosDTO);
        return dto;
    }

    @Transactional
    public void atualizarObservacaoAluno(Long agendamentoId, Long alunoId, String observacao) {
        logger.info("Tentativa de atualizar observação do aluno {} no agendamento {}", alunoId, agendamentoId);
        try {
            Agendamento agendamento = buscarPorId(agendamentoId);

            AgendamentoAluno agendamentoAluno = agendamento.getAgendamentoAlunos().stream()
                    .filter(aa -> aa.getAluno().getId().equals(alunoId))
                    .findFirst()
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Aluno não encontrado neste agendamento"));

            agendamentoAluno.setObservacao(observacao);
            agendamentoAlunoRepository.save(agendamentoAluno);

            logger.info("Observação atualizada com sucesso para aluno {} no agendamento {}", alunoId, agendamentoId);
        } catch (Exception e) {
            logger.error("Erro ao atualizar observação do aluno {} no agendamento {}", alunoId, agendamentoId, e);
            throw e;
        }
    }


}