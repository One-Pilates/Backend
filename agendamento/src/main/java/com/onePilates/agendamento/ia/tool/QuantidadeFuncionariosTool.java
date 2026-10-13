package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.enums.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Quantidade de secretárias e professores do estúdio, só para o administrador. */
@Component
public class QuantidadeFuncionariosTool implements FerramentaIA {

    private final FuncionarioRepository funcionarioRepository;

    public QuantidadeFuncionariosTool(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    @Override
    public String nome() { return "quantidade_funcionarios"; }

    @Override
    public String descricao() {
        return "Quantidade de funcionários do estúdio: secretárias e professores, com total, ativos e inativos de "
                + "cada grupo. Use para perguntas como 'quantos funcionários eu tenho?', 'quantos professores?' ou "
                + "'quantas secretárias?'.";
    }

    @Override
    public Set<Role> rolesPermitidas() { return Set.of(Role.ADMINISTRADOR); }

    @Override
    public Map<String, Object> parametros() {
        return Map.of("type", "object", "properties", Map.of(), "required", List.of());
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        Map<String, Long> secretarias = contar(Role.SECRETARIA);
        Map<String, Long> professores = contar(Role.PROFESSOR);
        return Map.of(
                "totalFuncionarios", secretarias.get("total") + professores.get("total"),
                "totalAtivos", secretarias.get("ativos") + professores.get("ativos"),
                "secretarias", secretarias,
                "professores", professores);
    }

    private Map<String, Long> contar(Role role) {
        long ativos = funcionarioRepository.countByRoleAndStatus(role, true);
        long total = funcionarioRepository.countByRole(role);
        return Map.of("total", total, "ativos", ativos, "inativos", total - ativos);
    }
}
