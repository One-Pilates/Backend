package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.ausencia.dto.AusenciaDTO;
import com.onePilates.agendamento.ausencia.dto.AusenciaResponseDTO;
import com.onePilates.agendamento.ausencia.service.AusenciaService;
import com.onePilates.agendamento.model.enums.DiaSemana;
import com.onePilates.agendamento.model.enums.Role;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ausências do próprio professor: consultar, registrar e cancelar.
 * O professor é sempre o usuário logado; o modelo não consegue escolher outro.
 */
@Component
public class MinhasAusenciasTool implements FerramentaIA {

    private final AusenciaService ausenciaService;

    public MinhasAusenciasTool(AusenciaService ausenciaService) {
        this.ausenciaService = ausenciaService;
    }

    @Override
    public String nome() { return "minhas_ausencias"; }

    @Override
    public String descricao() {
        return "Gerencia as ausências do PRÓPRIO professor logado. acao=listar mostra as ausências; "
                + "acao=registrar cria uma ausência (precisa de dataInicio, dataFim e motivo); "
                + "acao=cancelar apaga uma ausência pelo ausenciaId. Só use registrar ou cancelar quando o usuário pedir "
                + "explicitamente. Datas no formato AAAA-MM-DD (dia inteiro) ou AAAA-MM-DDTHH:MM.";
    }

    /** Só professor, e só sobre si mesmo. */
    @Override
    public Set<Role> rolesPermitidas() { return Set.of(Role.PROFESSOR); }

    @Override
    public Map<String, Object> parametros() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "acao", Map.of("type", "string", "enum", List.of("listar", "registrar", "cancelar")),
                        "dataInicio", Map.of("type", "string"),
                        "dataFim", Map.of("type", "string", "description", "Se for um dia só, repita a data de início"),
                        "motivo", Map.of("type", "string"),
                        "ausenciaId", Map.of("type", "integer")),
                "required", List.of("acao"));
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        String acao = String.valueOf(argumentos.get("acao"));
        return switch (acao) {
            case "listar" -> Map.of("ausencias", minhas(usuario).stream().map(this::resumo).toList());
            case "registrar" -> registrar(argumentos, usuario);
            case "cancelar" -> cancelar(argumentos, usuario);
            default -> Map.of("erro", "acao inválida: use listar, registrar ou cancelar");
        };
    }

    private Object registrar(Map<String, Object> args, ContextoUsuario usuario) {
        String motivo = String.valueOf(args.getOrDefault("motivo", "")).trim();
        if (motivo.isEmpty() || "null".equals(motivo)) {
            return Map.of("erro", "Informe o motivo da ausência");
        }
        LocalDateTime inicio;
        LocalDateTime fim;
        try {
            inicio = lerData(args.get("dataInicio"), false);
            fim = lerData(args.get("dataFim") != null ? args.get("dataFim") : args.get("dataInicio"), true);
        } catch (DateTimeParseException | NullPointerException e) {
            return Map.of("erro", "Datas inválidas, use AAAA-MM-DD ou AAAA-MM-DDTHH:MM");
        }
        if (fim.isBefore(inicio)) {
            return Map.of("erro", "A data de fim não pode ser anterior à de início");
        }
        if (fim.isBefore(LocalDateTime.now())) {
            return Map.of("erro", "A ausência não pode estar totalmente no passado");
        }

        AusenciaDTO dto = new AusenciaDTO();
        dto.setProfessorId(usuario.usuarioId());
        dto.setDataInicio(inicio);
        dto.setDataFim(fim);
        dto.setDiaSemanaInicio(diaSemana(inicio.getDayOfWeek()));
        dto.setDiaSemanaFim(diaSemana(fim.getDayOfWeek()));
        dto.setMotivo(motivo);

        AusenciaResponseDTO criada = ausenciaService.registrarAusencia(dto);
        Map<String, Object> resultado = new java.util.HashMap<>(resumo(criada));
        resultado.put("sucesso", true);
        return resultado;
    }

    private Object cancelar(Map<String, Object> args, ContextoUsuario usuario) {
        Long id = ArgumentosTool.longObrigatorio(args, "ausenciaId");
        boolean ehMinha = minhas(usuario).stream().anyMatch(a -> id.equals(a.getId().longValue()));
        if (!ehMinha) {
            return Map.of("erro", "Ausência não encontrada entre as suas");
        }
        ausenciaService.deletarAusencia(id.intValue());
        return Map.of("sucesso", true, "ausenciaCancelada", id);
    }

    private List<AusenciaResponseDTO> minhas(ContextoUsuario usuario) {
        return ausenciaService.listarPorProfessor(usuario.usuarioId());
    }

    private Map<String, Object> resumo(AusenciaResponseDTO a) {
        return Map.of(
                "id", a.getId(),
                "inicio", String.valueOf(a.getDataInicio()),
                "fim", String.valueOf(a.getDataFim()),
                "motivo", String.valueOf(a.getMotivo()));
    }

    /** Só a data vira dia inteiro (00:00 no início, 23:59 no fim). */
    private LocalDateTime lerData(Object valor, boolean fimDoDia) {
        String texto = String.valueOf(valor).trim();
        if (texto.length() == 10) {
            LocalDate dia = LocalDate.parse(texto);
            return fimDoDia ? dia.atTime(23, 59) : dia.atStartOfDay();
        }
        return LocalDateTime.parse(texto);
    }

    private DiaSemana diaSemana(DayOfWeek dia) {
        return switch (dia) {
            case MONDAY -> DiaSemana.SEGUNDA;
            case TUESDAY -> DiaSemana.TERCA;
            case WEDNESDAY -> DiaSemana.QUARTA;
            case THURSDAY -> DiaSemana.QUINTA;
            case FRIDAY -> DiaSemana.SEXTA;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }
}
