package com.onePilates.agendamento.config;

import com.onePilates.agendamento.auth.security.JwtAuthFilter;
import com.onePilates.agendamento.auth.security.JwtUtil;
import com.onePilates.agendamento.funcionario.repository.FuncionarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Test
    void passwordEncoder_DeveRetornarBCryptPasswordEncoder() {
        SecurityConfig config = new SecurityConfig(jwtUtil, funcionarioRepository);
        PasswordEncoder encoder = config.passwordEncoder();

        assertNotNull(encoder);
        assertInstanceOf(org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.class, encoder);
    }

    @Test
    void jwtAuthFilter_DeveRetornarJwtAuthFilter() {
        SecurityConfig config = new SecurityConfig(jwtUtil, funcionarioRepository);
        JwtAuthFilter filter = config.jwtAuthFilter();

        assertNotNull(filter);
        assertInstanceOf(JwtAuthFilter.class, filter);
    }

    @Test
    void corsConfigurationSource_DeveRetornarCorsConfigurationSource() {
        SecurityConfig config = new SecurityConfig(jwtUtil, funcionarioRepository);
        CorsConfigurationSource corsSource = config.corsConfigurationSource();

        assertNotNull(corsSource);
        assertInstanceOf(CorsConfigurationSource.class, corsSource);
    }

    @Test
    void securityFilterChain_DeveRetornarSecurityFilterChain() throws Exception {
        SecurityConfig config = new SecurityConfig(jwtUtil, funcionarioRepository);
        
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_SELF);
        
        DefaultSecurityFilterChain expectedChain = new DefaultSecurityFilterChain(
            AnyRequestMatcher.INSTANCE, 
            java.util.Collections.emptyList()
        );
        
        when(http.build()).thenReturn(expectedChain);

        SecurityFilterChain chain = config.securityFilterChain(http);

        assertNotNull(chain);
        assertEquals(expectedChain, chain);
        verify(http).cors(any());
        verify(http).csrf(any());
        verify(http).sessionManagement(any());
        verify(http).authorizeHttpRequests(any());
        verify(http).addFilterBefore(any(), any());
        verify(http).build();
    }
}

