package com.onePilates.agendamento.agendamento.validator;

import com.onePilates.agendamento.agendamento.dto.AgendamentoDTO;
import com.onePilates.agendamento.agendamento.repository.AgendamentoRepository;
import com.onePilates.agendamento.aluno.repository.AlunoRepository;
import com.onePilates.agendamento.ausencia.repository.AusenciaRepository;
import com.onePilates.agendamento.especialidade.repository.EspecialidadeRepository;
import com.onePilates.agendamento.exception.AlunoInativoException;
import com.onePilates.agendamento.exception.ConflitoHorarioException;
import com.onePilates.agendamento.exception.EntidadeNaoEncontradaException;
import com.onePilates.agendamento.exception.EquipamentoPCDInsuficienteException;
import com.onePilates.agendamento.exception.EspecialidadeIncompativelException;
import com.onePilates.agendamento.exception.OperacaoInvalidaException;
import com.onePilates.agendamento.exception.ProfessorAusenteException;
import com.onePilates.agendamento.exception.ProfessorInativoException;
import com.onePilates.agendamento.exception.SalaLotadaException;
import com.onePilates.agendamento.model.Agendamento;
import com.onePilates.agendamento.model.Aluno;
import com.onePilates.agendamento.model.Ausencia;
import com.onePilates.agendamento.model.Especialidade;
import com.onePilates.agendamento.model.Professor;
import com.onePilates.agendamento.model.Sala;
import com.onePilates.agendamento.model.enums.DiaSemana;
import com.onePilates.agendamento.professor.repository.ProfessorRepository;
import com.onePilates.agendamento.sala.repository.SalaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoValidatorTest {

    @Mock
    private SalaRepository salaRepository;

    @Mock
    private ProfessorRepository professorRepository;

    @Mock
    private EspecialidadeRepository especialidadeRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private AusenciaRepository ausenciaRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private AgendamentoValidator validator;

    private AgendamentoDTO dto;
    private Sala sala;
    private Professor professor;
    private Especialidade especialidade;
    private List<Aluno> alunos;

    @BeforeEach
    void setUp() {
        dto = new AgendamentoDTO();
        // Usar horário fixo válido (14:00) para evitar problemas com validação de horário
        LocalDateTime dataHoraValida = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).withSecond(0).withNano(0);
        dto.setDataHora(dataHoraValida);
        dto.setSalaId(1L);
        dto.setProfessorId(1L);
        dto.setEspecialidadeId(1L);
        dto.setAlunoIds(Set.of(1L, 2L));

        sala = new Sala();
        sala.setId(1L);
        sala.setNome("Sala 1");
        sala.setQuantidadeMaximaAlunos(5);
        sala.setQuantidadeEquipamentosPCD(2);
        sala.setEspecialidades(Set.of(new Especialidade()));

        professor = new Professor();
        professor.setId(1L);
        professor.setNome("Professor Teste");
        professor.setStatus(true);
        professor.setEspecialidades(Set.of(new Especialidade()));

        especialidade = new Especialidade();
        especialidade.setId(1L);
        especialidade.setNome("Pilates Clássico");

        Aluno aluno1 = new Aluno();
        aluno1.setId(1L);
        aluno1.setNome("Aluno 1");
        aluno1.setStatus(true);
        aluno1.setAlunoComLimitacoesFisicas(false);

        Aluno aluno2 = new Aluno();
        aluno2.setId(2L);
        aluno2.setNome("Aluno 2");
        aluno2.setStatus(true);
        aluno2.setAlunoComLimitacoesFisicas(false);

        alunos = new ArrayList<>(Arrays.asList(aluno1, aluno2));
    }

    @Test
    void validar_DevePassar_QuandoTodosOsDadosSaoValidos() {
        // Configurar especialidades para validação
        Especialidade esp = new Especialidade();
        esp.setId(1L);
        esp.setNome("Pilates Clássico");
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        when(ausenciaRepository.findByProfessorId(any())).thenReturn(Collections.emptyList());
        when(agendamentoRepository.existsBySalaIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(false);
        when(agendamentoRepository.existsByProfessorIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(false);
        when(agendamentoRepository.findAgendamentosByAlunoAndDataHoraExcludingId(any(), any(), any())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarSalaLotadaException_QuandoQuantidadeAlunosExcedeCapacidade() {
        sala.setQuantidadeMaximaAlunos(1);
        dto.setAlunoIds(Set.of(1L, 2L, 3L));
        
        // Adicionar terceiro aluno à lista
        Aluno aluno3 = new Aluno();
        aluno3.setId(3L);
        aluno3.setNome("Aluno 3");
        aluno3.setStatus(true);
        aluno3.setAlunoComLimitacoesFisicas(false);
        alunos.add(aluno3);

        // Configurar especialidades para validação
        Especialidade esp = new Especialidade();
        esp.setId(1L);
        esp.setNome("Pilates Clássico");
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        // Nota: Não mockamos conflitos ou ausências porque a exceção é lançada na validação de lotação (antes)

        assertThrows(SalaLotadaException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarEquipamentoPCDInsuficienteException_QuandoAlunosComLimitacoesExcedemEquipamentos() {
        Aluno alunoComLimitacao = new Aluno();
        alunoComLimitacao.setId(3L);
        alunoComLimitacao.setNome("Aluno com Limitação");
        alunoComLimitacao.setStatus(true);
        alunoComLimitacao.setAlunoComLimitacoesFisicas(true);
        alunos.add(alunoComLimitacao);
        dto.setAlunoIds(Set.of(1L, 2L, 3L));
        sala.setQuantidadeEquipamentosPCD(0);

        // Configurar especialidades para validação
        Especialidade esp = new Especialidade();
        esp.setId(1L);
        esp.setNome("Pilates Clássico");
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        // Nota: Não mockamos conflitos ou ausências porque a exceção é lançada na validação de equipamentos (antes)

        assertThrows(EquipamentoPCDInsuficienteException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarEntidadeNaoEncontradaException_QuandoSalaNaoExiste() {
        when(salaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarConflitoHorarioException_QuandoSalaJaEstaOcupada() {
        // Configurar especialidades para validação
        Especialidade esp = new Especialidade();
        esp.setId(1L);
        esp.setNome("Pilates Clássico");
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        // Criar um agendamento conflitante para a mensagem de erro
        Agendamento agendamentoConflitante = new Agendamento();
        agendamentoConflitante.setProfessor(professor);
        agendamentoConflitante.setSala(sala);

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        when(ausenciaRepository.findByProfessorId(any())).thenReturn(Collections.emptyList());
        when(agendamentoRepository.existsByProfessorIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(false);
        when(agendamentoRepository.existsBySalaIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(true);
        when(agendamentoRepository.findBySalaIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(Optional.of(agendamentoConflitante));
        // Nota: Não mockamos findAgendamentosByAlunoAndDataHoraExcludingId porque a exceção é lançada antes (conflito de sala)

        assertThrows(ConflitoHorarioException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarOperacaoInvalidaException_QuandoDataHoraNoPassado() {
        dto.setDataHora(LocalDateTime.now().minusHours(1));

        assertThrows(OperacaoInvalidaException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarOperacaoInvalidaException_QuandoDataHoraMuitoNoFuturo() {
        dto.setDataHora(LocalDateTime.now().plusYears(2));

        assertThrows(OperacaoInvalidaException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarOperacaoInvalidaException_QuandoHorarioAntesDas8h() {
        dto.setDataHora(LocalDateTime.now().plusDays(1).withHour(7).withMinute(0));

        assertThrows(OperacaoInvalidaException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarOperacaoInvalidaException_QuandoHorarioDepoisDas20h() {
        dto.setDataHora(LocalDateTime.now().plusDays(1).withHour(20).withMinute(0));

        assertThrows(OperacaoInvalidaException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarEntidadeNaoEncontradaException_QuandoProfessorNaoExiste() {
        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarEntidadeNaoEncontradaException_QuandoEspecialidadeNaoExiste() {
        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarEntidadeNaoEncontradaException_QuandoAlunoNaoExiste() {
        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(Collections.singletonList(alunos.get(0)));

        assertThrows(EntidadeNaoEncontradaException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarAlunoInativoException_QuandoAlunoEstaInativo() {
        alunos.get(0).setStatus(false);

        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);

        assertThrows(AlunoInativoException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarProfessorInativoException_QuandoProfessorEstaInativo() {
        professor.setStatus(false);

        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);

        assertThrows(ProfessorInativoException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarEspecialidadeIncompativelException_QuandoSalaNaoSuportaEspecialidade() {
        Especialidade outraEspecialidade = new Especialidade();
        outraEspecialidade.setId(2L);
        outraEspecialidade.setNome("Outra Especialidade");
        sala.setEspecialidades(Set.of(outraEspecialidade));

        Especialidade esp = new Especialidade();
        esp.setId(1L);
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);

        assertThrows(EspecialidadeIncompativelException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarEspecialidadeIncompativelException_QuandoProfessorNaoAtendeEspecialidade() {
        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));

        Especialidade outraEspecialidade = new Especialidade();
        outraEspecialidade.setId(2L);
        professor.setEspecialidades(Set.of(outraEspecialidade));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);

        assertThrows(EspecialidadeIncompativelException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarProfessorAusenteException_QuandoProfessorEstaAusentePorData() {
        Ausencia ausencia = new Ausencia();
        ausencia.setDataInicio(dto.getDataHora().minusHours(1));
        ausencia.setDataFim(dto.getDataHora().plusHours(1));

        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        when(ausenciaRepository.findByProfessorId(any())).thenReturn(Collections.singletonList(ausencia));

        assertThrows(ProfessorAusenteException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarConflitoHorarioException_QuandoProfessorJaTemAgendamento() {
        Agendamento agendamentoConflitante = new Agendamento();
        agendamentoConflitante.setProfessor(professor);
        agendamentoConflitante.setSala(sala);

        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        when(ausenciaRepository.findByProfessorId(any())).thenReturn(Collections.emptyList());
        when(agendamentoRepository.existsByProfessorIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(true);
        when(agendamentoRepository.findByProfessorIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(Optional.of(agendamentoConflitante));

        assertThrows(ConflitoHorarioException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarConflitoHorarioException_QuandoAlunoJaTemAgendamento() {
        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        Agendamento agendamentoAluno = new Agendamento();
        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        when(ausenciaRepository.findByProfessorId(any())).thenReturn(Collections.emptyList());
        when(agendamentoRepository.existsBySalaIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(false);
        when(agendamentoRepository.existsByProfessorIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(false);
        when(agendamentoRepository.findAgendamentosByAlunoAndDataHoraExcludingId(any(), any(), any())).thenReturn(Collections.singletonList(agendamentoAluno));

        assertThrows(ConflitoHorarioException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DevePassar_QuandoAgendamentoIdExcluirEVAlido() {
        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        when(ausenciaRepository.findByProfessorId(any())).thenReturn(Collections.emptyList());
        when(agendamentoRepository.existsBySalaIdAndDataHoraExcludingId(any(), any(), eq(1L))).thenReturn(false);
        when(agendamentoRepository.existsByProfessorIdAndDataHoraExcludingId(any(), any(), eq(1L))).thenReturn(false);
        when(agendamentoRepository.findAgendamentosByAlunoAndDataHoraExcludingId(any(), any(), eq(1L))).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> validator.validar(dto, 1L));
    }

    @Test
    void validar_DeveAceitarHorarioExatoDe8h() {
        dto.setDataHora(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0));

        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        when(ausenciaRepository.findByProfessorId(any())).thenReturn(Collections.emptyList());
        when(agendamentoRepository.existsBySalaIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(false);
        when(agendamentoRepository.existsByProfessorIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(false);
        when(agendamentoRepository.findAgendamentosByAlunoAndDataHoraExcludingId(any(), any(), any())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> validator.validar(dto));
    }

    @Test
    void validar_DeveAceitarHorarioExatoDe19h59() {
        dto.setDataHora(LocalDateTime.now().plusDays(1).withHour(19).withMinute(59));

        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        when(ausenciaRepository.findByProfessorId(any())).thenReturn(Collections.emptyList());
        when(agendamentoRepository.existsBySalaIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(false);
        when(agendamentoRepository.existsByProfessorIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(false);
        when(agendamentoRepository.findAgendamentosByAlunoAndDataHoraExcludingId(any(), any(), any())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarProfessorAusenteException_QuandoProfessorEstaAusentePorDiaSemana() {
        // Criar data para segunda-feira
        LocalDateTime segundaFeira = LocalDateTime.now().plusDays(1);
        while (segundaFeira.getDayOfWeek() != java.time.DayOfWeek.MONDAY) {
            segundaFeira = segundaFeira.plusDays(1);
        }
        dto.setDataHora(segundaFeira.withHour(14).withMinute(0));

        Ausencia ausencia = new Ausencia();
        ausencia.setDiaSemanaInicio(DiaSemana.SEGUNDA);
        ausencia.setDiaSemanaFim(DiaSemana.SEXTA);

        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        when(ausenciaRepository.findByProfessorId(any())).thenReturn(Collections.singletonList(ausencia));

        assertThrows(ProfessorAusenteException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DevePassar_QuandoProfessorNaoEstaAusentePorDiaSemana() {
        // Criar data para domingo (fora do intervalo de segunda a sexta)
        LocalDateTime domingo = LocalDateTime.now().plusDays(1);
        while (domingo.getDayOfWeek() != java.time.DayOfWeek.SUNDAY) {
            domingo = domingo.plusDays(1);
        }
        dto.setDataHora(domingo.withHour(14).withMinute(0));

        Ausencia ausencia = new Ausencia();
        ausencia.setDiaSemanaInicio(DiaSemana.SEGUNDA);
        ausencia.setDiaSemanaFim(DiaSemana.SEXTA);

        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        when(ausenciaRepository.findByProfessorId(any())).thenReturn(Collections.singletonList(ausencia));
        when(agendamentoRepository.existsBySalaIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(false);
        when(agendamentoRepository.existsByProfessorIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(false);
        when(agendamentoRepository.findAgendamentosByAlunoAndDataHoraExcludingId(any(), any(), any())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> validator.validar(dto));
    }

    @Test
    void validar_DevePassar_QuandoAusenciaNaoTemDatasNemDiasSemana() {
        Ausencia ausencia = new Ausencia();
        // Sem dataInicio, dataFim, diaSemanaInicio, diaSemanaFim

        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        when(ausenciaRepository.findByProfessorId(any())).thenReturn(Collections.singletonList(ausencia));
        when(agendamentoRepository.existsBySalaIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(false);
        when(agendamentoRepository.existsByProfessorIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(false);
        when(agendamentoRepository.findAgendamentosByAlunoAndDataHoraExcludingId(any(), any(), any())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarProfessorAusenteException_QuandoDataHoraIgualAoInicio() {
        Ausencia ausencia = new Ausencia();
        LocalDateTime dataHora = dto.getDataHora();
        ausencia.setDataInicio(dataHora);
        ausencia.setDataFim(dataHora.plusHours(2));

        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        when(ausenciaRepository.findByProfessorId(any())).thenReturn(Collections.singletonList(ausencia));

        assertThrows(ProfessorAusenteException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarProfessorAusenteException_QuandoDataHoraIgualAoFim() {
        Ausencia ausencia = new Ausencia();
        LocalDateTime dataHora = dto.getDataHora();
        ausencia.setDataInicio(dataHora.minusHours(2));
        ausencia.setDataFim(dataHora);

        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        when(ausenciaRepository.findByProfessorId(any())).thenReturn(Collections.singletonList(ausencia));

        assertThrows(ProfessorAusenteException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DevePassar_QuandoDataHoraAntesDoInicio() {
        Ausencia ausencia = new Ausencia();
        LocalDateTime dataHora = dto.getDataHora();
        ausencia.setDataInicio(dataHora.plusHours(1));
        ausencia.setDataFim(dataHora.plusHours(3));

        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        when(ausenciaRepository.findByProfessorId(any())).thenReturn(Collections.singletonList(ausencia));
        when(agendamentoRepository.existsBySalaIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(false);
        when(agendamentoRepository.existsByProfessorIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(false);
        when(agendamentoRepository.findAgendamentosByAlunoAndDataHoraExcludingId(any(), any(), any())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> validator.validar(dto));
    }

    @Test
    void validar_DevePassar_QuandoDataHoraDepoisDoFim() {
        Ausencia ausencia = new Ausencia();
        LocalDateTime dataHora = dto.getDataHora();
        ausencia.setDataInicio(dataHora.minusHours(3));
        ausencia.setDataFim(dataHora.minusHours(1));

        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        when(ausenciaRepository.findByProfessorId(any())).thenReturn(Collections.singletonList(ausencia));
        when(agendamentoRepository.existsBySalaIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(false);
        when(agendamentoRepository.existsByProfessorIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(false);
        when(agendamentoRepository.findAgendamentosByAlunoAndDataHoraExcludingId(any(), any(), any())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarConflitoHorarioException_QuandoConflitoSalaSemMensagem() {
        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        when(ausenciaRepository.findByProfessorId(any())).thenReturn(Collections.emptyList());
        when(agendamentoRepository.existsByProfessorIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(false);
        when(agendamentoRepository.existsBySalaIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(true);
        when(agendamentoRepository.findBySalaIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(Optional.empty());

        assertThrows(ConflitoHorarioException.class, () -> validator.validar(dto));
    }

    @Test
    void validar_DeveLancarConflitoHorarioException_QuandoConflitoProfessorSemMensagem() {
        Especialidade esp = new Especialidade();
        esp.setId(1L);
        sala.setEspecialidades(Set.of(esp));
        professor.setEspecialidades(Set.of(esp));

        when(salaRepository.findById(1L)).thenReturn(Optional.of(sala));
        when(professorRepository.findById(1L)).thenReturn(Optional.of(professor));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(alunoRepository.findAllById(any())).thenReturn(alunos);
        when(ausenciaRepository.findByProfessorId(any())).thenReturn(Collections.emptyList());
        when(agendamentoRepository.existsByProfessorIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(true);
        when(agendamentoRepository.findByProfessorIdAndDataHoraExcludingId(any(), any(), any())).thenReturn(Optional.empty());

        assertThrows(ConflitoHorarioException.class, () -> validator.validar(dto));
    }
}

