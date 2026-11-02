# One Pilates - Backend API

Backend robusto para gerenciamento de agendamentos, clientes, professores e funcionalidades administrativas da One Pilates.

<p align="center">
  <img src="https://skillicons.dev/icons?i=java,spring,mysql,rabbitmq" alt="Tecnologias principais" />
</p>

## Stack
- Java 17+
- Spring Boot 3
- Spring Security (JWT)
- Spring Data JPA
- MySQL
- RabbitMQ
- Swagger/OpenAPI
- Maven
- Lombok

## Funcionalidades
- Autenticação e autorização com JWT
- Gerenciamento de agendamentos
- Gestão de alunos, professores e administradores
- Controle de ausências
- Gerenciamento de especialidades e salas
- Upload de imagens
- API REST completa documentada
- Fila de mensagens com RabbitMQ
- Validações robustas de dados
- Portal do aluno: agenda, presença, cancelamento, feedback, perfil, pacote e dashboard. Detalhes em [PORTAL_ALUNO.md](PORTAL_ALUNO.md).

## Requisitos
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- RabbitMQ 3.12+

## Como rodar localmente
```bash
# Instalar dependências
mvn clean install

# Ambiente de desenvolvimento
mvn spring-boot:run
```

Aplicação local: http://localhost:8080

## Scripts
```bash
# Desenvolvimento
mvn spring-boot:run

# Build de produção
mvn clean package

# Executar testes
mvn test

# Executar testes com cobertura
mvn test jacoco:report

# Executar aplicação gerada
java -jar target/agendamento-0.0.1-SNAPSHOT.jar
```

## Build para produção
```bash
mvn clean package -DskipTests
```

Os arquivos finais serão gerados em `target/`.

## Documentação da API
Após iniciar a aplicação, acesse:

📖 Swagger UI: http://localhost:8080/swagger-ui.html

📋 OpenAPI JSON: http://localhost:8080/v3/api-docs

## Estrutura principal
```text
src/
  main/
    java/
      com/onePilates/agendamento/
        config/           # Configurações (Security, Swagger)
        controller/       # Controllers REST
        dto/              # Data Transfer Objects
        exception/        # Exceções customizadas
        handler/          # Exception handlers
        model/            # Entidades JPA
        repository/       # Repositories Spring Data
        service/          # Lógica de negócio
        security/         # Componentes de segurança
        validator/        # Validadores
        observer/         # Padrão Observer
    resources/
      application.properties    # Configurações da aplicação
  test/
    java/
      com/onePilates/agendamento/
        controller/       # Testes de controllers
        service/          # Testes de serviços
        integration/      # Testes de integração
```

## Variáveis de Ambiente
Crie um arquivo `application.properties` na pasta `src/main/resources/` com as configurações necessárias:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/onepilates
spring.datasource.username=root
spring.datasource.password=

# JWT
jwt.secret=sua_chave_secreta_aqui
jwt.expiration=86400000

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

## Licença
Este projeto é distribuído sob a licença MIT.
