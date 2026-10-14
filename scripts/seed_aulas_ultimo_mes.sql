-- Aulas FICTÍCIAS de 14/09/2026 a 13/10/2026, apenas para popular dashboards.
-- Dados reais usados: professores 4,5,6; alunos 1-20; especialidades 1-8; salas 1-4.
-- Para remover: DELETE FROM agendamento WHERE data_hora >= '2026-09-14' AND data_hora < '2026-10-14'; (apague antes agendamento_aluno desses ids)
USE onepilates;
START TRANSACTION;

INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-14 08:00:00', 4, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 18, 'PRESENTE', '2026-09-14 09:00:00', NULL),
  (@ag, 3, 'PRESENTE', '2026-09-14 09:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-14 12:00:00', 6, 3, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 18, 'PRESENTE', '2026-09-14 13:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-14 13:00:00', 6, 3, 8);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 14, 'PRESENTE', '2026-09-14 14:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-15 08:00:00', 4, 2, 3);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 18, 'FALTA', '2026-09-15 09:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 4, 'FALTA', '2026-09-15 09:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 13, 'PRESENTE', '2026-09-15 09:00:00', NULL),
  (@ag, 3, 'PRESENTE', '2026-09-15 09:00:00', NULL),
  (@ag, 10, 'PRESENTE', '2026-09-15 09:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-15 13:00:00', 5, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 9, 'PRESENTE', '2026-09-15 14:00:00', NULL),
  (@ag, 3, 'PRESENTE', '2026-09-15 14:00:00', NULL),
  (@ag, 6, 'PRESENTE', '2026-09-15 14:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-15 15:00:00', 5, 3, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 2, 'PRESENTE', '2026-09-15 16:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-15 18:00:00', 6, 1, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 7, 'PRESENTE', '2026-09-15 19:00:00', NULL),
  (@ag, 16, 'PRESENTE', '2026-09-15 19:00:00', NULL),
  (@ag, 13, 'PRESENTE', '2026-09-15 19:00:00', NULL),
  (@ag, 15, 'PRESENTE', '2026-09-15 19:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-16 08:00:00', 6, 4, 5);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 5, 'PRESENTE', '2026-09-16 09:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-16 13:00:00', 5, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 20, 'PRESENTE', '2026-09-16 14:00:00', NULL),
  (@ag, 15, 'PRESENTE', '2026-09-16 14:00:00', NULL),
  (@ag, 17, 'FALTA', '2026-09-16 14:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 9, 'FALTA', '2026-09-16 14:00:00', 'Faltou sem aviso'),
  (@ag, 1, 'PRESENTE', '2026-09-16 14:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-16 15:00:00', 4, 2, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 4, 'FALTA', '2026-09-16 16:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 10, 'FALTA', '2026-09-16 16:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 17, 'PRESENTE', '2026-09-16 16:00:00', NULL),
  (@ag, 7, 'PRESENTE', '2026-09-16 16:00:00', NULL),
  (@ag, 5, 'PRESENTE', '2026-09-16 16:00:00', NULL),
  (@ag, 6, 'FALTA', '2026-09-16 16:00:00', 'Faltou - avisou com antecedência');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-17 09:00:00', 5, 3, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 3, 'FALTA', '2026-09-17 10:00:00', 'Faltou - avisou com antecedência');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-17 12:00:00', 6, 1, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 7, 'FALTA', '2026-09-17 13:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 18, 'PRESENTE', '2026-09-17 13:00:00', NULL),
  (@ag, 20, 'PRESENTE', '2026-09-17 13:00:00', NULL),
  (@ag, 10, 'PRESENTE', '2026-09-17 13:00:00', NULL),
  (@ag, 13, 'PRESENTE', '2026-09-17 13:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-17 15:00:00', 6, 3, 2);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 2, 'PRESENTE', '2026-09-17 16:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-18 08:00:00', 4, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 19, 'PRESENTE', '2026-09-18 09:00:00', NULL),
  (@ag, 16, 'PRESENTE', '2026-09-18 09:00:00', NULL),
  (@ag, 8, 'PRESENTE', '2026-09-18 09:00:00', NULL),
  (@ag, 20, 'PRESENTE', '2026-09-18 09:00:00', NULL),
  (@ag, 14, 'FALTA', '2026-09-18 09:00:00', 'Faltou sem aviso'),
  (@ag, 4, 'PRESENTE', '2026-09-18 09:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-18 09:00:00', 4, 2, 3);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 7, 'PRESENTE', '2026-09-18 10:00:00', NULL),
  (@ag, 20, 'PRESENTE', '2026-09-18 10:00:00', NULL),
  (@ag, 18, 'PRESENTE', '2026-09-18 10:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-18 12:00:00', 6, 3, 2);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 3, 'FALTA', '2026-09-18 13:00:00', 'Faltou - avisou com antecedência');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-18 14:00:00', 4, 1, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 13, 'PRESENTE', '2026-09-18 15:00:00', NULL),
  (@ag, 1, 'FALTA', '2026-09-18 15:00:00', 'Faltou sem aviso'),
  (@ag, 20, 'PRESENTE', '2026-09-18 15:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-18 15:00:00', 6, 4, 4);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 10, 'PRESENTE', '2026-09-18 16:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-18 17:00:00', 5, 3, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 2, 'PRESENTE', '2026-09-18 18:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-18 18:00:00', 6, 1, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 6, 'PRESENTE', '2026-09-18 19:00:00', NULL),
  (@ag, 3, 'PRESENTE', '2026-09-18 19:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-19 09:00:00', 4, 1, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 3, 'PRESENTE', '2026-09-19 10:00:00', NULL),
  (@ag, 14, 'PRESENTE', '2026-09-19 10:00:00', NULL),
  (@ag, 17, 'PRESENTE', '2026-09-19 10:00:00', NULL),
  (@ag, 11, 'PRESENTE', '2026-09-19 10:00:00', NULL),
  (@ag, 9, 'PRESENTE', '2026-09-19 10:00:00', NULL),
  (@ag, 4, 'PRESENTE', '2026-09-19 10:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-19 12:00:00', 6, 3, 2);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 17, 'PRESENTE', '2026-09-19 13:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-21 14:00:00', 6, 4, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 17, 'PRESENTE', '2026-09-21 15:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-21 15:00:00', 4, 1, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 4, 'PRESENTE', '2026-09-21 16:00:00', NULL),
  (@ag, 20, 'PRESENTE', '2026-09-21 16:00:00', NULL),
  (@ag, 18, 'PRESENTE', '2026-09-21 16:00:00', NULL),
  (@ag, 5, 'PRESENTE', '2026-09-21 16:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-21 16:00:00', 6, 4, 5);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 3, 'PRESENTE', '2026-09-21 17:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-22 11:00:00', 5, 3, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 18, 'PRESENTE', '2026-09-22 12:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-22 12:00:00', 6, 1, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 18, 'PRESENTE', '2026-09-22 13:00:00', NULL),
  (@ag, 2, 'PRESENTE', '2026-09-22 13:00:00', NULL),
  (@ag, 12, 'PRESENTE', '2026-09-22 13:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-22 18:00:00', 6, 1, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 20, 'FALTA', '2026-09-22 19:00:00', 'Faltou sem aviso'),
  (@ag, 5, 'PRESENTE', '2026-09-22 19:00:00', NULL),
  (@ag, 8, 'PRESENTE', '2026-09-22 19:00:00', NULL),
  (@ag, 6, 'FALTA', '2026-09-22 19:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 17, 'FALTA', '2026-09-22 19:00:00', 'Faltou - avisou com antecedência');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-23 10:00:00', 4, 1, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 15, 'FALTA', '2026-09-23 11:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 12, 'PRESENTE', '2026-09-23 11:00:00', NULL),
  (@ag, 10, 'PRESENTE', '2026-09-23 11:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-23 11:00:00', 5, 1, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 17, 'FALTA', '2026-09-23 12:00:00', 'Faltou sem aviso'),
  (@ag, 13, 'FALTA', '2026-09-23 12:00:00', 'Faltou sem aviso'),
  (@ag, 18, 'FALTA', '2026-09-23 12:00:00', 'Faltou sem aviso'),
  (@ag, 11, 'PRESENTE', '2026-09-23 12:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-23 12:00:00', 6, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 13, 'PRESENTE', '2026-09-23 13:00:00', NULL),
  (@ag, 19, 'PRESENTE', '2026-09-23 13:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-23 13:00:00', 4, 1, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 20, 'PRESENTE', '2026-09-23 14:00:00', NULL),
  (@ag, 11, 'PRESENTE', '2026-09-23 14:00:00', NULL),
  (@ag, 4, 'PRESENTE', '2026-09-23 14:00:00', NULL),
  (@ag, 10, 'PRESENTE', '2026-09-23 14:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-23 14:00:00', 5, 2, 3);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 20, 'PRESENTE', '2026-09-23 15:00:00', NULL),
  (@ag, 19, 'FALTA', '2026-09-23 15:00:00', 'Faltou sem aviso'),
  (@ag, 10, 'PRESENTE', '2026-09-23 15:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-23 15:00:00', 6, 4, 6);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 15, 'PRESENTE', '2026-09-23 16:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-23 16:00:00', 5, 1, 3);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 20, 'FALTA', '2026-09-23 17:00:00', 'Faltou sem aviso'),
  (@ag, 11, 'PRESENTE', '2026-09-23 17:00:00', NULL),
  (@ag, 3, 'FALTA', '2026-09-23 17:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 8, 'FALTA', '2026-09-23 17:00:00', 'Faltou sem aviso'),
  (@ag, 10, 'FALTA', '2026-09-23 17:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 4, 'PRESENTE', '2026-09-23 17:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-23 17:00:00', 6, 1, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 4, 'PRESENTE', '2026-09-23 18:00:00', NULL),
  (@ag, 14, 'FALTA', '2026-09-23 18:00:00', 'Faltou - avisou com antecedência');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-23 18:00:00', 4, 1, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 5, 'PRESENTE', '2026-09-23 19:00:00', NULL),
  (@ag, 15, 'PRESENTE', '2026-09-23 19:00:00', NULL),
  (@ag, 17, 'PRESENTE', '2026-09-23 19:00:00', NULL),
  (@ag, 11, 'FALTA', '2026-09-23 19:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 19, 'FALTA', '2026-09-23 19:00:00', 'Faltou - avisou com antecedência');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-24 08:00:00', 5, 1, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 16, 'PRESENTE', '2026-09-24 09:00:00', NULL),
  (@ag, 8, 'PRESENTE', '2026-09-24 09:00:00', NULL),
  (@ag, 9, 'PRESENTE', '2026-09-24 09:00:00', NULL),
  (@ag, 15, 'PRESENTE', '2026-09-24 09:00:00', NULL),
  (@ag, 3, 'PRESENTE', '2026-09-24 09:00:00', NULL),
  (@ag, 12, 'PRESENTE', '2026-09-24 09:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-24 09:00:00', 5, 1, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 18, 'PRESENTE', '2026-09-24 10:00:00', NULL),
  (@ag, 15, 'PRESENTE', '2026-09-24 10:00:00', NULL),
  (@ag, 14, 'FALTA', '2026-09-24 10:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 2, 'PRESENTE', '2026-09-24 10:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-24 10:00:00', 5, 1, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 14, 'PRESENTE', '2026-09-24 11:00:00', NULL),
  (@ag, 18, 'PRESENTE', '2026-09-24 11:00:00', NULL),
  (@ag, 19, 'PRESENTE', '2026-09-24 11:00:00', NULL),
  (@ag, 8, 'PRESENTE', '2026-09-24 11:00:00', NULL),
  (@ag, 16, 'PRESENTE', '2026-09-24 11:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-24 11:00:00', 6, 4, 4);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 13, 'PRESENTE', '2026-09-24 12:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-24 12:00:00', 5, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 2, 'PRESENTE', '2026-09-24 13:00:00', NULL),
  (@ag, 9, 'PRESENTE', '2026-09-24 13:00:00', NULL),
  (@ag, 13, 'PRESENTE', '2026-09-24 13:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-24 13:00:00', 4, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 1, 'PRESENTE', '2026-09-24 14:00:00', NULL),
  (@ag, 18, 'FALTA', '2026-09-24 14:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 2, 'PRESENTE', '2026-09-24 14:00:00', NULL),
  (@ag, 12, 'FALTA', '2026-09-24 14:00:00', 'Faltou sem aviso'),
  (@ag, 8, 'PRESENTE', '2026-09-24 14:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-24 17:00:00', 6, 3, 5);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 12, 'PRESENTE', '2026-09-24 18:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-24 18:00:00', 6, 3, 8);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 10, 'PRESENTE', '2026-09-24 19:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-25 10:00:00', 6, 4, 6);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 3, 'PRESENTE', '2026-09-25 11:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-25 11:00:00', 6, 2, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 19, 'PRESENTE', '2026-09-25 12:00:00', NULL),
  (@ag, 2, 'PRESENTE', '2026-09-25 12:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-25 14:00:00', 6, 3, 8);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 16, 'PRESENTE', '2026-09-25 15:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-25 16:00:00', 6, 4, 8);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 14, 'PRESENTE', '2026-09-25 17:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-25 17:00:00', 6, 4, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 16, 'PRESENTE', '2026-09-25 18:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-25 18:00:00', 6, 4, 4);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 9, 'FALTA', '2026-09-25 19:00:00', 'Faltou sem aviso');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-26 08:00:00', 6, 4, 5);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 16, 'FALTA', '2026-09-26 09:00:00', 'Faltou sem aviso');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-28 09:00:00', 4, 4, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 17, 'FALTA', '2026-09-28 10:00:00', 'Faltou sem aviso');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-28 10:00:00', 6, 1, 3);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 15, 'PRESENTE', '2026-09-28 11:00:00', NULL),
  (@ag, 1, 'PRESENTE', '2026-09-28 11:00:00', NULL),
  (@ag, 3, 'PRESENTE', '2026-09-28 11:00:00', NULL),
  (@ag, 10, 'PRESENTE', '2026-09-28 11:00:00', NULL),
  (@ag, 8, 'PRESENTE', '2026-09-28 11:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-28 12:00:00', 6, 4, 5);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 9, 'PRESENTE', '2026-09-28 13:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-28 15:00:00', 6, 1, 3);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 7, 'PRESENTE', '2026-09-28 16:00:00', NULL),
  (@ag, 20, 'PRESENTE', '2026-09-28 16:00:00', NULL),
  (@ag, 16, 'FALTA', '2026-09-28 16:00:00', 'Faltou - avisou com antecedência');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-29 13:00:00', 4, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 10, 'PRESENTE', '2026-09-29 14:00:00', NULL),
  (@ag, 5, 'PRESENTE', '2026-09-29 14:00:00', NULL),
  (@ag, 16, 'PRESENTE', '2026-09-29 14:00:00', NULL),
  (@ag, 4, 'FALTA', '2026-09-29 14:00:00', 'Faltou sem aviso'),
  (@ag, 1, 'FALTA', '2026-09-29 14:00:00', 'Faltou sem aviso'),
  (@ag, 20, 'PRESENTE', '2026-09-29 14:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-29 14:00:00', 6, 1, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 3, 'PRESENTE', '2026-09-29 15:00:00', NULL),
  (@ag, 8, 'FALTA', '2026-09-29 15:00:00', 'Faltou sem aviso'),
  (@ag, 4, 'PRESENTE', '2026-09-29 15:00:00', NULL),
  (@ag, 14, 'PRESENTE', '2026-09-29 15:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-29 15:00:00', 6, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 20, 'FALTA', '2026-09-29 16:00:00', 'Faltou sem aviso'),
  (@ag, 4, 'PRESENTE', '2026-09-29 16:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-29 16:00:00', 4, 1, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 1, 'PRESENTE', '2026-09-29 17:00:00', NULL),
  (@ag, 14, 'PRESENTE', '2026-09-29 17:00:00', NULL),
  (@ag, 15, 'PRESENTE', '2026-09-29 17:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-29 18:00:00', 4, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 19, 'PRESENTE', '2026-09-29 19:00:00', NULL),
  (@ag, 7, 'PRESENTE', '2026-09-29 19:00:00', NULL),
  (@ag, 14, 'FALTA', '2026-09-29 19:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 4, 'PRESENTE', '2026-09-29 19:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-30 10:00:00', 6, 4, 6);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 15, 'PRESENTE', '2026-09-30 11:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-30 11:00:00', 5, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 3, 'PRESENTE', '2026-09-30 12:00:00', NULL),
  (@ag, 2, 'PRESENTE', '2026-09-30 12:00:00', NULL),
  (@ag, 14, 'PRESENTE', '2026-09-30 12:00:00', NULL),
  (@ag, 11, 'FALTA', '2026-09-30 12:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 9, 'PRESENTE', '2026-09-30 12:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-30 12:00:00', 6, 3, 8);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 16, 'PRESENTE', '2026-09-30 13:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-30 13:00:00', 6, 3, 6);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 16, 'FALTA', '2026-09-30 14:00:00', 'Faltou sem aviso');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-30 14:00:00', 5, 1, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 16, 'PRESENTE', '2026-09-30 15:00:00', NULL),
  (@ag, 10, 'PRESENTE', '2026-09-30 15:00:00', NULL),
  (@ag, 13, 'PRESENTE', '2026-09-30 15:00:00', NULL),
  (@ag, 2, 'FALTA', '2026-09-30 15:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 15, 'FALTA', '2026-09-30 15:00:00', 'Faltou sem aviso');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-30 15:00:00', 5, 1, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 16, 'PRESENTE', '2026-09-30 16:00:00', NULL),
  (@ag, 15, 'PRESENTE', '2026-09-30 16:00:00', NULL),
  (@ag, 2, 'FALTA', '2026-09-30 16:00:00', 'Faltou sem aviso'),
  (@ag, 7, 'PRESENTE', '2026-09-30 16:00:00', NULL),
  (@ag, 9, 'PRESENTE', '2026-09-30 16:00:00', NULL),
  (@ag, 20, 'FALTA', '2026-09-30 16:00:00', 'Faltou - avisou com antecedência');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-09-30 17:00:00', 4, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 4, 'PRESENTE', '2026-09-30 18:00:00', NULL),
  (@ag, 5, 'PRESENTE', '2026-09-30 18:00:00', NULL),
  (@ag, 16, 'PRESENTE', '2026-09-30 18:00:00', NULL),
  (@ag, 10, 'PRESENTE', '2026-09-30 18:00:00', NULL),
  (@ag, 9, 'PRESENTE', '2026-09-30 18:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-01 08:00:00', 5, 1, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 11, 'PRESENTE', '2026-10-01 09:00:00', NULL),
  (@ag, 17, 'FALTA', '2026-10-01 09:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 9, 'PRESENTE', '2026-10-01 09:00:00', NULL),
  (@ag, 1, 'FALTA', '2026-10-01 09:00:00', 'Faltou sem aviso'),
  (@ag, 10, 'PRESENTE', '2026-10-01 09:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-01 10:00:00', 6, 4, 6);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 7, 'FALTA', '2026-10-01 11:00:00', 'Faltou sem aviso');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-01 11:00:00', 6, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 13, 'PRESENTE', '2026-10-01 12:00:00', NULL),
  (@ag, 20, 'FALTA', '2026-10-01 12:00:00', 'Faltou sem aviso'),
  (@ag, 5, 'PRESENTE', '2026-10-01 12:00:00', NULL),
  (@ag, 16, 'FALTA', '2026-10-01 12:00:00', 'Faltou sem aviso'),
  (@ag, 2, 'FALTA', '2026-10-01 12:00:00', 'Faltou sem aviso');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-01 13:00:00', 6, 3, 2);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 9, 'PRESENTE', '2026-10-01 14:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-01 14:00:00', 6, 1, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 13, 'PRESENTE', '2026-10-01 15:00:00', NULL),
  (@ag, 11, 'FALTA', '2026-10-01 15:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 16, 'PRESENTE', '2026-10-01 15:00:00', NULL),
  (@ag, 2, 'PRESENTE', '2026-10-01 15:00:00', NULL),
  (@ag, 3, 'PRESENTE', '2026-10-01 15:00:00', NULL),
  (@ag, 4, 'PRESENTE', '2026-10-01 15:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-01 16:00:00', 4, 2, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 2, 'PRESENTE', '2026-10-01 17:00:00', NULL),
  (@ag, 10, 'PRESENTE', '2026-10-01 17:00:00', NULL),
  (@ag, 12, 'PRESENTE', '2026-10-01 17:00:00', NULL),
  (@ag, 18, 'PRESENTE', '2026-10-01 17:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-02 10:00:00', 6, 3, 5);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 8, 'PRESENTE', '2026-10-02 11:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-02 12:00:00', 5, 1, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 18, 'FALTA', '2026-10-02 13:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 6, 'PRESENTE', '2026-10-02 13:00:00', NULL),
  (@ag, 3, 'FALTA', '2026-10-02 13:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 15, 'PRESENTE', '2026-10-02 13:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-02 15:00:00', 5, 3, 3);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 19, 'PRESENTE', '2026-10-02 16:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-02 17:00:00', 6, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 20, 'FALTA', '2026-10-02 18:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 16, 'FALTA', '2026-10-02 18:00:00', 'Faltou sem aviso');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-03 09:00:00', 6, 2, 3);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 4, 'FALTA', '2026-10-03 10:00:00', 'Faltou sem aviso'),
  (@ag, 2, 'PRESENTE', '2026-10-03 10:00:00', NULL),
  (@ag, 10, 'PRESENTE', '2026-10-03 10:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-03 12:00:00', 6, 1, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 17, 'PRESENTE', '2026-10-03 13:00:00', NULL),
  (@ag, 20, 'FALTA', '2026-10-03 13:00:00', 'Faltou sem aviso'),
  (@ag, 9, 'FALTA', '2026-10-03 13:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 6, 'PRESENTE', '2026-10-03 13:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-05 09:00:00', 6, 3, 8);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 13, 'FALTA', '2026-10-05 10:00:00', 'Faltou - avisou com antecedência');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-05 12:00:00', 6, 1, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 19, 'PRESENTE', '2026-10-05 13:00:00', NULL),
  (@ag, 13, 'PRESENTE', '2026-10-05 13:00:00', NULL),
  (@ag, 12, 'PRESENTE', '2026-10-05 13:00:00', NULL),
  (@ag, 4, 'FALTA', '2026-10-05 13:00:00', 'Faltou sem aviso');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-05 13:00:00', 5, 1, 3);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 14, 'FALTA', '2026-10-05 14:00:00', 'Faltou sem aviso'),
  (@ag, 4, 'PRESENTE', '2026-10-05 14:00:00', NULL),
  (@ag, 5, 'PRESENTE', '2026-10-05 14:00:00', NULL),
  (@ag, 2, 'FALTA', '2026-10-05 14:00:00', 'Faltou sem aviso');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-05 14:00:00', 6, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 14, 'PRESENTE', '2026-10-05 15:00:00', NULL),
  (@ag, 4, 'FALTA', '2026-10-05 15:00:00', 'Faltou sem aviso'),
  (@ag, 16, 'PRESENTE', '2026-10-05 15:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-05 16:00:00', 4, 1, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 18, 'PRESENTE', '2026-10-05 17:00:00', NULL),
  (@ag, 12, 'FALTA', '2026-10-05 17:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 2, 'FALTA', '2026-10-05 17:00:00', 'Faltou sem aviso'),
  (@ag, 13, 'PRESENTE', '2026-10-05 17:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-05 17:00:00', 6, 3, 2);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 8, 'FALTA', '2026-10-05 18:00:00', 'Faltou - avisou com antecedência');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-06 09:00:00', 4, 1, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 5, 'FALTA', '2026-10-06 10:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 1, 'FALTA', '2026-10-06 10:00:00', 'Faltou sem aviso'),
  (@ag, 9, 'FALTA', '2026-10-06 10:00:00', 'Faltou sem aviso'),
  (@ag, 20, 'PRESENTE', '2026-10-06 10:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-06 11:00:00', 4, 1, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 17, 'FALTA', '2026-10-06 12:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 4, 'PRESENTE', '2026-10-06 12:00:00', NULL),
  (@ag, 3, 'PRESENTE', '2026-10-06 12:00:00', NULL),
  (@ag, 16, 'FALTA', '2026-10-06 12:00:00', 'Faltou sem aviso'),
  (@ag, 15, 'FALTA', '2026-10-06 12:00:00', 'Faltou - avisou com antecedência');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-06 12:00:00', 5, 1, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 14, 'FALTA', '2026-10-06 13:00:00', 'Faltou sem aviso'),
  (@ag, 4, 'PRESENTE', '2026-10-06 13:00:00', NULL),
  (@ag, 16, 'PRESENTE', '2026-10-06 13:00:00', NULL),
  (@ag, 15, 'PRESENTE', '2026-10-06 13:00:00', NULL),
  (@ag, 3, 'PRESENTE', '2026-10-06 13:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-06 13:00:00', 6, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 4, 'PRESENTE', '2026-10-06 14:00:00', NULL),
  (@ag, 20, 'PRESENTE', '2026-10-06 14:00:00', NULL),
  (@ag, 18, 'PRESENTE', '2026-10-06 14:00:00', NULL),
  (@ag, 7, 'PRESENTE', '2026-10-06 14:00:00', NULL),
  (@ag, 14, 'PRESENTE', '2026-10-06 14:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-06 16:00:00', 4, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 3, 'PRESENTE', '2026-10-06 17:00:00', NULL),
  (@ag, 20, 'PRESENTE', '2026-10-06 17:00:00', NULL),
  (@ag, 19, 'FALTA', '2026-10-06 17:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 18, 'PRESENTE', '2026-10-06 17:00:00', NULL),
  (@ag, 14, 'PRESENTE', '2026-10-06 17:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-06 18:00:00', 5, 2, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 10, 'PRESENTE', '2026-10-06 19:00:00', NULL),
  (@ag, 20, 'PRESENTE', '2026-10-06 19:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-07 10:00:00', 6, 1, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 4, 'FALTA', '2026-10-07 11:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 9, 'FALTA', '2026-10-07 11:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 8, 'PRESENTE', '2026-10-07 11:00:00', NULL),
  (@ag, 14, 'PRESENTE', '2026-10-07 11:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-07 11:00:00', 4, 4, 3);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 9, 'PRESENTE', '2026-10-07 12:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-07 18:00:00', 6, 3, 3);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 1, 'PRESENTE', '2026-10-07 19:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-08 08:00:00', 4, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 10, 'PRESENTE', '2026-10-08 09:00:00', NULL),
  (@ag, 11, 'PRESENTE', '2026-10-08 09:00:00', NULL),
  (@ag, 3, 'PRESENTE', '2026-10-08 09:00:00', NULL),
  (@ag, 2, 'PRESENTE', '2026-10-08 09:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-08 09:00:00', 5, 4, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 14, 'PRESENTE', '2026-10-08 10:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-08 10:00:00', 6, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 16, 'FALTA', '2026-10-08 11:00:00', 'Faltou sem aviso'),
  (@ag, 17, 'PRESENTE', '2026-10-08 11:00:00', NULL),
  (@ag, 10, 'FALTA', '2026-10-08 11:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 2, 'FALTA', '2026-10-08 11:00:00', 'Faltou sem aviso'),
  (@ag, 8, 'PRESENTE', '2026-10-08 11:00:00', NULL),
  (@ag, 7, 'PRESENTE', '2026-10-08 11:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-08 11:00:00', 4, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 18, 'PRESENTE', '2026-10-08 12:00:00', NULL),
  (@ag, 12, 'FALTA', '2026-10-08 12:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 3, 'PRESENTE', '2026-10-08 12:00:00', NULL),
  (@ag, 13, 'PRESENTE', '2026-10-08 12:00:00', NULL),
  (@ag, 2, 'PRESENTE', '2026-10-08 12:00:00', NULL),
  (@ag, 7, 'PRESENTE', '2026-10-08 12:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-08 13:00:00', 6, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 12, 'PRESENTE', '2026-10-08 14:00:00', NULL),
  (@ag, 3, 'PRESENTE', '2026-10-08 14:00:00', NULL),
  (@ag, 14, 'PRESENTE', '2026-10-08 14:00:00', NULL),
  (@ag, 4, 'FALTA', '2026-10-08 14:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 8, 'PRESENTE', '2026-10-08 14:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-08 16:00:00', 6, 3, 8);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 12, 'PRESENTE', '2026-10-08 17:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-08 17:00:00', 6, 3, 2);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 9, 'PRESENTE', '2026-10-08 18:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-08 18:00:00', 6, 1, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 15, 'PRESENTE', '2026-10-08 19:00:00', NULL),
  (@ag, 19, 'PRESENTE', '2026-10-08 19:00:00', NULL),
  (@ag, 20, 'PRESENTE', '2026-10-08 19:00:00', NULL),
  (@ag, 11, 'PRESENTE', '2026-10-08 19:00:00', NULL),
  (@ag, 17, 'PRESENTE', '2026-10-08 19:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-09 10:00:00', 4, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 10, 'PRESENTE', '2026-10-09 11:00:00', NULL),
  (@ag, 3, 'PRESENTE', '2026-10-09 11:00:00', NULL),
  (@ag, 19, 'FALTA', '2026-10-09 11:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 17, 'PRESENTE', '2026-10-09 11:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-09 11:00:00', 5, 4, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 17, 'PRESENTE', '2026-10-09 12:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-09 13:00:00', 4, 2, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 6, 'PRESENTE', '2026-10-09 14:00:00', NULL),
  (@ag, 2, 'PRESENTE', '2026-10-09 14:00:00', NULL),
  (@ag, 8, 'PRESENTE', '2026-10-09 14:00:00', NULL),
  (@ag, 15, 'PRESENTE', '2026-10-09 14:00:00', NULL),
  (@ag, 17, 'PRESENTE', '2026-10-09 14:00:00', NULL),
  (@ag, 9, 'PRESENTE', '2026-10-09 14:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-09 14:00:00', 4, 2, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 13, 'PRESENTE', '2026-10-09 15:00:00', NULL),
  (@ag, 10, 'PRESENTE', '2026-10-09 15:00:00', NULL),
  (@ag, 9, 'PRESENTE', '2026-10-09 15:00:00', NULL),
  (@ag, 5, 'FALTA', '2026-10-09 15:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 11, 'PRESENTE', '2026-10-09 15:00:00', NULL),
  (@ag, 2, 'PRESENTE', '2026-10-09 15:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-09 17:00:00', 6, 2, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 3, 'PRESENTE', '2026-10-09 18:00:00', NULL),
  (@ag, 14, 'FALTA', '2026-10-09 18:00:00', 'Faltou sem aviso'),
  (@ag, 17, 'FALTA', '2026-10-09 18:00:00', 'Faltou sem aviso'),
  (@ag, 12, 'FALTA', '2026-10-09 18:00:00', 'Faltou sem aviso'),
  (@ag, 1, 'PRESENTE', '2026-10-09 18:00:00', NULL),
  (@ag, 6, 'FALTA', '2026-10-09 18:00:00', 'Faltou - avisou com antecedência');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-09 18:00:00', 5, 3, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 20, 'PRESENTE', '2026-10-09 19:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-10 08:00:00', 6, 1, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 15, 'PRESENTE', '2026-10-10 09:00:00', NULL),
  (@ag, 2, 'PRESENTE', '2026-10-10 09:00:00', NULL),
  (@ag, 14, 'FALTA', '2026-10-10 09:00:00', 'Faltou - avisou com antecedência');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-10 09:00:00', 6, 3, 6);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 10, 'FALTA', '2026-10-10 10:00:00', 'Faltou - avisou com antecedência');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-10 12:00:00', 6, 4, 6);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 13, 'PRESENTE', '2026-10-10 13:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-12 08:00:00', 6, 3, 8);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 9, 'PRESENTE', '2026-10-12 09:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-12 09:00:00', 6, 3, 7);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 4, 'PRESENTE', '2026-10-12 10:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-12 12:00:00', 6, 3, 8);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 3, 'FALTA', '2026-10-12 13:00:00', 'Faltou sem aviso');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-12 13:00:00', 6, 3, 5);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 13, 'PRESENTE', '2026-10-12 14:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-12 15:00:00', 6, 4, 4);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 11, 'FALTA', '2026-10-12 16:00:00', 'Faltou - avisou com antecedência');
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-12 16:00:00', 5, 1, 1);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 5, 'PRESENTE', '2026-10-12 17:00:00', NULL),
  (@ag, 16, 'PRESENTE', '2026-10-12 17:00:00', NULL),
  (@ag, 15, 'PRESENTE', '2026-10-12 17:00:00', NULL),
  (@ag, 1, 'FALTA', '2026-10-12 17:00:00', 'Faltou - avisou com antecedência'),
  (@ag, 3, 'PRESENTE', '2026-10-12 17:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-13 12:00:00', 4, 4, 3);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 16, 'PRESENTE', '2026-10-13 13:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-13 14:00:00', 5, 1, 3);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 1, 'PRESENTE', '2026-10-13 15:00:00', NULL),
  (@ag, 12, 'PRESENTE', '2026-10-13 15:00:00', NULL),
  (@ag, 8, 'FALTA', '2026-10-13 15:00:00', 'Faltou sem aviso'),
  (@ag, 14, 'PRESENTE', '2026-10-13 15:00:00', NULL),
  (@ag, 6, 'PRESENTE', '2026-10-13 15:00:00', NULL);
INSERT INTO agendamento (data_hora, professor_id, sala_id, especialidade_id) VALUES ('2026-10-13 15:00:00', 6, 4, 6);
SET @ag = LAST_INSERT_ID();
INSERT INTO agendamento_aluno (agendamento_id, aluno_id, status_presenca, data_registro_presenca, observacao) VALUES
  (@ag, 13, 'FALTA', '2026-10-13 16:00:00', 'Faltou sem aviso');

COMMIT;
-- 121 aulas geradas
