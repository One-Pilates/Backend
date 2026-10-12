package com.onePilates.agendamento.ia.service;

import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.ia.client.GroqClient;
import com.onePilates.agendamento.ia.dto.IARequestDTO;
import com.onePilates.agendamento.ia.dto.IAResponseDTO;
import com.onePilates.agendamento.ia.tool.ContextoUsuario;
import com.onePilates.agendamento.model.Funcionario;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Ponto de entrada: valida o usuário, melhora o prompt e repassa ao {@link AgentService}. */
@Service
public class IAService {

    private static final String PROMPT_MELHORAR =
            "Reescreva o pedido do usuário de forma clara e objetiva, sem mudar o sentido "
                    + "e sem inventar informações. Mantenha o TIPO do pedido: plano de aula (conteúdo e exercícios) "
                    + "não é agendamento (marcar data e horário); só use as palavras agendar, marcar ou criar aula "
                    + "se o usuário as disse. Mantenha nomes e textos exatamente como escritos. "
                    + "Responda somente com o texto reescrito.";

    private final JwtUtil jwtUtil;
    private final FuncionarioRepository funcionarioRepository;
    private final GroqClient groqClient;
    private final AgentService agentService;

    public IAService(JwtUtil jwtUtil, FuncionarioRepository funcionarioRepository,
                     GroqClient groqClient, AgentService agentService) {
        this.jwtUtil = jwtUtil;
        this.funcionarioRepository = funcionarioRepository;
        this.groqClient = groqClient;
        this.agentService = agentService;
    }

    public IAResponseDTO processar(IARequestDTO dto) {
        ContextoUsuario usuario = validarUsuario(dto);
        String promptMelhorado = melhorarPrompt(dto.mensagem());
        String resposta = agentService.executar(usuario, dto.mensagem(), promptMelhorado);
        return new IAResponseDTO(dto.usuarioId(), dto.mensagem(), promptMelhorado, resposta, LocalDateTime.now());
    }

    /** Garante que o JWT é válido e pertence ao usuarioId informado; devolve quem está pedindo. */
    private ContextoUsuario validarUsuario(IARequestDTO dto) {
        String email;
        try {
            email = jwtUtil.extractEmail(dto.jwt());
        } catch (Exception e) {
            throw new IllegalArgumentException("JWT inválido ou expirado");
        }
        Funcionario funcionario = funcionarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário do token não encontrado"));
        if (!funcionario.getId().equals(dto.usuarioId())) {
            throw new IllegalArgumentException("O usuarioId não corresponde ao dono do JWT");
        }
        return new ContextoUsuario(funcionario.getId(), funcionario.getNome(), funcionario.getRole());
    }

    private String melhorarPrompt(String mensagem) {
        return groqClient.chat(List.of(
                Map.of("role", "system", "content", PROMPT_MELHORAR),
                Map.of("role", "user", "content", mensagem)
        ), 0.2, 400);
    }
}
