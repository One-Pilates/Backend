# Prompt — Portal do aluno OnePilates

Implemente o portal do aluno no backend Spring (`Backend/agendamento`), no pacote `com.onePilates.agendamento`. Não altere regras, endpoints ou tools que já funcionam para administrador, secretaria e professor, exceto o ponto de integração indicado abaixo (presença e cancelamento de aula, para o pacote). Siga o estilo do código atual: controller fino, regra no service, DTO manual com getter/setter, `@PreAuthorize("hasAnyAuthority(...)")` ou `hasAuthority`, exceção de negócio estendendo `BusinessException`, resposta de erro pelo `GlobalExceptionHandler` (`400` com `erro` e `codigoErro`).

Os controllers atuais não usam anotação OpenAPI. Nos endpoints novos, documente no Swagger com `@Tag`, `@Operation` e `@ApiResponse` (springdoc já está no `pom.xml`). Cada operação nova precisa de teste de service (Mockito) e de controller (`@WebMvcTest`, `TestSecurityConfig`, `@WithMockUser`), no mesmo formato de `AlunoServiceTest` e `AlunoControllerTest`.

## O que já existe e não deve ser refeito

- `Aluno` não é `Funcionario`. Não tem senha, foto nem `Role`. O enum `Role` é só `ADMINISTRADOR`, `SECRETARIA`, `PROFESSOR`. `JwtAuthFilter` e `IAService` resolvem apenas `Funcionario` pelo e-mail do JWT.
- Presença de aula já existe para a equipe: `PATCH /api/agendamentos/{agendamentoId}/presenca`, depois do horário, com `StatusPresenca` `PENDENTE`, `PRESENTE` ou `FALTA`. Isso continua exclusivo de administrador, secretaria e professor. Não entregue esse endpoint ao aluno.
- Cancelamento de aula da equipe é `DELETE /api/agendamentos/{id}` (`AgendamentoService.excluirAgendamento`), que apaga o agendamento inteiro e notifica professor e alunos. Uma aula pode ter vários alunos em `AgendamentoAluno`.
- Dashboard do professor: `GET /api/professores/{id}/{qtdUltimosDias}` e `ProfessorService.respostaDashProfessora`. Janela de `hoje - N dias` até o fim de hoje. Período válido: 1 a 365. Gráficos: `AgendamentoPorDiaDTO` e `AulaPorEspecialidadeDTO`. KPIs em `KPIsProfessorDTO`.
- Tools da IA implementam `FerramentaIA` e são filtradas por `rolesPermitidas()`. O padrão da interface é `EnumSet.allOf(Role.class)`. Várias tools não sobrescrevem isso: `listar_agendamentos`, `listar_proximos_agendamentos`, `listar_especialidades`, `listar_professores_e_salas`, `buscar_alunos`, `ver_observacoes_aluno`.
- `Especialidade` só tem `nome` (e os vínculos com professor e sala). Não existe descrição textual.
- `Aluno` tem `notificacaoAtiva` e `tipoContato`. O `AlunoDTO` chama o contato de `telefone` e o service grava em `tipoContato`. Não há campo `foto`.

## Pré-requisito: o aluno autentica

Sem isso, nenhum endpoint do aluno pode ir para produção.

1. Inclua `ALUNO` em `Role`.
2. Antes disso, feche as seis tools citadas acima com `rolesPermitidas()` explícito para `ADMINISTRADOR`, `SECRETARIA` e `PROFESSOR`. Se `ALUNO` entrar no enum com o padrão `EnumSet.allOf`, o aluno passa a ver agenda do estúdio, lista de alunos e observações clínicas.
3. Não grave `Role.ALUNO` em `Funcionario`, `Professor` ou `Secretaria`. Rejeite essa role se vier no DTO desses cadastros.
4. Em `Aluno`, acrescente `senha` (BCrypt), `foto` e, se o primeiro acesso dos funcionários for reaproveitado, `primeiroAcesso`.
5. Login e `JwtAuthFilter` resolvem funcionário ou aluno pelo e-mail. `IAService` / `IAProcessador` montam `ContextoUsuario` também para aluno (`usuarioId` = id do aluno).
6. Nenhum endpoint do aluno recebe o id dele na URL como fonte de verdade. O id sai do e-mail do token. O dashboard do professor hoje aceita qualquer id na URL; não repita esse IDOR.

