package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.professor.repository.ProfessorRepository;
import com.onePilates.agendamento.sala.repository.SalaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class ListarProfessoresESalasTool implements FerramentaIA {

    private final ProfessorRepository professorRepository;
    private final SalaRepository salaRepository;

    public ListarProfessoresESalasTool(ProfessorRepository professorRepository, SalaRepository salaRepository) {
        this.professorRepository = professorRepository;
        this.salaRepository = salaRepository;
    }

    @Override
    public String nome() { return "listar_professores_e_salas"; }

    @Override
    public String descricao() { return "Lista professores e salas com id, nome e as especialidades que atendem. Use para descobrir ids e quais combinações são possíveis ao criar ou editar um agendamento."; }

    @Override
    public Map<String, Object> parametros() {
        return Map.of("type", "object", "properties", Map.of());
    }

    /** Lê coleções lazy (especialidades): sem requisição HTTP não há sessão aberta, então precisa de transação. */
    @Override
    @Transactional(readOnly = true)
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        List<Map<String, Object>> professores = professorRepository.findAll().stream()
                .map(p -> Map.<String, Object>of("id", p.getId(), "nome", ArgumentosTool.texto(p.getNome()),
                        "especialidades", p.getEspecialidades().stream()
                                .map(e -> Map.<String, Object>of("id", e.getId(), "nome", ArgumentosTool.texto(e.getNome()))).toList()))
                .toList();
        List<Map<String, Object>> salas = salaRepository.findAll().stream()
                .map(s -> Map.<String, Object>of("id", s.getId(), "nome", ArgumentosTool.texto(s.getNome()),
                        "especialidades", s.getEspecialidades().stream()
                                .map(e -> Map.<String, Object>of("id", e.getId(), "nome", ArgumentosTool.texto(e.getNome()))).toList()))
                .toList();
        return Map.of("professores", professores, "salas", salas);
    }
}
