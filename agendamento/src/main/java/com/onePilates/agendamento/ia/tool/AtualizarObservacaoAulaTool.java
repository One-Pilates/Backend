package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.agendamento.service.AgendamentoService;
import com.onePilates.agendamento.model.enums.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AtualizarObservacaoAulaTool implements FerramentaIA {

    private final AgendamentoService agendamentoService;

    public AtualizarObservacaoAulaTool(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @Override
    public String nome() { return "atualizar_observacao_aula_aluno"; }

    @Override
    public String descricao() {
        return "Registra a observação de um aluno em uma aula específica (substitui a observação daquela aula). "
                + "Só use quando o usuário pedir explicitamente.";
    }

    /** Professor só consulta: alterar observações é de administrador e secretaria. */
    @Override
    public Set<Role> rolesPermitidas() { return Set.of(Role.ADMINISTRADOR, Role.SECRETARIA); }

    @Override
    public Map<String, Object> parametros() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "agendamentoId", Map.of("type", "integer"),
                        "alunoId", Map.of("type", "integer"),
                        "observacao", Map.of("type", "string", "description", "Texto exatamente como o usuário escreveu")),
                "required", List.of("agendamentoId", "alunoId", "observacao"));
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        Long agendamentoId = ArgumentosTool.longObrigatorio(argumentos, "agendamentoId");
        Long alunoId = ArgumentosTool.longObrigatorio(argumentos, "alunoId");
        String observacao = String.valueOf(argumentos.getOrDefault("observacao", "")).trim();
        if (observacao.isEmpty()) {
            return Map.of("erro", "Informe o texto da observação");
        }
        agendamentoService.atualizarObservacaoAluno(agendamentoId, alunoId, observacao);
        return Map.of("sucesso", true, "agendamentoId", agendamentoId, "alunoId", alunoId, "observacaoAtual", observacao);
    }
}
