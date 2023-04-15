package Modelos;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Comissao implements Serializable {

    private String id;
    private List<String> membros;
    private boolean encerrada;

    public Comissao() {
        this.membros = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getMembros() {
        return membros;
    }

    public void setMembros(List<String> membros) {
        this.membros = membros;
    }

    public boolean isEncerrada() {
        return encerrada;
    }

    public void setEncerrada(boolean encerrada) {
        this.encerrada = encerrada;
    }

    // Adicione métodos para criar, atualizar e encerrar comissões, bem como adicionar e remover membros.
}

