package com.onePilates.agendamento.auth.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SenhaTemp {
    private final PasswordEncoder passwordEncoder;

    public SenhaTemp(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void criarSenhaCodificada() {
        String senhaCodificada = passwordEncoder.encode("12345678");

    }
}
