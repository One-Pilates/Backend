
DROP DATABASE IF EXISTS onepilates_teste;
CREATE DATABASE onepilates_teste CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE onepilates_teste;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `administrador`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `administrador` (
  `id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKeiqhn7uqjj78ivfpaqlbhosud` FOREIGN KEY (`id`) REFERENCES `funcionario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `agendamento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `agendamento` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `data_hora` datetime(6) DEFAULT NULL,
  `especialidade_id` bigint DEFAULT NULL,
  `professor_id` bigint DEFAULT NULL,
  `sala_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2beva5c536bady1s35r9qg0ha` (`especialidade_id`),
  KEY `FKjukq8w02t95f3nc3sca090k46` (`professor_id`),
  KEY `FK1wofsop92c7biqfj473a3hc96` (`sala_id`),
  CONSTRAINT `FK1wofsop92c7biqfj473a3hc96` FOREIGN KEY (`sala_id`) REFERENCES `sala` (`id`),
  CONSTRAINT `FK2beva5c536bady1s35r9qg0ha` FOREIGN KEY (`especialidade_id`) REFERENCES `especialidade` (`id`),
  CONSTRAINT `FKjukq8w02t95f3nc3sca090k46` FOREIGN KEY (`professor_id`) REFERENCES `professor` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `agendamento_aluno`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `agendamento_aluno` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `credito_consumido` bit(1) DEFAULT NULL,
  `data_confirmacao` datetime(6) DEFAULT NULL,
  `data_registro_presenca` datetime(6) DEFAULT NULL,
  `observacao` varchar(255) DEFAULT NULL,
  `presenca_confirmada` bit(1) DEFAULT NULL,
  `status_presenca` enum('PENDENTE','PRESENTE','FALTA') NOT NULL,
  `agendamento_id` bigint NOT NULL,
  `aluno_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3jbcu4q0f2pkqftpnxofy7il5` (`agendamento_id`),
  KEY `FKri0gqu6qfn5a7iwjuggb2xewe` (`aluno_id`),
  CONSTRAINT `FK3jbcu4q0f2pkqftpnxofy7il5` FOREIGN KEY (`agendamento_id`) REFERENCES `agendamento` (`id`),
  CONSTRAINT `FKri0gqu6qfn5a7iwjuggb2xewe` FOREIGN KEY (`aluno_id`) REFERENCES `aluno` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `aluno`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `aluno` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `aluno_com_limitacoes_fisicas` bit(1) DEFAULT NULL,
  `cpf` varchar(255) DEFAULT NULL,
  `data_nascimento` date DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `foto` varchar(255) DEFAULT NULL,
  `nome` varchar(255) DEFAULT NULL,
  `notificacao_ativa` bit(1) DEFAULT NULL,
  `observacao` varchar(255) DEFAULT NULL,
  `primeiro_acesso` bit(1) DEFAULT NULL,
  `senha` varchar(255) DEFAULT NULL,
  `status` bit(1) DEFAULT NULL,
  `tipo_contato` varchar(255) DEFAULT NULL,
  `endereco_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_crrvmtky7d9tfarahi4jahewg` (`cpf`),
  UNIQUE KEY `UK_3wpes15e0anbfaa4do0pey97k` (`email`),
  UNIQUE KEY `UK_dky20u41x9c3xw6c1uo7g6rn6` (`endereco_id`),
  CONSTRAINT `FKeooy7cntssxbfvyx7odcig95j` FOREIGN KEY (`endereco_id`) REFERENCES `endereco` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ausencia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ausencia` (
  `id` int NOT NULL AUTO_INCREMENT,
  `data_fim` datetime(6) DEFAULT NULL,
  `data_inicio` datetime(6) DEFAULT NULL,
  `dia_semana_fim` enum('SEGUNDA','TERCA','QUARTA','QUINTA','SEXTA','SABADO','DOMINGO') DEFAULT NULL,
  `dia_semana_inicio` enum('SEGUNDA','TERCA','QUARTA','QUINTA','SEXTA','SABADO','DOMINGO') DEFAULT NULL,
  `motivo` varchar(255) DEFAULT NULL,
  `professor_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKovdht79waowsla7qidesca49r` (`professor_id`),
  CONSTRAINT `FKovdht79waowsla7qidesca49r` FOREIGN KEY (`professor_id`) REFERENCES `professor` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `cancelamento_aula`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cancelamento_aula` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `aluno_id` bigint DEFAULT NULL,
  `cancelado_em` datetime(6) DEFAULT NULL,
  `data_hora` datetime(6) DEFAULT NULL,
  `especialidade` varchar(255) DEFAULT NULL,
  `nome_aluno` varchar(255) DEFAULT NULL,
  `professor` varchar(255) DEFAULT NULL,
  `sala` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `endereco`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `endereco` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `bairro` varchar(255) DEFAULT NULL,
  `cep` varchar(255) DEFAULT NULL,
  `cidade` varchar(255) DEFAULT NULL,
  `estado` varchar(255) DEFAULT NULL,
  `numero` varchar(255) DEFAULT NULL,
  `rua` varchar(255) DEFAULT NULL,
  `uf` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `especialidade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `especialidade` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nome` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `feedback_aula`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feedback_aula` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `agendamento_id` bigint DEFAULT NULL,
  `aluno_id` bigint DEFAULT NULL,
  `comentario` varchar(255) DEFAULT NULL,
  `nota` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `funcionario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `funcionario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cargo` varchar(255) DEFAULT NULL,
  `codigo_verificacao` varchar(255) DEFAULT NULL,
  `cpf` varchar(255) DEFAULT NULL,
  `data_nascimento` date DEFAULT NULL,
  `data_ultima_criacao_codigo` datetime(6) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `foto` varchar(255) DEFAULT NULL,
  `nome` varchar(255) DEFAULT NULL,
  `notificacao_ativa` bit(1) DEFAULT NULL,
  `observacoes` varchar(255) DEFAULT NULL,
  `primeiro_acesso` bit(1) DEFAULT NULL,
  `role` enum('ADMINISTRADOR','SECRETARIA','PROFESSOR','ALUNO') NOT NULL,
  `senha` varchar(255) NOT NULL,
  `status` bit(1) DEFAULT NULL,
  `telefone` varchar(255) DEFAULT NULL,
  `endereco_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_t45qja1wnv0hu1cdw6vqjljgy` (`email`),
  UNIQUE KEY `UK_rxosr8731eb3gbnlbt2mqfan8` (`cpf`),
  UNIQUE KEY `UK_b31pv382jnxx59df9akrcd9ho` (`endereco_id`),
  CONSTRAINT `FKld57l49lwv8ik9ottu7yo5d6k` FOREIGN KEY (`endereco_id`) REFERENCES `endereco` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ia_acoes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ia_acoes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descricao` varchar(300) NOT NULL,
  `finalizada_em` datetime(6) DEFAULT NULL,
  `iniciada_em` datetime(6) DEFAULT NULL,
  `ordem` int NOT NULL,
  `realizacao_id` bigint NOT NULL,
  `status` enum('EM_ANDAMENTO','CONCLUIDA','ERRO') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_ia_acoes_realizacao` (`realizacao_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ia_conversas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ia_conversas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `criada_em` datetime(6) DEFAULT NULL,
  `ultimo_uso` datetime(6) DEFAULT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `ia_realizacoes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ia_realizacoes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `atualizado_em` datetime(6) DEFAULT NULL,
  `conversa_id` bigint NOT NULL,
  `criado_em` datetime(6) DEFAULT NULL,
  `erro` varchar(1000) DEFAULT NULL,
  `mensagem` text NOT NULL,
  `prompt_melhorado` text,
  `resposta` text,
  `status` enum('PENDENTE','PROCESSANDO','CONCLUIDO','ERRO') NOT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_ia_realizacoes_conversa` (`conversa_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `pacote_aula`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pacote_aula` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `aulas_remanescentes` int DEFAULT NULL,
  `aluno_id` bigint NOT NULL,
  `especialidade_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKhwwfnqgip1u4pd8q1stxxs7ex` (`aluno_id`,`especialidade_id`),
  KEY `FKi6g5im2i428w9s22sc14669ge` (`especialidade_id`),
  CONSTRAINT `FKe9eyxlc45mnsdmi74dt89q3xf` FOREIGN KEY (`aluno_id`) REFERENCES `aluno` (`id`),
  CONSTRAINT `FKi6g5im2i428w9s22sc14669ge` FOREIGN KEY (`especialidade_id`) REFERENCES `especialidade` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `professor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `professor` (
  `id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKto8oi5mkkjb7ellnpdy9jqy5h` FOREIGN KEY (`id`) REFERENCES `funcionario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `professor_especialidade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `professor_especialidade` (
  `professor_id` bigint NOT NULL,
  `especialidade_id` bigint NOT NULL,
  PRIMARY KEY (`professor_id`,`especialidade_id`),
  KEY `FKdhi9lrtpqsvbc89mtqpxbfd8m` (`especialidade_id`),
  CONSTRAINT `FKdhi9lrtpqsvbc89mtqpxbfd8m` FOREIGN KEY (`especialidade_id`) REFERENCES `especialidade` (`id`),
  CONSTRAINT `FKr8l83wipvn0j8dsn0md2jjkwk` FOREIGN KEY (`professor_id`) REFERENCES `professor` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sala`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sala` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nome` varchar(255) DEFAULT NULL,
  `quantidade_equipamentos_pcd` int NOT NULL,
  `quantidade_maxima_alunos` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sala_especialidade`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sala_especialidade` (
  `sala_id` bigint NOT NULL,
  `especialidade_id` bigint NOT NULL,
  PRIMARY KEY (`sala_id`,`especialidade_id`),
  KEY `FKlclqc9n0phtltcy2d7rncnb9x` (`especialidade_id`),
  CONSTRAINT `FKcx5vv0bu32xc68288u1s70v4y` FOREIGN KEY (`sala_id`) REFERENCES `sala` (`id`),
  CONSTRAINT `FKlclqc9n0phtltcy2d7rncnb9x` FOREIGN KEY (`especialidade_id`) REFERENCES `especialidade` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `secretaria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `secretaria` (
  `id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKj2q7l1fluiemk1tjo00wfjj26` FOREIGN KEY (`id`) REFERENCES `funcionario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

SET FOREIGN_KEY_CHECKS = 1;

-- Senha de todos os funcionarios e do aluno 1: senha123 (BCrypt)
SET @senha = '$2a$10$ELlcH8Wmg8kIlobvl9PmsuFhosXYUg4YktFx64rLm8qaFDBO1yvqK';

INSERT INTO especialidade (id, nome) VALUES
    (1, 'Pilates'),
    (2, 'Fisioterapia');

INSERT INTO sala (id, nome, quantidade_maxima_alunos, quantidade_equipamentos_pcd) VALUES
    (1, 'Sala 1', 10, 2);

INSERT INTO sala_especialidade (sala_id, especialidade_id) VALUES
    (1, 1),
    (1, 2);

INSERT INTO funcionario (id, nome, email, senha, role, cpf, status, notificacao_ativa, primeiro_acesso, cargo) VALUES
    (1, 'Admin Teste', 'admin@teste.com', @senha, 'ADMINISTRADOR', '00000000001', b'1', b'0', b'0', 'Administrador'),
    (2, 'Secretaria Teste', 'secretaria@teste.com', @senha, 'SECRETARIA', '00000000002', b'1', b'0', b'0', 'Secretaria'),
    (3, 'Professor Teste', 'professor@teste.com', @senha, 'PROFESSOR', '00000000003', b'1', b'0', b'0', 'Professor');

INSERT INTO administrador (id) VALUES (1);
INSERT INTO secretaria (id) VALUES (2);
INSERT INTO professor (id) VALUES (3);

INSERT INTO professor_especialidade (professor_id, especialidade_id) VALUES (3, 1);

INSERT INTO endereco (id, rua, numero, bairro, cidade, estado, cep, uf) VALUES
    (1, 'Rua do Aluno', '10', 'Centro', 'Sao Paulo', 'Sao Paulo', '01001000', 'SP'),
    (2, 'Rua do Aluno', '20', 'Centro', 'Sao Paulo', 'Sao Paulo', '01001000', 'SP');

INSERT INTO aluno (id, nome, email, cpf, senha, status, notificacao_ativa, primeiro_acesso, aluno_com_limitacoes_fisicas, tipo_contato, endereco_id) VALUES
    (1, 'Aluno 1', 'aluno1@teste.com', '11111111111', @senha, b'1', b'1', b'0', b'0', 'whatsapp', 1),
    (2, 'Aluno 2', 'aluno2@teste.com', '22222222222', NULL, b'1', b'0', b'0', b'0', 'telefone', 2);

-- Saldo 9: o pacote nasceu com 10 e a aula passada ja debitou 1.
INSERT INTO pacote_aula (id, aluno_id, especialidade_id, aulas_remanescentes) VALUES
    (1, 1, 1, 9);

INSERT INTO agendamento (id, data_hora, professor_id, sala_id, especialidade_id) VALUES
    (1, '2026-12-15 10:00:00.000000', 3, 1, 1),
    (2, '2026-10-01 10:00:00.000000', 3, 1, 1);

INSERT INTO agendamento_aluno (id, agendamento_id, aluno_id, status_presenca, presenca_confirmada, credito_consumido) VALUES
    (1, 1, 1, 'PENDENTE', b'0', b'0'),
    (2, 1, 2, 'PENDENTE', b'0', b'0'),
    (3, 2, 1, 'PRESENTE', b'0', b'1');

