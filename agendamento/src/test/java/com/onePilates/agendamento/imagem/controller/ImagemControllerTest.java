package com.onePilates.agendamento.imagem.controller;

import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.config.TestSecurityConfig;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.imagem.service.ImagemService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ImagemController.class)
@Import(TestSecurityConfig.class)
class ImagemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private FuncionarioRepository funcionarioRepository;

    @MockBean
    private ImagemService imagemService;

    private Path imagensDir;
    private static final String TEST_DIR = "imagens";

    @BeforeEach
    void setUp() throws IOException {
        imagensDir = Paths.get(TEST_DIR);
        Files.createDirectories(imagensDir);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Limpar arquivos de teste criados
        if (Files.exists(imagensDir)) {
            Files.walk(imagensDir)
                    .sorted((a, b) -> -a.compareTo(b)) // Deletar arquivos antes dos diretórios
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // Ignorar erros ao deletar
                        }
                    });
        }
    }

    @Test
    void servirImagem_DeveRetornar404_QuandoArquivoNaoExiste() throws Exception {
        mockMvc.perform(get("/api/imagens/arquivo_inexistente.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    void servirImagem_DeveRetornar400_QuandoCaminhoInvalido() throws Exception {
        // Caminhos com .. são rejeitados pelo Spring por segurança, retornando 400
        mockMvc.perform(get("/api/imagens/../etc/passwd"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void servirImagem_DeveRetornar404_QuandoArquivoNaoExisteNoDiretorioEsperado() throws Exception {
        // O controller espera arquivos no diretório "imagens/", não em caminhos absolutos
        mockMvc.perform(get("/api/imagens/test_image.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    void servirImagem_DeveRetornar200_QuandoArquivoJpgExiste() throws Exception {
        File testFile = imagensDir.resolve("test_jpg_controller.jpg").toFile();
        testFile.createNewFile();
        
        mockMvc.perform(get("/api/imagens/imagens/test_jpg_controller.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    void servirImagem_DeveRetornar200_QuandoArquivoJpegExiste() throws Exception {
        File testFile = imagensDir.resolve("test_jpeg_controller.jpeg").toFile();
        testFile.createNewFile();
        
        mockMvc.perform(get("/api/imagens/imagens/test_jpeg_controller.jpeg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    void servirImagem_DeveRetornar200_QuandoArquivoPngExiste() throws Exception {
        File testFile = imagensDir.resolve("test_png_controller.png").toFile();
        testFile.createNewFile();
        
        mockMvc.perform(get("/api/imagens/imagens/test_png_controller.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    void servirImagem_DeveRetornar200_QuandoArquivoGifExiste() throws Exception {
        File testFile = imagensDir.resolve("test_gif_controller.gif").toFile();
        testFile.createNewFile();
        
        mockMvc.perform(get("/api/imagens/imagens/test_gif_controller.gif"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_GIF));
    }

    @Test
    void servirImagem_DeveRetornar200_QuandoArquivoWebpExiste() throws Exception {
        File testFile = imagensDir.resolve("test_webp_controller.webp").toFile();
        testFile.createNewFile();
        
        mockMvc.perform(get("/api/imagens/imagens/test_webp_controller.webp"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/webp"));
    }

    @Test
    void servirImagem_DeveRetornar404_QuandoArquivoEhDiretorio() throws Exception {
        Path subDir = Files.createDirectories(imagensDir.resolve("subdiretorio_controller"));
        
        mockMvc.perform(get("/api/imagens/imagens/subdiretorio_controller"))
                .andExpect(status().isNotFound());
    }

    @Test
    void servirImagem_DeveRetornar200_QuandoArquivoSemPrefixoImagens() throws Exception {
        File testFile = imagensDir.resolve("test_sem_prefixo.jpg").toFile();
        testFile.createNewFile();
        
        // Testar caminho sem prefixo "imagens/" - o controller adiciona automaticamente
        mockMvc.perform(get("/api/imagens/test_sem_prefixo.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    void servirImagem_DeveRetornar200_QuandoArquivoComExtensaoDesconhecida() throws Exception {
        File testFile = imagensDir.resolve("test_unknown_controller.bmp").toFile();
        testFile.createNewFile();
        
        mockMvc.perform(get("/api/imagens/imagens/test_unknown_controller.bmp"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/octet-stream"));
    }

    @Test
    void servirImagem_DeveRetornar200_QuandoArquivoComExtensaoMaiuscula() throws Exception {
        File testFile = imagensDir.resolve("test_UPPERCASE.JPG").toFile();
        testFile.createNewFile();
        
        mockMvc.perform(get("/api/imagens/imagens/test_UPPERCASE.JPG"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    void servirImagem_DeveRetornar200_QuandoArquivoComNomeComplexo() throws Exception {
        File testFile = imagensDir.resolve("professor_123_1234567890.png").toFile();
        testFile.createNewFile();
        
        mockMvc.perform(get("/api/imagens/imagens/professor_123_1234567890.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"professor_123_1234567890.png\""));
    }

    @Test
    void servirImagem_DeveRetornar200_QuandoArquivoComExtensaoJPEGMaiuscula() throws Exception {
        File testFile = imagensDir.resolve("test_JPEG.JPEG").toFile();
        testFile.createNewFile();
        
        mockMvc.perform(get("/api/imagens/imagens/test_JPEG.JPEG"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    void servirImagem_DeveRetornar200_QuandoArquivoComExtensaoPNGMaiuscula() throws Exception {
        File testFile = imagensDir.resolve("test_PNG.PNG").toFile();
        testFile.createNewFile();
        
        mockMvc.perform(get("/api/imagens/imagens/test_PNG.PNG"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    void servirImagem_DeveRetornar404_QuandoCaminhoVazio() throws Exception {
        mockMvc.perform(get("/api/imagens/"))
                .andExpect(status().isNotFound());
    }

    @Test
    void servirImagem_DeveRetornar404_QuandoNaoEhArquivo() throws Exception {
        mockMvc.perform(get("/api/imagens/imagens/"))
                .andExpect(status().isNotFound());
    }

    @Test
    void servirImagem_DeveRetornar200_QuandoArquivoComExtensaoGifMaiuscula() throws Exception {
        File testFile = imagensDir.resolve("test_GIF.GIF").toFile();
        testFile.createNewFile();
        
        mockMvc.perform(get("/api/imagens/imagens/test_GIF.GIF"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_GIF));
    }

    @Test
    void servirImagem_DeveRetornar200_QuandoArquivoComExtensaoWebpMaiuscula() throws Exception {
        File testFile = imagensDir.resolve("test_WEBP.WEBP").toFile();
        testFile.createNewFile();
        
        mockMvc.perform(get("/api/imagens/imagens/test_WEBP.WEBP"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/webp"));
    }

    @Test
    void servirImagem_DeveRetornar200_QuandoArquivoComExtensaoMista() throws Exception {
        File testFile = imagensDir.resolve("test_mixed.JpG").toFile();
        testFile.createNewFile();
        
        mockMvc.perform(get("/api/imagens/imagens/test_mixed.JpG"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }
}

