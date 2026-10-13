package com.onePilates.agendamento.ia.service;

import java.util.Map;

/** Texto amigável de cada ferramenta, mostrado ao usuário enquanto o agente trabalha. */
final class DescricaoAcoes {

    private static final Map<String, String> POR_TOOL = Map.ofEntries(
            Map.entry("buscar_alunos", "Buscando alunos"),
            Map.entry("listar_agendamentos", "Lendo as agendas"),
            Map.entry("listar_proximos_agendamentos", "Consultando as próximas aulas"),
            Map.entry("listar_professores_e_salas", "Consultando professores e salas"),
            Map.entry("listar_especialidades", "Consultando as especialidades"),
            Map.entry("ver_observacoes_aluno", "Lendo as observações do aluno"),
            Map.entry("atualizar_observacao_aluno", "Atualizando a observação do aluno"),
            Map.entry("atualizar_observacao_aula_aluno", "Atualizando a observação da aula"),
            Map.entry("criar_agendamento", "Criando o agendamento"),
            Map.entry("criar_agendamentos_recorrentes", "Criando os agendamentos recorrentes"),
            Map.entry("criar_agendamentos_diarios", "Criando os agendamentos diários"),
            Map.entry("estatisticas_alunos", "Calculando as estatísticas dos alunos"),
            Map.entry("quantidade_funcionarios", "Contando os funcionários"),
            Map.entry("editar_agendamento", "Editando o agendamento"),
            Map.entry("excluir_agendamentos", "Preparando/excluindo aulas"),
            Map.entry("minhas_ausencias", "Consultando as ausências"),
            Map.entry("dashboard_geral", "Montando o dashboard geral"),
            Map.entry("dashboard_professor", "Montando o dashboard do professor"));

    private DescricaoAcoes() {
    }

    static String da(String nomeTool) {
        return POR_TOOL.getOrDefault(nomeTool, "Executando " + nomeTool);
    }
}
