package com.onePilates.agendamento;

import com.onePilates.agendamento.inicializacao.IniciarAplicacaoService;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.File;

@SpringBootApplication
public class AgendamentoApplication {

    private static final Logger log =
            LoggerFactory.getLogger(AgendamentoApplication.class);

    private static final Dotenv DOTENV = carregarDotenv();

        // Aceita execução a partir da pasta do serviço, de Backend ou da raiz do repositório.
    private static Dotenv carregarDotenv() {
                String[] diretorios = {"./", "./agendamento", "./Backend/agendamento"};

                String diretorio = "./Backend/agendamento";
                for (String candidato : diretorios) {
                        if (new File(candidato, ".env").exists()) {
                                diretorio = candidato;
                                break;
                        }
                }

        return Dotenv.configure()
                .directory(diretorio)
                .ignoreIfMissing()
                .load();
    }

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