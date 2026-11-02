# Portal do aluno

O aluno autentica com a role `ALUNO` e usa a base `/api/aluno`. O id dele sai do e-mail do JWT. Ele não cria, não edita e não reagenda aula. O cadastro completo continua em `/api/alunos`, exclusivo da equipe.

O contrato pedido está em [PROMPT_PLANEJAMENTO_ALUNO.md](PROMPT_PLANEJAMENTO_ALUNO.md). As rotas novas estão no Swagger com `@Tag`, `@Operation` e `@ApiResponse`.

## Autenticação

`Role` agora inclui `ALUNO`. Essa role não é gravada em funcionário, professor ou secretaria: se vier no DTO desses cadastros, o service responde com erro de negócio.

O aluno tem `senha` (BCrypt), `foto` e `primeiroAcesso`. `POST /auth/login` procura primeiro um funcionário pelo e-mail e, se não achar, um aluno. O token carrega a role `ALUNO`. `JwtAuthFilter` autentica esse token mesmo sem linha em `funcionario`. Pedido sem token nas rotas protegidas responde `401`.

As tools da equipe que estavam abertas para qualquer role agora exigem `ADMINISTRADOR`, `SECRETARIA` ou `PROFESSOR`: `listar_agendamentos`, `listar_proximos_agendamentos`, `listar_especialidades`, `listar_professores_e_salas`, `buscar_alunos` e `ver_observacoes_aluno`.

## Rotas da equipe

Base `/api/alunos`. Criar, buscar por id e excluir continuam com administrador e secretaria. Listar e o `PATCH` de cadastro também aceitam professor.

- `GET /api/alunos/{id}/pacotes` — administrador e secretaria consultam qualquer aluno. O aluno autenticado consulta só o próprio id; outro id responde `403`.
- `PUT /api/alunos/{alunoId}/pacotes/{especialidadeId}` — corpo `{ "aulasRemanescentes": 10 }`. Só administrador e secretaria. O valor substitui o saldo. Zero é válido. Negativo é rejeitado. Aluno e professor recebem `403`.

## Rotas do portal

Base `/api/aluno`, autoridade `ALUNO`.

| Método | Caminho | Efeito |
|---|---|---|
| GET | `/api/aluno/agenda` | Aulas futuras em que ele ainda está. Query opcional `de` e `ate`. |
| PATCH | `/api/aluno/agenda/{agendamentoId}/presenca` | Confirma que vai comparecer. Aula passada: `400` `AULA_JA_REALIZADA`. Aula de outro aluno: `404`. |
| DELETE | `/api/aluno/agenda/{agendamentoId}` | Cancela a própria participação. Menos de 24 horas, inclusive aula no passado: `400` `CANCELAMENTO_FORA_DO_PRAZO`. Exatamente 24 horas ainda cancela. |
| POST | `/api/aluno/agenda/{agendamentoId}/feedback` | Nota de 1 a 5 e comentário opcional, só depois do início da aula. Antes: `400` `AULA_AINDA_NAO_REALIZADA`. Segundo feedback: `409`. |
| PATCH | `/api/aluno/agenda/{agendamentoId}/feedback` | Atualiza o próprio feedback. |
| GET | `/api/aluno/perfil` | Perfil do token. |
| PATCH | `/api/aluno/perfil` | Só `notificacaoAtiva` e `tipoContato`. Nome, CPF e status enviados no JSON são ignorados. |
| POST | `/api/aluno/perfil/foto` | Multipart no campo `foto`. |
| GET | `/api/aluno/dashboard/{qtdUltimosDias}` | Histórico, gráficos e KPIs. Período de 1 a 365. |
| GET | `/api/aluno/pacotes` | Saldo do próprio aluno. |

Confirmar presença grava `presencaConfirmada` e `dataConfirmacao`. Isso é separado do `PRESENTE` / `FALTA` que a equipe registra em `PATCH /api/agendamentos/{id}/presenca`. Esse endpoint da equipe não foi aberto para o aluno.

Cancelar remove o vínculo `AgendamentoAluno` e grava um `CancelamentoAula` com aluno, data, especialidade, professor, sala e `canceladoEm`. Se ele for o último aluno, a aula inteira sai pelo `AgendamentoService.excluirAgendamento`. Se restarem outros, a aula fica e o professor é avisado quando `notificacaoAtiva` for verdadeiro.

## Pacote de aulas

Cada aluno tem um saldo por especialidade, na tabela `pacote_aula`. Agendar não debita. `PRESENTE` em `AgendamentoService.registrarPresencas` baixa 1 na especialidade da aula e marca `creditoConsumido` no vínculo. Um segundo `PRESENTE` não debita de novo. `FALTA` e `PENDENTE` não debitam. Sem pacote cadastrado, a presença é gravada e o saldo não muda. Saldo zero não fica negativo e não é marcado como debitado. Cancelar o que já foi debitado devolve 1. Se a especialidade da aula mudar, o estorno sai da especialidade antiga e o débito entra na nova só para quem continua `PRESENTE`.

## Dashboard

A janela é de `hoje - N dias` até o fim de hoje. Aulas futuras ficam na agenda. A taxa de presença é `presenças / (presenças + faltas)`. Cancelada e pendente entram no histórico e ficam fora da taxa.

## IA do aluno

Só `Role.ALUNO`, e nenhuma tool aceita `alunoId` vindo do modelo.

- `minha_agenda` — próximas aulas do próprio aluno e o saldo do pacote. Sem nomes de outros alunos.
- `especialidades_estudio` — nome da especialidade, professores e salas. Sem id de agendamento e sem dados de aluno.

O system prompt desse perfil consulta a própria agenda, as especialidades do estúdio e o saldo. Não agenda, não cancela e não vê dados de outros alunos.

## Testes

Unidade e fatia HTTP seguem `AlunoServiceTest` e `AlunoControllerTest`: Mockito para service e `@WebMvcTest` para controller. Os casos novos estão em `PacoteAulaServiceTest`, `PortalAlunoServiceTest`, `AlunoPortalControllerTest` e nos testes de tool e de login.

Integração e Swagger usam o banco `onepilates_teste`, separado de `onepilates`. O script [agendamento/src/test/resources/sql/onepilates_teste.sql](agendamento/src/test/resources/sql/onepilates_teste.sql) recria esse banco e carrega administrador, secretaria, professor, dois alunos, um pacote e duas aulas. A senha dos usuários da carga é `senha123`.

```bash
mysql -u root -p < agendamento/src/test/resources/sql/onepilates_teste.sql
cp agendamento/src/test/resources/application-test.properties.example agendamento/src/test/resources/application-test.properties
```

Preencha a senha do MySQL só no `application-test.properties`. Esse arquivo não entra no git.

Na pasta `agendamento`:

```bash
mvn -Dtest=AgendamentoControllerIntegrationTest,AlunoSwaggerIntegrationTest test
```

`AlunoSwaggerIntegrationTest` lê `/v3/api-docs` e chama as rotas com o Bearer obtido em `POST /auth/login`, do mesmo modo que o Swagger depois de Authorize.

Swagger da aplicação: http://localhost:8080/swagger-ui.html
