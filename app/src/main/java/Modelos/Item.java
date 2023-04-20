package Modelos;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Item  implements Serializable {
    private String id;
    private String nome;
    private String placa;
    private List<String> fotos;
    private String observacao;
    private String fotoURL;

    public Item() {
    }

    @PropertyName("id")
    public String getId() {
        return id;
    }

    @PropertyName("id")
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("nome")
    public String getNome() {
        return nome;
    }

    @PropertyName("nome")
    public void setNome(String nome) {
        this.nome = nome;
    }

    @PropertyName("placa")
    public String getPlaca() {
        return placa;
    }

    @PropertyName("placa")
    public void setPlaca(String placa) {
        this.placa = placa;
    }

    @PropertyName("observacao")
    public String getObservacao() {
        return observacao;
    }

    @PropertyName("observacao")
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    @PropertyName("fotoURL")
    public String getFotoURL() {
        return fotoURL;
    }

    @PropertyName("fotoURL")
    public void setFotoURL(String fotoURL) {
        this.fotoURL = fotoURL;
    }

    @PropertyName("fotos")
    public List<String> getFotos() {
        return fotos;
    }

    @PropertyName("fotos")
    public void setFotos(List<String> fotos) {
        this.fotos = fotos;
    }
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("nome", nome);
        result.put("placa", placa);
        result.put("observacao", observacao);
        result.put("fotos", fotos);
        return result;
    }

}
