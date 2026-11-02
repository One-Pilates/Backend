package com.onePilates.agendamento.professor.service;

import com.onePilates.agendamento.agendamento.repository.AgendamentoRepository;
import com.onePilates.agendamento.agendamento.service.AgendamentoService;
import com.onePilates.agendamento.email.service.RabbitMQProducer;
import com.onePilates.agendamento.especialidade.repository.EspecialidadeRepository;
import com.onePilates.agendamento.exception.BusinessException;
import com.onePilates.agendamento.imagem.service.ImagemService;
import com.onePilates.agendamento.model.enums.Role;
import com.onePilates.agendamento.professor.dto.ProfessorDTO;
import com.onePilates.agendamento.professor.repository.ProfessorRepository;
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
class ProfessorServiceTest {

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private EspecialidadeRepository especialidadeRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private AgendamentoService agendamentoService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ImagemService imagemService;

    @Mock
    private RabbitMQProducer rabbitMQ;

    @InjectMocks
    private ProfessorService professorService;

    @Test
    void criar_DeveRejeitarRoleAluno() {
        ProfessorDTO dto = new ProfessorDTO();
        dto.setRole(Role.ALUNO);

        assertThrows(BusinessException.class, () -> professorService.criarProfessor(dto));
        verify(professorRepository, never()).save(any());
    }
}
