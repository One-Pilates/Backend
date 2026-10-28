package com.onePilates.agendamento.auth.controller;

import com.onePilates.agendamento.auth.dto.CriarCodigoValidacaoDTO;
import com.onePilates.agendamento.auth.dto.LoginDTO;
import com.onePilates.agendamento.auth.dto.LoginResponseDTO;
import com.onePilates.agendamento.auth.dto.NovaSenhaDTO;
import com.onePilates.agendamento.auth.dto.NovaSenhaResponseDTO;
import com.onePilates.agendamento.auth.dto.ValidarCodigoVerificacaoDTO;
import com.onePilates.agendamento.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO request) {
        LoginResponseDTO response = authService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/criarCodigoVerificacao")
    public ResponseEntity<String> validacaoECriacaoDoCodigoDeVerificacao(@Valid @RequestBody CriarCodigoValidacaoDTO dto) {
        return ResponseEntity.ok(authService.criarCodigoVerificacao(dto.getEmail()));
    }

    @PostMapping("/validarCodigo")
    public ResponseEntity<Boolean> validarCodigo(@Valid @RequestBody ValidarCodigoVerificacaoDTO dto) {
        return ResponseEntity.ok(authService.validarCodigoVerificacao(dto.getEmail(), dto.getCodigo()));
    }

    @PostMapping("/alterarSenha")
    public ResponseEntity<NovaSenhaResponseDTO> trocarSenha(@Valid @RequestBody NovaSenhaDTO dto){
        return ResponseEntity.ok(authService.novaSenha(dto.getSenha(),dto.getEmail()));
    }


}
