package com.onePilates.agendamento.endereco.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.config.TestSecurityConfig;
import com.onePilates.agendamento.endereco.dto.EnderecoDTO;
import com.onePilates.agendamento.endereco.service.EnderecoService;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.Endereco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnderecoController.class)
@Import(TestSecurityConfig.class)
class EnderecoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnderecoService enderecoService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private FuncionarioRepository funcionarioRepository;

    private EnderecoDTO dto;
    private Endereco endereco;

    @BeforeEach
    void setUp() {
        dto = new EnderecoDTO();
        dto.setRua("Rua Teste");
        dto.setNumero("123");
        dto.setBairro("Bairro Teste");
        dto.setCidade("Cidade Teste");
        dto.setEstado("Estado Teste");
        dto.setCep("12345678");
        dto.setUf("SP");

        endereco = new Endereco();
        endereco.setId(1L);
        endereco.setRua("Rua Teste");
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void verTodosOsEnderecos_DeveRetornar200() throws Exception {
        List<Endereco> lista = Arrays.asList(endereco);
        when(enderecoService.verTodosOsEnderecos()).thenReturn(lista);

        mockMvc.perform(get("/api/endereco"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(enderecoService).verTodosOsEnderecos();
    }

    @Test
    @WithMockUser(authorities = {"ADMINISTRADOR"})
    void cadastrarEndereco_DeveRetornar201() throws Exception {
        when(enderecoService.cadastrarEndereco(any(EnderecoDTO.class))).thenReturn(endereco);

        mockMvc.perform(post("/api/endereco")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));

        verify(enderecoService).cadastrarEndereco(any(EnderecoDTO.class));
    }
}

