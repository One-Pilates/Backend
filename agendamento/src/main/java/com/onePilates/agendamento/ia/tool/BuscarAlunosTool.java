package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.model.Aluno;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class BuscarAlunosTool implements FerramentaIA {

    private static final int MAX_LISTA = 15;

    private final AlunoRepository alunoRepository;

    public BuscarAlunosTool(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    @Override
    public String nome() { return "buscar_alunos"; }

    @Override
    public String descricao() {
        return "Identifica um aluno pelo nome. O nome informado pode ser parcial (ex.: primeiro e último nome): "
                + "cada palavra dita precisa aparecer no nome completo, sem diferenciar maiúsculas e acentos. "
                + "Retorna o aluno só se houver exatamente um correspondente; se houver nenhum ou mais de um, "
                + "retorna erro e nenhuma ação deve ser feita. Use listar=true apenas quando o usuário quiser "
                + "ver uma lista de alunos.";
    }

    @Override
    public Map<String, Object> parametros() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "nome", Map.of("type", "string", "description", "Nome como o usuário falou, ex.: Guilherme Lima"),
                        "listar", Map.of("type", "boolean", "description", "true para listar todos os correspondentes")),
                "required", List.of("nome"));
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        String consulta = String.valueOf(argumentos.getOrDefault("nome", "")).trim();
        if (consulta.isEmpty()) {
            return Map.of("erro", "Informe o nome do aluno");
        }
        String[] palavras = normalizar(consulta).split("\\s+");

        List<Aluno> correspondentes = alunoRepository.findAll().stream()
                .filter(a -> a.getNome() != null && contemTodas(normalizar(a.getNome()), palavras))
                .toList();

        if (Boolean.parseBoolean(String.valueOf(argumentos.get("listar")))) {
            return Map.of("alunos", correspondentes.stream().limit(MAX_LISTA).map(this::resumo).toList(),
                    "total", correspondentes.size());
        }

        if (correspondentes.isEmpty()) {
            return Map.of("encontrado", false, "erro", "Nenhum aluno encontrado com o nome informado: " + consulta);
        }
        if (correspondentes.size() > 1) {
            // nome idêntico ao digitado desempata; fora isso é ambíguo e nada deve ser feito
            String consultaNormalizada = String.join(" ", palavras);
            List<Aluno> exatos = correspondentes.stream()
                    .filter(a -> normalizar(a.getNome()).equals(consultaNormalizada)).toList();
            if (exatos.size() == 1) {
                return Map.of("encontrado", true, "alunos", List.of(resumo(exatos.get(0))));
            }
            return Map.of("encontrado", false,
                    "erro", "Mais de um aluno corresponde a " + consulta + ". Não faça nenhuma ação: informe ao usuário "
                            + "que não foi possível identificar um único aluno e peça o nome completo.");
        }
        return Map.of("encontrado", true, "alunos", List.of(resumo(correspondentes.get(0))));
    }

    private Map<String, Object> resumo(Aluno a) {
        return Map.of("id", a.getId(), "nome", a.getNome(), "ativo", Boolean.TRUE.equals(a.getStatus()));
    }

    /** Cada palavra buscada deve ser o início de alguma palavra do nome (Lima casa com Lima, não com Palimar). */
    private boolean contemTodas(String nomeNormalizado, String[] palavras) {
        String[] palavrasDoNome = nomeNormalizado.split("\\s+");
        return Arrays.stream(palavras).allMatch(p -> Arrays.stream(palavrasDoNome).anyMatch(n -> n.startsWith(p)));
    }

    private String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }
}
