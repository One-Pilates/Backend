package com.onePilates.agendamento.secretaria.service;

import com.onePilates.agendamento.agendamento.repository.AgendamentoRepository;
import com.onePilates.agendamento.email.service.RabbitMQProducer;
import com.onePilates.agendamento.endereco.repository.EnderecoRepository;
import com.onePilates.agendamento.exception.BusinessException;
import com.onePilates.agendamento.imagem.service.ImagemService;
import com.onePilates.agendamento.model.enums.Role;
import com.onePilates.agendamento.secretaria.dto.SecretariaDTO;
import com.onePilates.agendamento.secretaria.repository.SecretariaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SecretariaServiceTest {

    @Mock
    private SecretariaRepository secretariaRepository;

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ImagemService imagemService;

    @Mock
    private RabbitMQProducer rabbitMQ;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private SecretariaService secretariaService;

    @Test
    void criar_DeveRejeitarRoleAluno() {
        SecretariaDTO dto = new SecretariaDTO();
        dto.setRole(Role.ALUNO);

        assertThrows(BusinessException.class, () -> secretariaService.criarSecretaria(dto));
        verify(secretariaRepository, never()).save(any());
    }
}