## Regras do portal

O aluno vê as próprias aulas e pode cancelar a própria participação. Não cria, não edita e não reagenda aula. Não usa `POST /api/agendamentos` nem `PATCH /api/alunos/{id}` (esse patch continua com administrador, secretaria e professor e altera cadastro completo).

Cancelar é o vínculo `AgendamentoAluno`, não o agendamento inteiro. Se ele for o último aluno, exclua o agendamento pelo fluxo que já notifica o professor. Se restarem outros alunos, a aula permanece e o professor é avisado da desistência quando `notificacaoAtiva` for verdadeiro.

Prazo: cancelar é permitido quando `dataHora - agora >= 24 horas`. Menos que isso, inclusive aula no passado, responde `400` com `codigoErro` `CANCELAMENTO_FORA_DO_PRAZO` e a mensagem “Não é possível cancelar a aula com menos de 24 horas de antecedência.” Exatamente 24 horas ainda cancela. Injete `java.time.Clock` nesse cálculo para o teste não depender do relógio real. Aula cancelada fora do prazo não altera vínculo, pacote nem notificação.

Confirmar presença é intenção antes da aula, separada do `PRESENTE`/`FALTA` da equipe. Em `AgendamentoAluno`, grave `presencaConfirmada` e `dataConfirmacao`. Só em aula futura, com o vínculo ainda ativo. Aula passada: `400` `AULA_JA_REALIZADA`.

Feedback é uma entidade nova, um por `(agendamento, aluno)`, nota de 1 a 5 e comentário opcional. Só depois de `dataHora <= agora` e se o aluno não tiver cancelado aquela aula. Segundo feedback na mesma aula: `409`. Pode atualizar o próprio.

Perfil editável pelo aluno: foto, `notificacaoAtiva` e `tipoContato`. Nome, e-mail, CPF, nascimento, status, observação, limitação física e endereço continuam com a equipe.

## Pacote de aulas por especialidade

Cada aluno tem um saldo por especialidade. Exemplo: 10 aulas de pilates e 5 de fisioterapia. O número exibido é de aulas remanescentes.

- Administrador e secretaria definem o número livremente. O valor enviado substitui o saldo. Não é um incremento. Zero é válido. Negativo é rejeitado.
- O aluno só consulta o próprio saldo. Professor não edita.
- Aula dada abaixa 1 no pacote da especialidade daquela aula. Aula dada é a presença registrada como `PRESENTE` em `AgendamentoService.registrarPresencas`. `FALTA` e `PENDENTE` não abaixam.
- Aula cancelada não abaixa. Cancelar sem débito anterior não mexe no saldo. Se a aula já tinha sido debitada (`PRESENTE`) e depois o vínculo ou o agendamento for cancelado, ou a presença deixar de ser `PRESENTE`, devolva 1. O efeito líquido de uma aula cancelada é zero.
- Cada vínculo debita no máximo uma vez. Troca `PRESENTE` → `PRESENTE` não debita de novo.
- Sem pacote cadastrado para aquela especialidade, a presença é gravada e o saldo não muda. Não crie pacote automaticamente.
- Saldo já em zero não fica negativo e não deve ser marcado como debitado, para um estorno não inventar crédito.
- Agendar a aula não debita. O débito acontece quando a aula é dada.
- Se a especialidade do agendamento mudar depois de um débito, estorne na especialidade antiga e debite na nova somente os vínculos que continuam `PRESENTE`.
- Tabela sugerida `pacote_aula`, única por `(aluno_id, especialidade_id)`, campo `aulasRemanescentes`. Flag `creditoConsumido` em `AgendamentoAluno` para não debitar duas vezes.
- Hibernate está com `ddl-auto=update`. Não escreva migration manual se o restante do projeto também não escreve.

