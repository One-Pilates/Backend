package com.onePilates.agendamento.ia.service;

import com.onePilates.agendamento.exception.OperacaoInvalidaException;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.ia.client.GroqClient;
import com.onePilates.agendamento.ia.dto.IASolicitacaoDTO;
import com.onePilates.agendamento.ia.model.IAConversa;
import com.onePilates.agendamento.ia.model.IARealizacao;
import com.onePilates.agendamento.ia.model.StatusRealizacao;
import com.onePilates.agendamento.ia.repository.IAAcaoRepository;
import com.onePilates.agendamento.ia.repository.IAConversaRepository;
import com.onePilates.agendamento.ia.repository.IARealizacaoRepository;
import com.onePilates.agendamento.model.Secretaria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class IAServiceAudioTest {

    private FuncionarioRepository funcionarioRepository;
    private IARealizacaoRepository realizacaoRepository;
    private IAAcaoRepository acaoRepository;
    private IAConversaRepository conversaRepository;
    private ApplicationEventPublisher eventos;
    private AtomicReference<String> respostaTranscricao;
    private AtomicInteger salvamentosRealizacao;
    private IAService iaService;

    @BeforeEach
    void setUp() {
        respostaTranscricao = new AtomicReference<>("Quais aulas temos amanhã?");
        salvamentosRealizacao = new AtomicInteger(0);

        Secretaria secretaria = new Secretaria();
        secretaria.setId(10L);
        secretaria.setEmail("sec@onepilates.com");

        funcionarioRepository = (FuncionarioRepository) Proxy.newProxyInstance(
                FuncionarioRepository.class.getClassLoader(),
                new Class<?>[]{FuncionarioRepository.class},
                (proxy, method, args) -> {
                    if ("findByEmail".equals(method.getName()) && "sec@onepilates.com".equals(args[0])) {
                        return Optional.of(secretaria);
                    }
                    return Optional.empty();
                }
        );

        realizacaoRepository = (IARealizacaoRepository) Proxy.newProxyInstance(
                IARealizacaoRepository.class.getClassLoader(),
                new Class<?>[]{IARealizacaoRepository.class},
                (proxy, method, args) -> {
                    if ("findByUsuarioIdAndStatusIn".equals(method.getName())) {
                        return List.of();
                    }
                    if ("save".equals(method.getName())) {
                        salvamentosRealizacao.incrementAndGet();
                        return args[0];
                    }
                    return List.of();
                }
        );

        acaoRepository = (IAAcaoRepository) Proxy.newProxyInstance(
                IAAcaoRepository.class.getClassLoader(),
                new Class<?>[]{IAAcaoRepository.class},
                (proxy, method, args) -> List.of()
        );

        conversaRepository = (IAConversaRepository) Proxy.newProxyInstance(
                IAConversaRepository.class.getClassLoader(),
                new Class<?>[]{IAConversaRepository.class},
                (proxy, method, args) -> {
                    if ("findTopByUsuarioIdOrderByUltimoUsoDesc".equals(method.getName())) {
                        return Optional.empty();
                    }
                    if ("save".equals(method.getName())) {
                        return new IAConversa(10L);
                    }
                    return Optional.empty();
                }
        );

        eventos = event -> {};

        GroqClient groqStub = new GroqClient() {
            @Override
            public String transcreverAudio(byte[] audioBytes, String nomeOriginal, String contentType) {
                return respostaTranscricao.get();
            }
        };

        ConversaService conversaService = new ConversaService(conversaRepository, realizacaoRepository);
        iaService = new IAService(
                funcionarioRepository,
                realizacaoRepository,
                acaoRepository,
                conversaService,
                eventos,
                groqStub
        );
    }

    @Test
    void deveTranscreverAudioEEnfileirarPedido() {
        MockMultipartFile audioFile = new MockMultipartFile(
                "audio",
                "gravacao.m4a",
                "audio/m4a",
                "conteudo-audio-falso".getBytes(StandardCharsets.UTF_8)
        );

        IASolicitacaoDTO resultado = iaService.solicitarPorAudio(10L, audioFile, false, "sec@onepilates.com");

        assertNotNull(resultado);
        assertEquals(StatusRealizacao.PENDENTE, resultado.status());
        assertEquals("Quais aulas temos amanhã?", resultado.textoTranscrito());
        assertEquals(1, salvamentosRealizacao.get());
    }

    @Test
    void deveRecusarQuandoTranscricaoRetornarVazia() {
        respostaTranscricao.set("   ");

        MockMultipartFile audioFile = new MockMultipartFile(
                "audio",
                "gravacao.m4a",
                "audio/m4a",
                "silencio".getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(OperacaoInvalidaException.class, () ->
                iaService.solicitarPorAudio(10L, audioFile, false, "sec@onepilates.com")
        );
        assertEquals(0, salvamentosRealizacao.get());
    }
}
