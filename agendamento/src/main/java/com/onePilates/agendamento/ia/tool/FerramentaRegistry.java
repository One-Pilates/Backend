package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.model.enums.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Reúne as {@link FerramentaIA} registradas no Spring e monta a lista no formato da Groq. */
@Component
public class FerramentaRegistry {

    private final Map<String, FerramentaIA> ferramentas;

    public FerramentaRegistry(List<FerramentaIA> ferramentas) {
        this.ferramentas = ferramentas.stream()
                .collect(Collectors.toMap(FerramentaIA::nome, Function.identity()));
    }

    public Optional<FerramentaIA> buscar(String nome) {
        return Optional.ofNullable(ferramentas.get(nome));
    }

    /** Definições (formato OpenAI/Groq) só das tools que o perfil pode usar. */
    public List<Map<String, Object>> definicoes(Role role) {
        return ferramentas.values().stream()
                .filter(f -> f.rolesPermitidas().contains(role))
                .map(f -> Map.<String, Object>of(
                        "type", "function",
                        "function", Map.of(
                                "name", f.nome(),
                                "description", f.descricao(),
                                "parameters", f.parametros())))
                .toList();
    }
}
