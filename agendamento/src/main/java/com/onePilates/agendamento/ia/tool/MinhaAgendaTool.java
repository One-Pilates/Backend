package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.aluno.dto.AgendaItemDTO;
import com.onePilates.agendamento.aluno.dto.PacoteAulaResponseDTO;
import com.onePilates.agendamento.aluno.service.PacoteAulaService;
import com.onePilates.agendamento.aluno.service.PortalAlunoService;
import com.onePilates.agendamento.model.enums.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class MinhaAgendaTool implements FerramentaIA {

    private final PortalAlunoService portalAlunoService;
    private final PacoteAulaService pacoteAulaService;

    public MinhaAgendaTool(PortalAlunoService portalAlunoService, PacoteAulaService pacoteAulaService) {
        this.portalAlunoService = portalAlunoService;
        this.pacoteAulaService = pacoteAulaService;
    }

    @Override
    public String nome() { return "minha_agenda"; }

    @Override
    public String descricao() {
        return "Lista as próximas aulas do próprio aluno autenticado: horário, professor, sala, especialidade, "
                + "se a presença já foi confirmada e o saldo de aulas remanescentes. Não aceita alunoId.";
    }

    @Override
    public Set<Role> rolesPermitidas() { return Set.of(Role.ALUNO); }

    @Override
    public Map<String, Object> parametros() {
        return Map.of("type", "object", "properties", Map.of());
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        Long alunoId = usuario.usuarioId();
        List<Map<String, Object>> aulas = portalAlunoService.listarAgenda(alunoId, null, null).stream()
                .map(this::aula)
                .toList();
        List<Map<String, Object>> pacotes = pacoteAulaService.listar(alunoId).stream()
                .map(this::pacote)
                .toList();
        return Map.of("aulas", aulas, "aulasRemanescentes", pacotes);
    }

    private Map<String, Object> aula(AgendaItemDTO item) {
        return Map.of(
                "dataHora", item.getDataHora() == null ? "" : item.getDataHora().toString(),
                "professor", item.getProfessor() == null ? "" : item.getProfessor(),
                "sala", item.getSala() == null ? "" : item.getSala(),
                "especialidade", item.getEspecialidade() == null ? "" : item.getEspecialidade(),
                "presencaConfirmada", Boolean.TRUE.equals(item.getPresencaConfirmada()));
    }

    private Map<String, Object> pacote(PacoteAulaResponseDTO pacote) {
        return Map.of(
                "especialidade", pacote.getEspecialidade() == null ? "" : pacote.getEspecialidade(),
                "aulasRemanescentes", pacote.getAulasRemanescentes() == null ? 0 : pacote.getAulasRemanescentes());
    }
}
