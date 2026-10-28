package com.onePilates.agendamento.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onePilates.agendamento.auth.dto.CriarCodigoValidacaoDTO;
import com.onePilates.agendamento.auth.dto.LoginDTO;
import com.onePilates.agendamento.auth.dto.LoginResponseDTO;
import com.onePilates.agendamento.auth.dto.NovaSenhaDTO;
import com.onePilates.agendamento.auth.dto.NovaSenhaResponseDTO;
import com.onePilates.agendamento.auth.dto.ValidarCodigoVerificacaoDTO;
import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.auth.service.AuthService;
import com.onePilates.agendamento.config.TestSecurityConfig;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private FuncionarioRepository funcionarioRepository;

    private LoginDTO loginDTO;
    private LoginResponseDTO loginResponseDTO;

    @BeforeEach
    void setUp() {
        loginDTO = new LoginDTO();
        loginDTO.setEmail("teste@teste.com");
        loginDTO.setSenha("senha123");

        loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setToken("token123");
        loginResponseDTO.setRole("PROFESSOR");
    }

    @Test
    void login_DeveRetornar200_QuandoCredenciaisValidas() throws Exception {
        when(authService.authenticate(any(LoginDTO.class))).thenReturn(loginResponseDTO);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.role").value("PROFESSOR"));

        verify(authService).authenticate(any(LoginDTO.class));
    }

    @Test
    void validacaoECriacaoDoCodigoDeVerificacao_DeveRetornar200() throws Exception {
        CriarCodigoValidacaoDTO dto = new CriarCodigoValidacaoDTO();
        dto.setEmail("teste@teste.com");

        when(authService.criarCodigoVerificacao("teste@teste.com")).thenReturn("Código enviado");

        mockMvc.perform(post("/auth/criarCodigoVerificacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(authService).criarCodigoVerificacao("teste@teste.com");
    }

    @Test
    void validarCodigo_DeveRetornar200() throws Exception {
        ValidarCodigoVerificacaoDTO dto = new ValidarCodigoVerificacaoDTO();
        dto.setEmail("teste@teste.com");
        dto.setCodigo("12345");

        when(authService.validarCodigoVerificacao("teste@teste.com", "12345")).thenReturn(true);

        mockMvc.perform(post("/auth/validarCodigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(authService).validarCodigoVerificacao("teste@teste.com", "12345");
    }

    @Test
    void trocarSenha_DeveRetornar200() throws Exception {
        NovaSenhaDTO dto = new NovaSenhaDTO();
        dto.setEmail("teste@teste.com");
        dto.setSenha("novaSenha123");

        NovaSenhaResponseDTO responseDTO = new NovaSenhaResponseDTO();
        responseDTO.setMensagem("Senha alterada com sucesso");

        when(authService.novaSenha("novaSenha123", "teste@teste.com")).thenReturn(responseDTO);

        mockMvc.perform(post("/auth/alterarSenha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Senha alterada com sucesso"));

        verify(authService).novaSenha("novaSenha123", "teste@teste.com");
    }
}

