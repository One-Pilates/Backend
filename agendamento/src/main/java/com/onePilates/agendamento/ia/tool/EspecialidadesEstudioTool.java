package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.especialidade.repository.EspecialidadeRepository;
import com.onePilates.agendamento.model.enums.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class EspecialidadesEstudioTool implements FerramentaIA {

    private final EspecialidadeRepository especialidadeRepository;

    public EspecialidadesEstudioTool(EspecialidadeRepository especialidadeRepository) {
        this.especialidadeRepository = especialidadeRepository;
    }

    @Override
    public String nome() { return "especialidades_estudio"; }

    @Override
    public String descricao() {
        return "Lista o nome de cada especialidade do estúdio, os professores que a ministram e as salas em que ocorre. "
                + "Não devolve agendamentos nem dados de alunos.";
    }

    @Override
    public Set<Role> rolesPermitidas() { return Set.of(Role.ALUNO); }

    @Override
    public Map<String, Object> parametros() {
        return Map.of("type", "object", "properties", Map.of());
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        List<Map<String, Object>> especialidades = especialidadeRepository.findAll().stream()
                .map(e -> Map.<String, Object>of(
                        "nome", e.getNome(),
                        "professores", e.getProfessores().stream().map(p -> p.getNome()).toList(),
                        "salas", e.getSalas().stream().map(s -> s.getNome()).toList()))
                .toList();
        return Map.of("especialidades", especialidades);
    }
}