Consulta e edição:

- `GET /api/alunos/{alunoId}/pacotes` — administrador e secretaria consultam o aluno do path. Aluno autenticado consulta só o próprio id; outro id responde `403`.
- `PUT /api/alunos/{alunoId}/pacotes/{especialidadeId}` — corpo `{ "aulasRemanescentes": 10 }`. Somente administrador e secretaria. Cria o pacote se não existir. Aluno e professor recebem `403`.

Descreva no Swagger a regra: aula dada (`PRESENTE`) diminui o saldo; aula cancelada não diminui; o PUT grava o saldo absoluto.

## Demais endpoints do aluno

Base `/api/aluno`, autoridade `ALUNO`. Id do aluno sempre do token.

| Método | Caminho | Efeito | Resposta |
|---|---|---|---|
| GET | `/api/aluno/agenda` | Aulas futuras em que ele ainda está. Query opcional `de` e `ate`. Sem criação. | 200 |
| PATCH | `/api/aluno/agenda/{agendamentoId}/presenca` | Confirma que vai comparecer | 200, 400 `AULA_JA_REALIZADA`, 404 se a aula não é dele |
| DELETE | `/api/aluno/agenda/{agendamentoId}` | Cancela a participação, com a regra das 24 horas | 204 ou 400 `CANCELAMENTO_FORA_DO_PRAZO` |
| POST | `/api/aluno/agenda/{agendamentoId}/feedback` | Cria feedback de aula já iniciada | 201, 400 `AULA_AINDA_NAO_REALIZADA`, 409 se já existe |
| PATCH | `/api/aluno/agenda/{agendamentoId}/feedback` | Atualiza o próprio feedback | 200 ou 404 |
| GET | `/api/aluno/perfil` | Perfil do token | 200 |
| PATCH | `/api/aluno/perfil` | Só `notificacaoAtiva` e `tipoContato` | 200 |
| POST | `/api/aluno/perfil/foto` | Multipart, no padrão de `POST /api/professores/{id}/uploadFoto` | 200 com o caminho da foto |
| GET | `/api/aluno/dashboard/{qtdUltimosDias}` | Histórico, KPIs e gráficos. Período 1 a 365 | 200 ou 400 |
| GET | `/api/aluno/pacotes` | Atalho do próprio saldo, mesma regra do GET de pacotes | 200 |

Não crie `POST` de agendamento para esse perfil.

## Dashboard do aluno

Mesma janela do professor: de `hoje - N dias` até o fim de hoje. Aulas futuras ficam na agenda, não no gráfico.

`RespostaDashAlunoDTO`:

- `historico`: data/hora, especialidade, professor, sala e situação (`PRESENTE`, `FALTA`, `PENDENTE`, `CANCELADA`)
- `aulasPorDiaSemana`: reutilize `AgendamentoPorDiaDTO`
- `aulasPorEspecialidade`: reutilize `AulaPorEspecialidadeDTO`
- KPIs: aulas no período, presenças, faltas, cancelamentos, taxa de presença (`presenças / (presenças + faltas)`; cancelada e pendente ficam de fora), especialidade mais frequentada, dia da semana mais frequente, aulas remanescentes por especialidade

Não copie “alunos atendidos” nem “sessões ministradas” de `KPIsProfessorDTO`.

Para o histórico de cancelamento existir, não apague o fato da desistência. Ao remover o `AgendamentoAluno`, grave um `CancelamentoAula` com aluno, data/hora, especialidade, professor, sala e `canceladoEm`. Se o agendamento inteiro for excluído por ser o último aluno, o snapshot continua (não dependa de FK com cascade para `Agendamento`).

