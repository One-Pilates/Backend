package com.onePilates.agendamento.inicializacao;

import com.onePilates.agendamento.administrador.dto.AdministradorDTO;
import com.onePilates.agendamento.administrador.repository.AdministradorRepository;
import com.onePilates.agendamento.administrador.service.AdministradorService;
import com.onePilates.agendamento.model.Administrador;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
public class IniciarAplicacaoService {

    private final AdministradorService administradorService;
    private final AdministradorRepository administradorRepository;

    @Value("${app.admin.email:}")
    private String emailAdmin;

    @Value("${app.admin.nome:}")
    private String nomeAdmin;

    @Value("${app.admin.cpf:}")
    private String cpfAdmin;

    public IniciarAplicacaoService(
            AdministradorService administradorService,
            AdministradorRepository administradorRepository
    ) {
        this.administradorService = administradorService;
        this.administradorRepository = administradorRepository;
    }

    public String criarAdministradorInicial() {

        List<Administrador> administradores = administradorRepository.findAll();

        if (!administradores.isEmpty()) {

            return """
                    
                    ==========================================
                    ADMINISTRADOR JÁ EXISTE NO SISTEMA
                    NOVO ADMIN NÃO FOI CRIADO
                    ==========================================
                    
                    """;
        }

        if (emailAdmin == null || emailAdmin.isBlank() ||
                nomeAdmin == null || nomeAdmin.isBlank() ||
                cpfAdmin == null || cpfAdmin.isBlank()) {

            return """
                    
                    ==========================================
                    FALHA AO CRIAR ADMINISTRADOR
                    VARIÁVEIS DE AMBIENTE NÃO CONFIGURADAS
                    ==========================================
                    
                    """;
        }

        AdministradorDTO dto = new AdministradorDTO();

        dto.setCargo("ADMINISTRADOR");
        dto.setEmail(emailAdmin);
        dto.setNome(nomeAdmin);
        dto.setSenha(criarSenhaAleatoria());
        dto.setPrimeiro_acesso(true);
        dto.setNotificacaoAtiva(true);
        dto.setStatus(true);

        dto.setCpf(cpfAdmin);

        administradorService.criarAdministrador(dto);

        return """
                
                ==========================================
                ADMINISTRADOR CRIADO COM SUCESSO
                ==========================================
                
                """;
    }

    private String criarSenhaAleatoria() {

        String caracteres =
                "0123456789";

        SecureRandom random = new SecureRandom();

        int tamanhoSenha = 6;

        StringBuilder senha = new StringBuilder();

        for (int i = 0; i < tamanhoSenha; i++) {

            int indice = random.nextInt(caracteres.length());

            senha.append(caracteres.charAt(indice));
        }

        return senha.toString();
    }
}