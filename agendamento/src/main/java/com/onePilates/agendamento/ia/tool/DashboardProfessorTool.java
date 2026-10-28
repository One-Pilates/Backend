package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.dashboard.dto.RespostaDashProfessoraDTO;
import com.onePilates.agendamento.model.enums.Role;
import com.onePilates.agendamento.professor.dto.KPIsProfessorDTO;
import com.onePilates.agendamento.professor.service.ProfessorService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dashboard de um professor. Administrador consulta qualquer professor (informando professorId);
 * o professor só enxerga o PRÓPRIO dashboard. Secretaria não tem acesso, como no endpoint original.
 */
@Component
public class DashboardProfessorTool implements FerramentaIA {

    private final ProfessorService professorService;

    public DashboardProfessorTool(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @Override
    public String nome() { return "dashboard_professor"; }

    @Override
    public String descricao() {
        return "Indicadores do dashboard de um professor nos últimos N dias (padrão 30, máximo 365): alunos atendidos, "
                + "sessões realizadas, dia da semana de maior atendimento, especialidade mais requisitada e gráficos. "
                + "Se o usuário for professor, o dashboard é sempre o dele (não informe professorId). "
                + "Administrador deve informar o professorId.";
    }

    @Override
    public Set<Role> rolesPermitidas() { return Set.of(Role.ADMINISTRADOR, Role.PROFESSOR); }

    @Override
    public Map<String, Object> parametros() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "dias", Map.of("type", "integer", "description", "Últimos N dias (1 a 365)"),
                        "professorId", Map.of("type", "integer", "description", "Só para administrador")),
                "required", List.of());
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        Long pedido = ArgumentosTool.longOpcional(argumentos, "professorId");
        Long professorId;
        if (usuario.role() == Role.PROFESSOR) {
            if (pedido != null && !pedido.equals(usuario.usuarioId())) {
                return Map.of("erro", "Permissão negada: o professor só pode ver o próprio dashboard");
            }
            professorId = usuario.usuarioId();
        } else {
            if (pedido == null) {
                return Map.of("erro", "Informe o professorId (use listar_professores_e_salas)");
            }
            professorId = pedido;
        }

        Long informado = ArgumentosTool.longOpcional(argumentos, "dias");
        int dias = (int) Math.max(1, Math.min(informado == null ? 30 : informado, 365));
        RespostaDashProfessoraDTO dash = professorService.respostaDashProfessora(professorId, dias);
        KPIsProfessorDTO kpis = dash.getKpisProfessorDTO();

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("professorId", professorId);
        resposta.put("periodo", "últimos " + dias + " dias");
        resposta.put("alunosAtendidos", kpis.getQtdAlunosAtendidos());
        resposta.put("sessoesRealizadas", kpis.getQtdTotalSessoesRealizadas());
        resposta.put("diaSemanaComMaiorAtendimento", String.valueOf(kpis.getDiaSemanaComMaiorAtendimento()));
        resposta.put("especialidadeMaisRequisitada", kpis.getEspecialidadeMaisRequisitada());
        resposta.put("aulasPorDiaDaSemana", dash.getAgendamentosPorDiasDTO());
        resposta.put("aulasPorEspecialidade", dash.getAulasPorEspecialidadesDTO());
        return resposta;
    }
}
