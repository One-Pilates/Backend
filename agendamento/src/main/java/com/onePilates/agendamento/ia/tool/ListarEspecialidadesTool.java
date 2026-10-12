package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.especialidade.repository.EspecialidadeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ListarEspecialidadesTool implements FerramentaIA {

    private final EspecialidadeRepository especialidadeRepository;

    public ListarEspecialidadesTool(EspecialidadeRepository especialidadeRepository) {
        this.especialidadeRepository = especialidadeRepository;
    }

    @Override
    public String nome() { return "listar_especialidades"; }

    @Override
    public String descricao() { return "Lista as especialidades (modalidades de aula) cadastradas no estúdio."; }

    @Override
    public Map<String, Object> parametros() {
        return Map.of("type", "object", "properties", Map.of());
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        List<Map<String, Object>> lista = especialidadeRepository.findAll().stream()
                .map(e -> Map.<String, Object>of("id", e.getId(), "nome", e.getNome()))
                .toList();
        return Map.of("especialidades", lista);
    }
}
