package com.onePilates.agendamento.agendamento.validator;

import com.onePilates.agendamento.agendamento.dto.AgendamentoDTO;
import com.onePilates.agendamento.agendamento.repository.AgendamentoRepository;
import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.ausencia.repository.AusenciaRepository;
import com.onePilates.agendamento.especialidade.repository.EspecialidadeRepository;
import com.onePilates.agendamento.exception.AlunoInativoException;
import com.onePilates.agendamento.exception.BusinessException;
import com.onePilates.agendamento.exception.ConflitoHorarioException;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.exception.EquipamentoPCDInsuficienteException;
import com.onePilates.agendamento.exception.EspecialidadeIncompativelException;
import com.onePilates.agendamento.exception.OperacaoInvalidaException;
import com.onePilates.agendamento.exception.ProfessorAusenteException;
import com.onePilates.agendamento.exception.ProfessorInativoException;
import com.onePilates.agendamento.exception.SalaLotadaException;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.Ausencia;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.model.Professor;
import com.onePilates.agendamento.model.Sala;
import com.onePilates.agendamento.model.enums.DiaSemana;
import com.onePilates.agendamento.professor.repository.ProfessorRepository;
import com.onePilates.agendamento.sala.repository.SalaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AgendamentoValidator {

    private static final Logger logger = LoggerFactory.getLogger(AgendamentoValidator.class);

    private final SalaRepository salaRepository;
    private final ProfessorRepository professorRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final AlunoRepository alunoRepository;
    private final AusenciaRepository ausenciaRepository;
    private final AgendamentoRepository agendamentoRepository;

    public AgendamentoValidator(SalaRepository salaRepository, ProfessorRepository professorRepository, EspecialidadeRepository especialidadeRepository, AlunoRepository alunoRepository, AusenciaRepository ausenciaRepository, AgendamentoRepository agendamentoRepository) {
        this.salaRepository = salaRepository;
        this.professorRepository = professorRepository;
        this.especialidadeRepository = especialidadeRepository;
        this.alunoRepository = alunoRepository;
        this.ausenciaRepository = ausenciaRepository;
        this.agendamentoRepository = agendamentoRepository;

    }

    /**
     * Valida todas as regras de negócio para um agendamento.
     * 
     * @param dto DTO contendo os dados do agendamento a ser validado
     * @throws BusinessException se alguma validação falhar
     */
    public void validar(AgendamentoDTO dto) {
        validar(dto, null);
    }

    /**
     * Valida todas as regras de negócio para um agendamento, excluindo um agendamento específico das verificações de conflito.
     * Este método é usado na atualização de agendamentos para evitar que o próprio agendamento seja considerado como conflito.
     * 
     * @param dto DTO contendo os dados do agendamento a ser validado
     * @param agendamentoIdExcluir ID do agendamento a ser excluído das verificações de conflito (null para criação)
     * @throws BusinessException se alguma validação falhar
     */
    public void validar(AgendamentoDTO dto, Long agendamentoIdExcluir) {
        logger.debug("Iniciando validação de agendamento para data/hora: {} (excluindo ID: {})", 
                dto.getDataHora(), agendamentoIdExcluir);

        LocalDateTime dataHora = dto.getDataHora();

        // Validação 1: Data/Hora (validação barata, deve ser primeira)
        validarDataHora(dataHora);

        if (dto.getAlunoIds() == null || dto.getAlunoIds().isEmpty()) {
            throw new OperacaoInvalidaException("A aula deve possuir pelo menos um aluno agendado.");
        }

        // Validação 2: Buscar entidades (necessário para outras validações)
        Sala sala = salaRepository.findById(dto.getSalaId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Sala não encontrada"));

        Professor professor = professorRepository.findById(dto.getProfessorId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Professor não encontrado"));

        Especialidade especialidade = especialidadeRepository.findById(dto.getEspecialidadeId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Especialidade não encontrada"));

        List<Aluno> alunos = alunoRepository.findAllById(dto.getAlunoIds());
        if (alunos.size() != dto.getAlunoIds().size()) {
            throw new EntidadeNaoEncontradaException("Um ou mais alunos não foram encontrados");
        }

        // Validações 3-4: Status (validações baratas, sem queries complexas)
        validarStatusAluno(alunos);
        validarStatusProfessor(professor);

        // Validações 5-6: Especialidades (validações médias, sem queries ao banco)
        validarEspecialidadeSala(sala, especialidade);
        validarEspecialidadeProfessor(professor, especialidade);

        // Validações 7-8: Lotação e equipamentos (validações médias)
        validarLotacaoSala(sala, alunos.size());
        validarEquipamentosPCD(sala, alunos);

        // Validação 9: Ausência do professor (query ao banco)
        validarAusenciaProfessor(professor, dataHora);

        // Validação 10: Conflitos (validações mais caras, queries ao banco - deixar por último)
        validarConflitosBasicos(dto, dataHora, alunos, agendamentoIdExcluir);

        logger.debug("Validação de agendamento concluída com sucesso");
    }

    /**
     * Valida se a data/hora do agendamento é válida (não é no passado, não é muito no futuro, etc).
     */
    private void validarDataHora(LocalDateTime dataHora) {
        LocalDateTime agora = LocalDateTime.now();
        
        // Validar se não é no passado
        if (dataHora.isBefore(agora)) {
            throw new OperacaoInvalidaException(
                String.format("Não é possível agendar em data/hora passada. Data/hora informada: %s",
                    dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")))
            );
        }
        
        // Validar se não é muito no futuro (mais de 1 ano)
        LocalDateTime umAnoDepois = agora.plusYears(1);
        if (dataHora.isAfter(umAnoDepois)) {
            throw new OperacaoInvalidaException(
                "Não é possível agendar com mais de 1 ano de antecedência."
            );
        }
        
        // Validar dias de funcionamento (Segunda a Sexta-feira — proibido Sábado e Domingo)
        DayOfWeek diaSemana = dataHora.getDayOfWeek();
        if (diaSemana == DayOfWeek.SATURDAY || diaSemana == DayOfWeek.SUNDAY) {
            throw new OperacaoInvalidaException(
                "Não é permitido agendar aulas aos sábados e domingos. O estúdio funciona apenas de segunda a sexta-feira."
            );
        }

        // Validar horário de expediente (8h às 20h)
        int hora = dataHora.getHour();
        if (hora < 8 || hora >= 20) {
            throw new OperacaoInvalidaException(
                String.format("O horário de agendamento deve estar entre 08:00 e 20:00. Horário informado: %02d:00",
                    hora)
            );
        }
    }

    private void validarConflitosBasicos(AgendamentoDTO dto, LocalDateTime dataHora, List<Aluno> alunos, Long agendamentoIdExcluir) {
        // Verificar conflito de professor, excluindo o agendamento atual se fornecido
        if (agendamentoRepository.existsByProfessorIdAndDataHoraExcludingId(dto.getProfessorId(), dataHora, agendamentoIdExcluir)) {
            // Buscar o agendamento conflitante exato para incluir na mensagem
            String mensagem = agendamentoRepository.findByProfessorIdAndDataHoraExcludingId(dto.getProfessorId(), dataHora, agendamentoIdExcluir)
                .map(conflito -> String.format("O professor %s já possui um agendamento em %s na sala %s.",
                    conflito.getProfessor().getNome(),
                    dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")),
                    conflito.getSala().getNome()))
                .orElse("O professor já possui um agendamento neste horário.");
            throw new ConflitoHorarioException(mensagem);
        }

        // Verificar conflito de sala, excluindo o agendamento atual se fornecido
        if (agendamentoRepository.existsBySalaIdAndDataHoraExcludingId(dto.getSalaId(), dataHora, agendamentoIdExcluir)) {
            // Buscar o agendamento conflitante exato para incluir na mensagem
            String mensagem = agendamentoRepository.findBySalaIdAndDataHoraExcludingId(dto.getSalaId(), dataHora, agendamentoIdExcluir)
                .map(conflito -> String.format("A sala %s já está ocupada em %s pelo professor %s.",
                    conflito.getSala().getNome(),
                    dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")),
                    conflito.getProfessor().getNome()))
                .orElse("A sala já está ocupada neste horário.");
            throw new ConflitoHorarioException(mensagem);
        }

        // Validar conflito de alunos, excluindo o agendamento atual se fornecido
        List<String> nomesIndisponiveis = alunos.stream()
                .filter(aluno -> !agendamentoRepository.findAgendamentosByAlunoAndDataHoraExcludingId(aluno, dataHora, agendamentoIdExcluir).isEmpty())
                .map(Aluno::getNome)
                .toList();

        if (!nomesIndisponiveis.isEmpty()) {
            String dataHoraFormatada = dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));
            throw new ConflitoHorarioException(
                String.format("Os seguintes alunos estão indisponíveis para o horário %s: %s",
                    dataHoraFormatada,
                    String.join(", ", nomesIndisponiveis))
            );
        }
    }

    private void validarLotacaoSala(Sala sala, int quantidadeAlunos) {
        if (quantidadeAlunos > sala.getQuantidadeMaximaAlunos()) {
            logger.warn("Tentativa de agendar {} alunos em sala que suporta apenas {}", quantidadeAlunos, sala.getQuantidadeMaximaAlunos());
            int alunosExcedentes = quantidadeAlunos - sala.getQuantidadeMaximaAlunos();
            throw new SalaLotadaException(
                String.format("A sala %s suporta no máximo %d alunos, mas foram solicitados %d alunos. Remova %d aluno(s) ou escolha outra sala.",
                    sala.getNome(),
                    sala.getQuantidadeMaximaAlunos(),
                    quantidadeAlunos,
                    alunosExcedentes)
            );
        }
    }

    private void validarEquipamentosPCD(Sala sala, List<Aluno> alunos) {
        List<Aluno> alunosComLimitacoes = alunos.stream()
            .filter(aluno -> Boolean.TRUE.equals(aluno.getAlunoComLimitacoesFisicas()))
            .toList();
        
        long quantidadeAlunosComLimitacoes = alunosComLimitacoes.size();

        if (quantidadeAlunosComLimitacoes > sala.getQuantidadeEquipamentosPCD()) {
            logger.warn("Tentativa de agendar {} alunos com limitações em sala com apenas {} equipamentos PCD",
                    quantidadeAlunosComLimitacoes, sala.getQuantidadeEquipamentosPCD());
            
            String nomesAlunosPCD = alunosComLimitacoes.stream()
                .map(Aluno::getNome)
                .collect(java.util.stream.Collectors.joining(", "));
            
            int alunosExcedentes = (int) (quantidadeAlunosComLimitacoes - sala.getQuantidadeEquipamentosPCD());
            
            throw new EquipamentoPCDInsuficienteException(
                String.format("A sala %s possui apenas %d equipamento(s) PCD, mas %d aluno(s) com limitações físicas foram agendados (%s). Remova %d aluno(s) com limitações ou escolha outra sala.",
                    sala.getNome(),
                    sala.getQuantidadeEquipamentosPCD(),
                    quantidadeAlunosComLimitacoes,
                    nomesAlunosPCD,
                    alunosExcedentes)
            );
        }
    }

    private void validarAusenciaProfessor(Professor professor, LocalDateTime dataHora) {
        List<Ausencia> ausencias = ausenciaRepository.findByProfessorId(professor.getId());

        boolean professorAusente = ausencias.stream()
            .anyMatch(ausencia -> {
                LocalDateTime inicio = ausencia.getDataInicio();
                LocalDateTime fim = ausencia.getDataFim();

                // Verificar se dataHora está dentro do período de ausência
                if (inicio != null && fim != null) {
                    return (dataHora.isEqual(inicio) || dataHora.isAfter(inicio)) 
                        && (dataHora.isEqual(fim) || dataHora.isBefore(fim));
                }

                // Se não há data específica, verificar dia da semana
                if (ausencia.getDiaSemanaInicio() != null && ausencia.getDiaSemanaFim() != null) {
                    DayOfWeek diaAgendamento = dataHora.getDayOfWeek();
                    DiaSemana diaSemanaAgendamento = mapearDayOfWeekParaDiaSemana(diaAgendamento);
                    return estaNoIntervaloDiasSemana(
                        diaSemanaAgendamento,
                        ausencia.getDiaSemanaInicio(),
                        ausencia.getDiaSemanaFim()
                    );
                }

                return false;
            });

        if (professorAusente) {
            logger.warn("Tentativa de agendar com professor {} que está ausente no horário {}", professor.getNome(), dataHora);
            throw new ProfessorAusenteException(
                String.format("O professor %s está ausente no horário agendado (%s).",
                    professor.getNome(),
                    dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
            );
        }
    }

    private void validarStatusAluno(List<Aluno> alunos) {
        List<String> alunosInativos = alunos.stream()
            .filter(aluno -> Boolean.FALSE.equals(aluno.getStatus()))
            .map(Aluno::getNome)
            .toList();

        if (!alunosInativos.isEmpty()) {
            logger.warn("Tentativa de agendar alunos inativos: {}", String.join(", ", alunosInativos));
            throw new AlunoInativoException(
                "Não é possível agendar alunos inativos: " + String.join(", ", alunosInativos)
            );
        }
    }

    private void validarStatusProfessor(Professor professor) {
        if (Boolean.FALSE.equals(professor.getStatus())) {
            logger.warn("Tentativa de agendar com professor inativo: {}", professor.getNome());
            throw new ProfessorInativoException(
                "Não é possível agendar com professor inativo: " + professor.getNome()
            );
        }
    }

    private void validarEspecialidadeSala(Sala sala, Especialidade especialidade) {
        boolean salaSuportaEspecialidade = sala.getEspecialidades().stream()
            .anyMatch(esp -> esp.getId().equals(especialidade.getId()));

        if (!salaSuportaEspecialidade) {
            logger.warn("Tentativa de agendar especialidade {} em sala {} que não a suporta", especialidade.getNome(), sala.getNome());
            
            String especialidadesDisponiveis = sala.getEspecialidades().stream()
                .map(Especialidade::getNome)
                .collect(java.util.stream.Collectors.joining(", "));
            
            String mensagem = String.format("A sala %s não suporta a especialidade %s.",
                sala.getNome(),
                especialidade.getNome());
            
            if (!especialidadesDisponiveis.isEmpty()) {
                mensagem += " Especialidades disponíveis: " + especialidadesDisponiveis + ".";
            }
            
            throw new EspecialidadeIncompativelException(mensagem);
        }
    }

    private void validarEspecialidadeProfessor(Professor professor, Especialidade especialidade) {
        boolean professorLecionaEspecialidade = professor.getEspecialidades().stream()
            .anyMatch(esp -> esp.getId().equals(especialidade.getId()));

        if (!professorLecionaEspecialidade) {
            logger.warn("Tentativa de agendar especialidade {} com professora {} que não a atende", 
                    especialidade.getNome(), professor.getNome());
            
            String especialidadesDisponiveis = professor.getEspecialidades().stream()
                .map(Especialidade::getNome)
                .collect(java.util.stream.Collectors.joining(", "));
            
            String mensagem = String.format("A professora %s não atende a especialidade %s.",
                professor.getNome(),
                especialidade.getNome());
            
            if (!especialidadesDisponiveis.isEmpty()) {
                mensagem += " Especialidades disponíveis: " + especialidadesDisponiveis + ".";
            }
            
            throw new EspecialidadeIncompativelException(mensagem);
        }
    }

    private DiaSemana mapearDayOfWeekParaDiaSemana(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> DiaSemana.SEGUNDA;
            case TUESDAY -> DiaSemana.TERCA;
            case WEDNESDAY -> DiaSemana.QUARTA;
            case THURSDAY -> DiaSemana.QUINTA;
            case FRIDAY -> DiaSemana.SEXTA;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }

    private boolean estaNoIntervaloDiasSemana(DiaSemana dia, DiaSemana inicio, DiaSemana fim) {
        Map<DiaSemana, Integer> ordem = Map.of(
            DiaSemana.SEGUNDA, 1,
            DiaSemana.TERCA, 2,
            DiaSemana.QUARTA, 3,
            DiaSemana.QUINTA, 4,
            DiaSemana.SEXTA, 5,
            DiaSemana.SABADO, 6,
            DiaSemana.DOMINGO, 7
        );

        int diaNum = ordem.get(dia);
        int inicioNum = ordem.get(inicio);
        int fimNum = ordem.get(fim);

        if (inicioNum <= fimNum) {
            return diaNum >= inicioNum && diaNum <= fimNum;
        } else {
            // Intervalo que cruza o fim de semana (ex: SEXTA a SEGUNDA)
            return diaNum >= inicioNum || diaNum <= fimNum;
        }
    }


    public List<String> validarCriacao(AgendamentoDTO dto) {

        List<String> erros = new ArrayList<>();

        try {
            validarDataHora(dto.getDataHora());
        } catch (BusinessException e) {
            erros.add(e.getMessage());
        }

        Sala sala = null;
        Professor professor = null;
        Especialidade especialidade = null;
        List<Aluno> alunos = new ArrayList<>();

        try {
            sala = salaRepository.findById(dto.getSalaId())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Sala não encontrada"));
        } catch (BusinessException e) {
            erros.add(e.getMessage());
        }

        try {
            professor = professorRepository.findById(dto.getProfessorId())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Professor não encontrado"));
        } catch (BusinessException e) {
            erros.add(e.getMessage());
        }

        try {
            especialidade = especialidadeRepository.findById(dto.getEspecialidadeId())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Especialidade não encontrada"));
        } catch (BusinessException e) {
            erros.add(e.getMessage());
        }

        try {
            alunos = alunoRepository.findAllById(dto.getAlunoIds());

            if (alunos.size() != dto.getAlunoIds().size()) {
                throw new EntidadeNaoEncontradaException("Um ou mais alunos não foram encontrados");
            }

        } catch (BusinessException e) {
            erros.add(e.getMessage());
        }

        // só executa regras dependentes se entidades existirem

        if (professor != null) {
            try {
                validarStatusProfessor(professor);
            } catch (BusinessException e) {
                erros.add(e.getMessage());
            }
        }

        if (!alunos.isEmpty()) {
            try {
                validarStatusAluno(alunos);
            } catch (BusinessException e) {
                erros.add(e.getMessage());
            }
        }

        if (sala != null && especialidade != null) {
            try {
                validarEspecialidadeSala(sala, especialidade);
            } catch (BusinessException e) {
                erros.add(e.getMessage());
            }
        }

        if (professor != null && especialidade != null) {
            try {
                validarEspecialidadeProfessor(professor, especialidade);
            } catch (BusinessException e) {
                erros.add(e.getMessage());
            }
        }

        if (sala != null) {
            try {
                validarLotacaoSala(sala, alunos.size());
            } catch (BusinessException e) {
                erros.add(e.getMessage());
            }

            try {
                validarEquipamentosPCD(sala, alunos);
            } catch (BusinessException e) {
                erros.add(e.getMessage());
            }
        }

        if (professor != null) {
            try {
                validarAusenciaProfessor(professor, dto.getDataHora());
            } catch (BusinessException e) {
                erros.add(e.getMessage());
            }
        }

        try {
            validarConflitosBasicos(dto, dto.getDataHora(), alunos, null);
        } catch (BusinessException e) {
            erros.add(e.getMessage());
        }

        return erros;
    }
    public List<String> validarCriacaoListandoErros(AgendamentoDTO dto) {
        List<String> erros = new ArrayList<>();

        try {
            validarDataHora(dto.getDataHora());
        } catch (BusinessException e) {
            erros.add(e.getMessage());
        }

        Sala sala = null;
        Professor professor = null;
        Especialidade especialidade = null;
        List<Aluno> alunos = new ArrayList<>();

        try {
            sala = salaRepository.findById(dto.getSalaId())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Sala não encontrada"));
        } catch (BusinessException e) {
            erros.add(e.getMessage());
        }

        try {
            professor = professorRepository.findById(dto.getProfessorId())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Professor não encontrado"));
        } catch (BusinessException e) {
            erros.add(e.getMessage());
        }

        try {
            especialidade = especialidadeRepository.findById(dto.getEspecialidadeId())
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Especialidade não encontrada"));
        } catch (BusinessException e) {
            erros.add(e.getMessage());
        }

        try {
            alunos = alunoRepository.findAllById(dto.getAlunoIds());
            if (alunos.size() != dto.getAlunoIds().size()) {
                throw new EntidadeNaoEncontradaException("Um ou mais alunos não foram encontrados");
            }
        } catch (BusinessException e) {
            erros.add(e.getMessage());
        }

        // só executa regras dependentes se entidades existirem
        if (professor != null) {
            try {
                validarStatusProfessor(professor);
            } catch (BusinessException e) {
                erros.add(e.getMessage());
            }
        }

        if (!alunos.isEmpty()) {
            try {
                validarStatusAluno(alunos);
            } catch (BusinessException e) {
                erros.add(e.getMessage());
            }
        }

        if (sala != null && especialidade != null) {
            try {
                validarEspecialidadeSala(sala, especialidade);
            } catch (BusinessException e) {
                erros.add(e.getMessage());
            }
        }

        if (professor != null && especialidade != null) {
            try {
                validarEspecialidadeProfessor(professor, especialidade);
            } catch (BusinessException e) {
                erros.add(e.getMessage());
            }
        }

        if (sala != null) {
            try {
                validarLotacaoSala(sala, alunos.size());
            } catch (BusinessException e) {
                erros.add(e.getMessage());
            }

            try {
                validarEquipamentosPCD(sala, alunos);
            } catch (BusinessException e) {
                erros.add(e.getMessage());
            }
        }

        if (professor != null) {
            try {
                validarAusenciaProfessor(professor, dto.getDataHora());
            } catch (BusinessException e) {
                erros.add(e.getMessage());
            }
        }

        // 🔑 Aqui usamos o acumulativo para não lançar exceção
        validarConflitosBasicosAcumulativo(dto, dto.getDataHora(), alunos, null, erros);

        return erros;
    }

    public void validarConflitosBasicosAcumulativo(
            AgendamentoDTO dto,
            LocalDateTime dataHora,
            List<Aluno> alunos,
            Long agendamentoIdExcluir,
            List<String> erros
    ) {

        // Conflito de professor
        if (agendamentoRepository.existsByProfessorIdAndDataHoraExcludingId(
                dto.getProfessorId(),
                dataHora,
                agendamentoIdExcluir
        )) {

            String mensagem = agendamentoRepository
                    .findByProfessorIdAndDataHoraExcludingId(
                            dto.getProfessorId(),
                            dataHora,
                            agendamentoIdExcluir
                    )
                    .map(conflito -> String.format(
                            "O professor %s já possui um agendamento em %s na sala %s.",
                            conflito.getProfessor().getNome(),
                            dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")),
                            conflito.getSala().getNome()
                    ))
                    .orElse("O professor já possui um agendamento neste horário.");

            erros.add(mensagem);
        }

        // Conflito de sala
        if (agendamentoRepository.existsBySalaIdAndDataHoraExcludingId(
                dto.getSalaId(),
                dataHora,
                agendamentoIdExcluir
        )) {

            String mensagem = agendamentoRepository
                    .findBySalaIdAndDataHoraExcludingId(
                            dto.getSalaId(),
                            dataHora,
                            agendamentoIdExcluir
                    )
                    .map(conflito -> String.format(
                            "A sala %s já está ocupada em %s pelo professor %s.",
                            conflito.getSala().getNome(),
                            dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")),
                            conflito.getProfessor().getNome()
                    ))
                    .orElse("A sala já está ocupada neste horário.");

            erros.add(mensagem);
        }

        // Conflito de alunos
        List<String> nomesIndisponiveis = alunos.stream()
                .filter(aluno ->
                        !agendamentoRepository
                                .findAgendamentosByAlunoAndDataHoraExcludingId(
                                        aluno,
                                        dataHora,
                                        agendamentoIdExcluir
                                )
                                .isEmpty()
                )
                .map(Aluno::getNome)
                .toList();

        if (!nomesIndisponiveis.isEmpty()) {

            String dataHoraFormatada = dataHora.format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")
            );

            erros.add(
                    String.format(
                            "Os seguintes alunos estão indisponíveis para o horário %s: %s",
                            dataHoraFormatada,
                            String.join(", ", nomesIndisponiveis)
                    )
            );
        }
    }

}



