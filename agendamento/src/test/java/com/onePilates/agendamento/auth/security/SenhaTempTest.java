package com.onePilates.agendamento.auth.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SenhaTempTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SenhaTemp senhaTemp;

    @Test
    void criarSenhaCodificada_DeveChamarPasswordEncoder() {
        String senhaCodificadaMock = "$2a$10$encryptedPasswordHashHere";
        when(passwordEncoder.encode("12345678")).thenReturn(senhaCodificadaMock);

        senhaTemp.criarSenhaCodificada();

        verify(passwordEncoder, times(1)).encode("12345678");
    }

    @Test
    void criarSenhaCodificada_DeveGerarHashValido() {
        String senhaCodificadaMock = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        when(passwordEncoder.encode("12345678")).thenReturn(senhaCodificadaMock);

        senhaTemp.criarSenhaCodificada();

        verify(passwordEncoder).encode(eq("12345678"));
    }

    @Test
    void criarSenhaCodificada_DeveSempreUsarMesmaSenha() {
        senhaTemp.criarSenhaCodificada();

        verify(passwordEncoder).encode("12345678");
    }
}


