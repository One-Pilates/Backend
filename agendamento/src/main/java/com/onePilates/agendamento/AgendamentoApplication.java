package com.onePilates.agendamento;

import com.onePilates.agendamento.inicializacao.IniciarAplicacaoService;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class AgendamentoApplication {

    private static final Logger log =
            LoggerFactory.getLogger(AgendamentoApplication.class);

    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    public static void main(String[] args) {

        carregarVariaveisDoEnv();

        // Inicializa a aplicação Spring
        ApplicationContext context =
                SpringApplication.run(AgendamentoApplication.class, args);

        // Inicializa administrador padrão
        IniciarAplicacaoService iniciarAplicacaoService =
                context.getBean(IniciarAplicacaoService.class);

        String resultadoAdministrador =
                iniciarAplicacaoService.criarAdministradorInicial();

        // Log do administrador
        log.info(resultadoAdministrador);

        // Log de inicialização
        log.info("""

                ██████╗ ███╗   ██╗███████╗██████╗ ██╗██╗      █████╗ ████████╗███████╗███████╗
                ██╔═══██╗████╗  ██║██╔════╝██╔══██╗██║██║     ██╔══██╗╚══██╔══╝██╔════╝██╔════╝
                ██║   ██║██╔██╗ ██║█████╗  ██████╔╝██║██║     ███████║   ██║   █████╗  ███████╗
                ██║   ██║██║╚██╗██║██╔══╝  ██╔═══╝ ██║██║     ██╔══██║   ██║   ██╔══╝  ╚════██║
                ╚██████╔╝██║ ╚████║███████╗██║     ██║███████╗██║  ██║   ██║   ███████╗███████║
                 ╚═════╝ ╚═╝  ╚═══╝╚══════╝╚═╝     ╚═╝╚══════╝╚═╝  ╚═╝   ╚═╝   ╚══════╝╚══════╝

                           🚀 APLICAÇÃO RODANDO COM SUCESSO 🚀

                """);
    }

    private static void carregarVariaveisDoEnv() {

        for (DotenvEntry entry : DOTENV.entries()) {

            System.getProperties().putIfAbsent(
                    entry.getKey(),
                    entry.getValue()
            );
        }
    }
}