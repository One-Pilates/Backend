package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.aluno.dto.AlunoDTO;
import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.aluno.service.AlunoService;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.enums.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AtualizarObservacaoAlunoTool implements FerramentaIA {

    private final AlunoRepository alunoRepository;
    private final AlunoService alunoService;

    public AtualizarObservacaoAlunoTool(AlunoRepository alunoRepository, AlunoService alunoService) {
        this.alunoRepository = alunoRepository;
        this.alunoService = alunoService;
    }

    @Override
    public String nome() { return "atualizar_observacao_aluno"; }

    @Override
    public String descricao() {
        return "Altera a observação GERAL do aluno. Só use quando o usuário pedir explicitamente. "
                + "modo acrescentar (padrão) adiciona ao texto existente; substituir troca tudo.";
    }

    /** Professor só consulta: alterar observações é de administrador e secretaria. */
    @Override
    public Set<Role> rolesPermitidas() { return Set.of(Role.ADMINISTRADOR, Role.SECRETARIA); }

    @Override
    public Map<String, Object> parametros() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "alunoId", Map.of("type", "integer"),
                        "observacao", Map.of("type", "string", "description", "Texto exatamente como o usuário escreveu"),
                        "modo", Map.of("type", "string", "enum", List.of("acrescentar", "substituir"))),
                "required", List.of("alunoId", "observacao"));
    }

    @Override
    public Object executar(Map<String, Object> argumentos, ContextoUsuario usuario) {
        Long alunoId = ArgumentosTool.longObrigatorio(argumentos, "alunoId");
        String nova = String.valueOf(argumentos.getOrDefault("observacao", "")).trim();
        if (nova.isEmpty()) {
            return Map.of("erro", "Informe o texto da observação");
        }
        Aluno aluno = alunoRepository.findById(alunoId).orElse(null);
        if (aluno == null) {
            return Map.of("erro", "Aluno não encontrado");
        }
        String antes = aluno.getObservacao() == null ? "" : aluno.getObservacao();
        boolean substituir = "substituir".equalsIgnoreCase(String.valueOf(argumentos.get("modo")));
        String depois = substituir || antes.isBlank() ? nova : antes + "\n" + nova;

        AlunoDTO dto = new AlunoDTO();
        dto.setObservacao(depois);
        alunoService.atualizarAluno(alunoId, dto);
        return Map.of("sucesso", true, "aluno", aluno.getNome(), "observacaoAnterior", antes, "observacaoAtual", depois);
    }
}
