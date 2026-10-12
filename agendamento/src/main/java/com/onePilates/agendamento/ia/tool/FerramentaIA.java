package com.onePilates.agendamento.ia.tool;

import com.onePilates.agendamento.model.enums.Role;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Uma ação que o agente pode pedir ao modelo para executar. */
public interface FerramentaIA {

    /** Nome único, como o modelo vai chamar a tool. */
    String nome();

    /** Quando usar a tool; é enviada ao modelo. */
    String descricao();

    /** JSON Schema dos parâmetros (type=object). */
    Map<String, Object> parametros();

    /** Perfis que podem usar a tool. Por padrão, todos (espelhe o @PreAuthorize do controller equivalente). */
    default Set<Role> rolesPermitidas() {
        return EnumSet.allOf(Role.class);
    }

    /** Executa e devolve algo serializável em JSON (Map, List, String...). */
    Object executar(Map<String, Object> argumentos, ContextoUsuario usuario);
}
