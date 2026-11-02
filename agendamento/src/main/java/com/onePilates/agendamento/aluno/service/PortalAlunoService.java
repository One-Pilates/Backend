package com.onePilates.agendamento.aluno.service;

import com.onePilates.agendamento.agendamento.dto.AgendamentoPorDiaDTO;
import com.onePilates.agendamento.agendamento.dto.AulaPorEspecialidadeDTO;
import com.onePilates.agendamento.agendamento.repository.AgendamentoAlunoRepository;
import com.onePilates.agendamento.agendamento.service.AgendamentoService;
import com.onePilates.agendamento.aluno.dto.AgendaItemDTO;
import com.onePilates.agendamento.aluno.dto.AlunoResponseDTO;
import com.onePilates.agendamento.aluno.dto.HistoricoAlunoDTO;
import com.onePilates.agendamento.aluno.dto.KpisAlunoDTO;
import com.onePilates.agendamento.aluno.dto.PerfilAlunoDTO;
import com.onePilates.agendamento.aluno.dto.RespostaDashAlunoDTO;
import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.aluno.repository.CancelamentoAulaRepository;
import com.onePilates.agendamento.aluno.repository.FeedbackAulaRepository;
import com.onePilates.agendamento.email.dto.AulaCanceladaEmailDTO;
import com.onePilates.agendamento.email.dto.EmailRequestDTO;
import com.onePilates.agendamento.email.service.RabbitMQProducer;
import com.onePilates.agendamento.exception.AulaAindaNaoRealizadaException;
import com.onePilates.agendamento.exception.AulaJaRealizadaException;
import com.onePilates.agendamento.exception.CancelamentoForaDoPrazoException;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.exception.FeedbackJaExistenteException;
import com.onePilates.agendamento.exception.OperacaoInvalidaException;
import com.onePilates.agendamento.imagem.service.ImagemService;
import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.AgendamentoAluno;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.CancelamentoAula;
import com.onePilates.agendamento.model.FeedbackAula;
import com.onePilates.agendamento.model.Professor;
import com.onePilates.agendamento.model.enums.StatusPresenca;
import com.onePilates.agendamento.model.enums.TipoEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PortalAlunoService {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    private final AlunoRepository alunoRepository;
    private final AgendamentoAlunoRepository agendamentoAlunoRepository;
    private final CancelamentoAulaRepository cancelamentoAulaRepository;
    private final FeedbackAulaRepository feedbackAulaRepository;
    private final PacoteAulaService pacoteAulaService;
    private final AgendamentoService agendamentoService;
    private final RabbitMQProducer rabbitMQProducer;
    private final ImagemService imagemService;
    private final Clock clock;

    @Autowired
    public PortalAlunoService(AlunoRepository alunoRepository, AgendamentoAlunoRepository agendamentoAlunoRepository,
                              CancelamentoAulaRepository cancelamentoAulaRepository, FeedbackAulaRepository feedbackAulaRepository,
                              PacoteAulaService pacoteAulaService, AgendamentoService agendamentoService,
                              RabbitMQProducer rabbitMQProducer, ImagemService imagemService) {
        this(alunoRepository, agendamentoAlunoRepository, cancelamentoAulaRepository, feedbackAulaRepository,
                pacoteAulaService, agendamentoService, rabbitMQProducer, imagemService, Clock.system(ZONA));
    }

    public PortalAlunoService(AlunoRepository alunoRepository, AgendamentoAlunoRepository agendamentoAlunoRepository,
                              CancelamentoAulaRepository cancelamentoAulaRepository, FeedbackAulaRepository feedbackAulaRepository,
                              PacoteAulaService pacoteAulaService, AgendamentoService agendamentoService,
                              RabbitMQProducer rabbitMQProducer, ImagemService imagemService, Clock clock) {
        this.alunoRepository = alunoRepository;
        this.agendamentoAlunoRepository = agendamentoAlunoRepository;
        this.cancelamentoAulaRepository = cancelamentoAulaRepository;
        this.feedbackAulaRepository = feedbackAulaRepository;
        this.pacoteAulaService = pacoteAulaService;
        this.agendamentoService = agendamentoService;
        this.rabbitMQProducer = rabbitMQProducer;
        this.imagemService = imagemService;
        this.clock = clock;
    }

    public List<AgendaItemDTO> listarAgenda(Long alunoId, LocalDate de, LocalDate ate) {
        LocalDateTime agora = agora();
        return agendamentoAlunoRepository.findByAlunoId(alunoId).stream()
                .filter(v -> v.getAluno().getId().equals(alunoId))
                .filter(v -> v.getAgendamento().getDataHora().isAfter(agora))
                .filter(v -> de == null || !v.getAgendamento().getDataHora().isBefore(de.atStartOfDay()))
                .filter(v -> ate == null || v.getAgendamento().getDataHora().isBefore(ate.plusDays(1).atStartOfDay()))
                .map(this::paraAgenda)
                .toList();
    }

    @Transactional
    public AgendamentoAluno confirmarPresenca(Long alunoId, Long agendamentoId) {
        AgendamentoAluno vinculo = vinculoDoAluno(alunoId, agendamentoId);
        if (!vinculo.getAgendamento().getDataHora().isAfter(agora())) {
            throw new AulaJaRealizadaException();
        }
        vinculo.setPresencaConfirmada(true);
        vinculo.setDataConfirmacao(agora());
        return agendamentoAlunoRepository.save(vinculo);
    }

    @Transactional
    public void cancelar(Long alunoId, Long agendamentoId) {
        AgendamentoAluno vinculo = vinculoDoAluno(alunoId, agendamentoId);
        Duration antecedencia = Duration.between(agora(), vinculo.getAgendamento().getDataHora());
        if (antecedencia.compareTo(Duration.ofHours(24)) < 0) {
            throw new CancelamentoForaDoPrazoException();
        }
        cancelamentoAulaRepository.save(snapshot(vinculo));
        List<AgendamentoAluno> daAula = agendamentoAlunoRepository.findByAgendamentoId(vinculo.getAgendamento().getId());
        if (daAula.size() <= 1) {
            agendamentoService.excluirAgendamento(vinculo.getAgendamento().getId());
            return;
        }
        pacoteAulaService.estornar(vinculo);
        agendamentoAlunoRepository.delete(vinculo);
        notificarProfessor(vinculo);
    }

    @Transactional
    public FeedbackAula criarFeedback(Long alunoId, Long agendamentoId, Integer nota, String comentario) {
        AgendamentoAluno vinculo = vinculoDoAluno(alunoId, agendamentoId);
        if (vinculo.getAgendamento().getDataHora().isAfter(agora())) {
            throw new AulaAindaNaoRealizadaException();
        }
        validarNota(nota);
        if (feedbackAulaRepository.findByAgendamentoIdAndAlunoId(agendamentoId, alunoId).isPresent()) {
            throw new FeedbackJaExistenteException();
        }
        FeedbackAula feedback = new FeedbackAula();
        feedback.setAgendamentoId(agendamentoId);
        feedback.setAlunoId(alunoId);
        feedback.setNota(nota);
        feedback.setComentario(comentario);
        return feedbackAulaRepository.save(feedback);
    }

    @Transactional
    public FeedbackAula atualizarFeedback(Long alunoId, Long agendamentoId, Integer nota, String comentario) {
        FeedbackAula feedback = feedbackAulaRepository.findByAgendamentoIdAndAlunoId(agendamentoId, alunoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Feedback não encontrado"));
        validarNota(nota);
        feedback.setNota(nota);
        if (comentario != null) {
            feedback.setComentario(comentario);
        }
        return feedbackAulaRepository.save(feedback);
    }

    public AlunoResponseDTO obterPerfil(Long alunoId) {
        return paraPerfil(buscarAluno(alunoId));
    }

    @Transactional
    public Aluno atualizarPerfil(Long alunoId, PerfilAlunoDTO dto) {
        Aluno aluno = buscarAluno(alunoId);
        if (dto.getNotificacaoAtiva() != null) {
            aluno.setNotificacaoAtiva(dto.getNotificacaoAtiva());
        }
        if (dto.getTipoContato() != null) {
            aluno.setTipoContato(dto.getTipoContato());
        }
        return alunoRepository.save(aluno);
    }

    @Transactional
    public String uploadFoto(Long alunoId, MultipartFile arquivo) {
        Aluno aluno = buscarAluno(alunoId);
        String caminho = imagemService.uploadFotoAluno(arquivo, alunoId);
        aluno.setFoto(caminho);
        alunoRepository.save(aluno);
        return caminho;
    }

    public RespostaDashAlunoDTO dashboard(Long alunoId, int qtdUltimosDias) {
        if (qtdUltimosDias < 1 || qtdUltimosDias > 365) {
            throw new OperacaoInvalidaException("Período deve estar entre 1 e 365 dias");
        }
        LocalDate hoje = LocalDate.now(clock);
        LocalDateTime inicio = hoje.minusDays(qtdUltimosDias).atStartOfDay();
        LocalDateTime fim = hoje.plusDays(1).atStartOfDay();

        List<HistoricoAlunoDTO> historico = new ArrayList<>();
        for (AgendamentoAluno vinculo : agendamentoAlunoRepository.findByAlunoId(alunoId)) {
            if (!vinculo.getAluno().getId().equals(alunoId)) {
                continue;
            }
            LocalDateTime quando = vinculo.getAgendamento().getDataHora();
            if (quando.isBefore(inicio) || !quando.isBefore(fim)) {
                continue;
            }
            historico.add(historicoDe(vinculo));
        }
        for (CancelamentoAula cancelamento : cancelamentoAulaRepository.findByAlunoId(alunoId)) {
            if (cancelamento.getDataHora().isBefore(inicio) || !cancelamento.getDataHora().isBefore(fim)) {
                continue;
            }
            HistoricoAlunoDTO item = new HistoricoAlunoDTO();
            item.setDataHora(cancelamento.getDataHora());
            item.setEspecialidade(cancelamento.getEspecialidade());
            item.setProfessor(cancelamento.getProfessor());
            item.setSala(cancelamento.getSala());
            item.setSituacao("CANCELADA");
            historico.add(item);
        }

        RespostaDashAlunoDTO resposta = new RespostaDashAlunoDTO();
        resposta.setHistorico(historico);
        resposta.setKpis(kpis(historico, alunoId));
        resposta.setAulasPorDiaSemana(porDia(historico));
        resposta.setAulasPorEspecialidade(porEspecialidade(historico));
        return resposta;
    }

    private KpisAlunoDTO kpis(List<HistoricoAlunoDTO> historico, Long alunoId) {
        KpisAlunoDTO kpis = new KpisAlunoDTO();
        long presencas = historico.stream().filter(h -> "PRESENTE".equals(h.getSituacao())).count();
        long faltas = historico.stream().filter(h -> "FALTA".equals(h.getSituacao())).count();
        long cancelamentos = historico.stream().filter(h -> "CANCELADA".equals(h.getSituacao())).count();
        kpis.setPresencas(presencas);
        kpis.setFaltas(faltas);
        kpis.setCancelamentos(cancelamentos);
        kpis.setAulasNoPeriodo(historico.size());
        kpis.setTaxaPresenca(presencas + faltas == 0 ? 0d : (double) presencas / (presencas + faltas));
        kpis.setEspecialidadeMaisFrequentada(maisFrequente(historico, true));
        kpis.setDiaSemanaMaisFrequente(diaMaisFrequente(historico));
        kpis.setAulasRemanescentes(pacoteAulaService.listar(alunoId));
        return kpis;
    }

    private String maisFrequente(List<HistoricoAlunoDTO> historico, boolean soPresenca) {
        Map<String, Long> contagem = new HashMap<>();
        for (HistoricoAlunoDTO item : historico) {
            if (soPresenca && !"PRESENTE".equals(item.getSituacao())) {
                continue;
            }
            if (item.getEspecialidade() == null) {
                continue;
            }
            contagem.merge(item.getEspecialidade(), 1L, Long::sum);
        }
        return contagem.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }

    private String diaMaisFrequente(List<HistoricoAlunoDTO> historico) {
        Map<String, Long> contagem = new HashMap<>();
        for (HistoricoAlunoDTO item : historico) {
            if (!"PRESENTE".equals(item.getSituacao()) || item.getDataHora() == null) {
                continue;
            }
            contagem.merge(dia(item.getDataHora().getDayOfWeek()), 1L, Long::sum);
        }
        return contagem.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }

    private List<AgendamentoPorDiaDTO> porDia(List<HistoricoAlunoDTO> historico) {
        Map<String, Long> contagem = new HashMap<>();
        for (HistoricoAlunoDTO item : historico) {
            if (item.getDataHora() == null) {
                continue;
            }
            contagem.merge(dia(item.getDataHora().getDayOfWeek()), 1L, Long::sum);
        }
        return contagem.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new AgendamentoPorDiaDTO(e.getKey(), e.getValue()))
                .toList();
    }

    private List<AulaPorEspecialidadeDTO> porEspecialidade(List<HistoricoAlunoDTO> historico) {
        Map<String, Long> contagem = new HashMap<>();
        for (HistoricoAlunoDTO item : historico) {
            if (item.getEspecialidade() == null || "CANCELADA".equals(item.getSituacao())) {
                continue;
            }
            contagem.merge(item.getEspecialidade(), 1L, Long::sum);
        }
        long total = contagem.values().stream().mapToLong(Long::longValue).sum();
        return contagem.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry<String, Long>::getValue).reversed())
                .map(e -> new AulaPorEspecialidadeDTO(null, e.getKey(), total == 0 ? 0d : (e.getValue() * 100d) / total))
                .toList();
    }

    private String dia(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "SEGUNDA";
            case TUESDAY -> "TERCA";
            case WEDNESDAY -> "QUARTA";
            case THURSDAY -> "QUINTA";
            case FRIDAY -> "SEXTA";
            case SATURDAY -> "SABADO";
            case SUNDAY -> "DOMINGO";
        };
    }

    private HistoricoAlunoDTO historicoDe(AgendamentoAluno vinculo) {
        HistoricoAlunoDTO item = new HistoricoAlunoDTO();
        Agendamento agendamento = vinculo.getAgendamento();
        item.setDataHora(agendamento.getDataHora());
        item.setEspecialidade(agendamento.getEspecialidade().getNome());
        item.setProfessor(agendamento.getProfessor().getNome());
        item.setSala(agendamento.getSala().getNome());
        StatusPresenca status = vinculo.getStatusPresenca() == null ? StatusPresenca.PENDENTE : vinculo.getStatusPresenca();
        item.setSituacao(status.name());
        return item;
    }

    private void notificarProfessor(AgendamentoAluno vinculo) {
        Professor professor = vinculo.getAgendamento().getProfessor();
        if (!Boolean.TRUE.equals(professor.getNotificacaoAtiva())) {
            return;
        }
        AulaCanceladaEmailDTO payload = new AulaCanceladaEmailDTO();
        payload.setNomeProfessor(professor.getNome());
        payload.setDataHoraAgendamento(vinculo.getAgendamento().getDataHora().toString());
        payload.setNomeEspecialidade(vinculo.getAgendamento().getEspecialidade().getNome());
        payload.setNomeSala(vinculo.getAgendamento().getSala().getNome());
        EmailRequestDTO email = new EmailRequestDTO();
        email.setTypeEmail(TipoEmail.AULA_CANCELADA);
        email.setDestinatario(professor.getEmail());
        email.setPayload(payload);
        rabbitMQProducer.enviarPraFilaDeEmails(email);
    }

    private CancelamentoAula snapshot(AgendamentoAluno vinculo) {
        CancelamentoAula cancelamento = new CancelamentoAula();
        cancelamento.setAlunoId(vinculo.getAluno().getId());
        cancelamento.setNomeAluno(vinculo.getAluno().getNome());
        cancelamento.setDataHora(vinculo.getAgendamento().getDataHora());
        cancelamento.setEspecialidade(vinculo.getAgendamento().getEspecialidade().getNome());
        cancelamento.setProfessor(vinculo.getAgendamento().getProfessor().getNome());
        cancelamento.setSala(vinculo.getAgendamento().getSala().getNome());
        cancelamento.setCanceladoEm(agora());
        return cancelamento;
    }

    private AgendaItemDTO paraAgenda(AgendamentoAluno vinculo) {
        AgendaItemDTO item = new AgendaItemDTO();
        item.setAgendamentoId(vinculo.getAgendamento().getId());
        item.setDataHora(vinculo.getAgendamento().getDataHora());
        item.setProfessor(vinculo.getAgendamento().getProfessor().getNome());
        item.setSala(vinculo.getAgendamento().getSala().getNome());
        item.setEspecialidade(vinculo.getAgendamento().getEspecialidade().getNome());
        item.setPresencaConfirmada(Boolean.TRUE.equals(vinculo.getPresencaConfirmada()));
        return item;
    }

    private AlunoResponseDTO paraPerfil(Aluno aluno) {
        AlunoResponseDTO dto = new AlunoResponseDTO();
        dto.setId(aluno.getId());
        dto.setNome(aluno.getNome());
        dto.setEmail(aluno.getEmail());
        dto.setCpf(aluno.getCpf());
        dto.setStatus(aluno.getStatus());
        dto.setTelefone(aluno.getTipoContato());
        dto.setNotificacaoAtiva(aluno.getNotificacaoAtiva());
        return dto;
    }

    private AgendamentoAluno vinculoDoAluno(Long alunoId, Long agendamentoId) {
        return agendamentoAlunoRepository.findByAlunoId(alunoId).stream()
                .filter(v -> v.getAluno().getId().equals(alunoId))
                .filter(v -> v.getAgendamento().getId().equals(agendamentoId))
                .findFirst()
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Aula não encontrada para este aluno"));
    }

    private Aluno buscarAluno(Long alunoId) {
        return alunoRepository.findById(alunoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Aluno não encontrado"));
    }

    private void validarNota(Integer nota) {
        if (nota == null || nota < 1 || nota > 5) {
            throw new OperacaoInvalidaException("A nota deve ser de 1 a 5.");
        }
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }
}
