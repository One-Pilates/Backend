package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.enums.Role;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Quantidade de alunos (ativos e inativos) e média de idade calculada pela data de nascimento. */
@Component
public class EstatisticasAlunosTool implements FerramentaIA {

    private final AlunoRepository alunoRepository;

    public EstatisticasAlunosTool(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    @Override
    public String nome() { return "estatisticas_alunos"; }

    @Override
    public String descricao() {
        return "Estatísticas dos alunos do estúdio: quantos alunos existem (total, ativos e inativos) e a média de "
                + "idade calculada pela data de nascimento, além da idade do mais novo e do mais velho. Use para "
                + "perguntas como 'quantos alunos temos?' ou 'qual a média de idade dos alunos?'. O cadastro NÃO "
                + "tem gênero/sexo: se perguntarem, diga que essa informação não existe no sistema; nunca deduza "
                + "pelo nome.";
    }

    /** Mesmos perfis que consultam alunos por GET /api/alunos: administração e secretaria. */
    @Override
    public Set<Role> rolesPermitidas() { return Set.of(Role.ADMINISTRADOR, Role.SECRETARIA); }

    @Override
    public Map<String, Object> parametros() {
        return Map.of(
                "type", "object",
                "properties", Map.of("apenasAtivos", Map.of("type", "boolean",
                        "description", "true calcula a idade só com alunos ativos; padrão false (todos)")),
                "required", List.of());
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        boolean apenasAtivos = Boolean.parseBoolean(String.valueOf(argumentos.get("apenasAtivos")));
        List<Aluno> alunos = alunoRepository.findAll();
        long ativos = alunos.stream().filter(a -> Boolean.TRUE.equals(a.getStatus())).count();

        LocalDate hoje = LocalDate.now();
        List<Integer> idades = alunos.stream()
                .filter(a -> !apenasAtivos || Boolean.TRUE.equals(a.getStatus()))
                .map(Aluno::getDataNascimento)
                .filter(d -> d != null && !d.isAfter(hoje))
                .map(d -> Period.between(d, hoje).getYears())
                .toList();

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("totalAlunos", alunos.size());
        resultado.put("alunosAtivos", ativos);
        resultado.put("alunosInativos", alunos.size() - ativos);
        resultado.put("baseDoCalculoDeIdade", apenasAtivos ? "apenas alunos ativos" : "todos os alunos");
        resultado.put("alunosComDataDeNascimento", idades.size());
        if (idades.isEmpty()) {
            resultado.put("observacao", "Nenhum aluno com data de nascimento válida para calcular a idade");
        } else {
            double media = idades.stream().mapToInt(Integer::intValue).average().orElse(0);
            resultado.put("mediaIdade", Math.round(media * 10) / 10.0);
            resultado.put("idadeMinima", idades.stream().mapToInt(Integer::intValue).min().orElse(0));
            resultado.put("idadeMaxima", idades.stream().mapToInt(Integer::intValue).max().orElse(0));
        }
        return resultado;
    }
}
