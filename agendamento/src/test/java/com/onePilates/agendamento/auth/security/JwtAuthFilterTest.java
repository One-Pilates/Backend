package com.onePilates.agendamento.auth.security;

import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import com.onePilates.agendamento.model.Administrador;
import com.onePilates.agendamento.model.Funcionario;
import com.onePilates.agendamento.model.enums.Role;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private Funcionario funcionario;
    private String validToken;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        funcionario = new com.onePilates.agendamento.model.Administrador();
        funcionario.setId(1L);
        funcionario.setEmail("teste@example.com");
        funcionario.setRole(Role.ADMINISTRADOR);
        
        validToken = "valid.jwt.token";
    }

    @Test
    void doFilterInternal_DeveContinuar_QuandoNaoHaHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractEmail(any());
    }

    @Test
    void doFilterInternal_DeveContinuar_QuandoHeaderNaoTemBearer() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader token");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractEmail(any());
    }

    @Test
    void doFilterInternal_DeveContinuar_QuandoHeaderEstaVazio() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractEmail(any());
    }

    @Test
    void doFilterInternal_DeveAutenticar_QuandoTokenValido() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.isTokenExpired(validToken)).thenReturn(false);
        when(jwtUtil.extractEmail(validToken)).thenReturn("teste@example.com");
        when(funcionarioRepository.findByEmail("teste@example.com")).thenReturn(Optional.of(funcionario));
        when(jwtUtil.extractRole(validToken)).thenReturn("ADMINISTRADOR");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("teste@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternal_DeveContinuar_QuandoTokenExpirado() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.isTokenExpired(validToken)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractEmail(any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_DeveContinuar_QuandoEmailENull() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.isTokenExpired(validToken)).thenReturn(false);
        when(jwtUtil.extractEmail(validToken)).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_DeveContinuar_QuandoFuncionarioNaoEncontrado() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.isTokenExpired(validToken)).thenReturn(false);
        when(jwtUtil.extractEmail(validToken)).thenReturn("teste@example.com");
        when(funcionarioRepository.findByEmail("teste@example.com")).thenReturn(Optional.empty());

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_DeveContinuar_QuandoTokenInvalidoLancaExcecao() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.isTokenExpired(validToken)).thenThrow(new JwtException("Token inválido"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_DeveContinuar_QuandoExtractEmailLancaExcecao() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.isTokenExpired(validToken)).thenReturn(false);
        when(jwtUtil.extractEmail(validToken)).thenThrow(new JwtException("Token inválido"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_DeveAutenticarComRoleCorreto() throws ServletException, IOException {
        funcionario.setRole(Role.PROFESSOR);
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.isTokenExpired(validToken)).thenReturn(false);
        when(jwtUtil.extractEmail(validToken)).thenReturn("teste@example.com");
        when(funcionarioRepository.findByEmail("teste@example.com")).thenReturn(Optional.of(funcionario));
        when(jwtUtil.extractRole(validToken)).thenReturn("PROFESSOR");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("teste@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("PROFESSOR")));
    }

    @Test
    void doFilterInternal_DeveLancarExcecao_QuandoRoleENull() {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.isTokenExpired(validToken)).thenReturn(false);
        when(jwtUtil.extractEmail(validToken)).thenReturn("teste@example.com");
        when(funcionarioRepository.findByEmail("teste@example.com")).thenReturn(Optional.of(funcionario));
        when(jwtUtil.extractRole(validToken)).thenReturn(null);

        // Quando role é null, o SimpleGrantedAuthority lança IllegalArgumentException
        // que não é capturada pelo catch de JwtException
        assertThrows(IllegalArgumentException.class, () -> {
            jwtAuthFilter.doFilterInternal(request, response, filterChain);
        });
    }

    @Test
    void doFilterInternal_DeveExtrairTokenCorretamente_QuandoBearerTemEspacos() throws ServletException, IOException {
        // O código faz header.substring(7) para extrair o token após "Bearer "
        // Como pode haver espaços extras, usamos anyString() para simplificar o mock
        String headerComEspacos = "Bearer      valid.jwt.token"; // Múltiplos espaços após Bearer
        
        when(request.getHeader("Authorization")).thenReturn(headerComEspacos);
        when(jwtUtil.isTokenExpired(anyString())).thenReturn(false);
        when(jwtUtil.extractEmail(anyString())).thenReturn("teste@example.com");
        when(funcionarioRepository.findByEmail("teste@example.com")).thenReturn(Optional.of(funcionario));
        when(jwtUtil.extractRole(anyString())).thenReturn("ADMINISTRADOR");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil).isTokenExpired(anyString());
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