## Tools novas da IA

Somente `Role.ALUNO`. Nenhuma aceita `alunoId` vindo do modelo: usam `usuario.usuarioId()`.

- `minha_agenda` — próximas aulas do próprio aluno: horário, professor, sala, especialidade e se a presença já foi confirmada. Sem nomes de outros alunos.
- `especialidades_estudio` — nome de cada especialidade, professores que a ministram e salas em que ocorre. Sem ids internos de agendamento e sem dados de outros alunos. Não reutilize `listar_especialidades` para o aluno: essa tool é da equipe e devolve id para quem agenda.

Não crie tool de cancelar, agendar, editar perfil ou alterar pacote. Cancelamento, feedback, confirmação e pacote ficam na API.

No system prompt de `AgentService`, para o perfil `ALUNO`: consulta a própria agenda, as especialidades do estúdio e, se fizer sentido na conversa, as aulas remanescentes do próprio pacote (somente leitura, tool ou dado já carregado — se criar tool, ela também ignora `alunoId` e só lê o saldo). Não agenda, não cancela, não altera cadastro e não vê dados de outros alunos. Se não houver ferramenta, explica e não contorna.

`GET /api/ia/agente` hoje aceita qualquer autenticado. Com o aluno logado, o filtro de `FerramentaRegistry.definicoes(role)` é o que limita o que o modelo vê. Garanta que o aluno não receba tools de criar, editar, excluir, dashboard geral, dashboard de professor, observações ou busca de outros alunos.

## Testes mínimos

Service:

- agenda só devolve aulas futuras daquele aluno
- confirmar presença grava os dois campos; aula passada lança `AULA_JA_REALIZADA`; aula de outro aluno não é encontrada
- cancelar com 24 horas ou mais remove o vínculo, grava `CancelamentoAula` e notifica o professor se a notificação estiver ativa
- cancelar o último aluno também exclui o agendamento
- cancelar com menos de 24 horas, ou aula passada, lança `CANCELAMENTO_FORA_DO_PRAZO` e não altera nada
- feedback antes da aula lança `AULA_AINDA_NAO_REALIZADA`; segundo feedback conflita
- patch de perfil não altera nome, CPF nem status
- dashboard vazio devolve listas vazias e KPIs zerados; com dados, calcula taxa, especialidade, dia mais frequente e inclui cancelamento
- período fora de 1–365 é rejeitado
- PUT de pacote cria e também substitui o saldo
- `PRESENTE` debita 1 da especialidade correta; segundo registro não debita de novo
- `FALTA` não debita
- cancelar aula não debitada mantém o saldo; cancelar aula já debitada devolve 1
- saldo zero não fica negativo
- aluno não define pacote; consulta de outro aluno dá `403`
- ausência de pacote não impede o registro de presença

Controller:

- cada rota com a autoridade certa retorna o status esperado
- perfil errado recebe `403`; anônimo recebe `401`
- o `400` de cancelamento traz `codigoErro = CANCELAMENTO_FORA_DO_PRAZO`

Tools:

- `minha_agenda` ignora `alunoId` enviado pelo modelo
- `FerramentaRegistry.definicoes(Role.ALUNO)` não contém as seis tools que estavam abertas nem as tools de escrita da equipe

Auth:

- login de aluno emite JWT com role `ALUNO`
- o filtro autentica esse token sem exigir linha em `Funcionario`

## Ordem sugerida

1. Fechar as tools abertas e só então adicionar `Role.ALUNO` e o login do aluno.
2. Pacote, débito em `PRESENTE` e estorno no cancelamento.
3. Agenda, confirmação e cancelamento com 24 horas.
4. Feedback, perfil e foto.
5. Dashboard.
6. `minha_agenda` e `especialidades_estudio`, mais o trecho do system prompt.
