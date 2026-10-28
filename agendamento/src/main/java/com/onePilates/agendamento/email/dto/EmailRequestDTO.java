package com.onePilates.agendamento.email.dto;

import com.onePilates.agendamento.model.enums.TipoEmail;
public class EmailRequestDTO {
    private TipoEmail typeEmail;
    private String destinatario;
    private Object payload;

    public EmailRequestDTO() {
    }

    public TipoEmail getTypeEmail() {
        return typeEmail;
    }

    public void setTypeEmail(TipoEmail typeEmail) {
        this.typeEmail = typeEmail;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
