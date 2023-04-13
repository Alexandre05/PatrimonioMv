package Mode;

import java.util.ArrayList;
import java.util.List;

public class Vistoria {
    private List<ItensVistorias> itens;
    private String id;
    private String localizacao;
    private String data;

    public void setItens(List<ItensVistorias> itens) {
        this.itens = itens;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Vistoria() {
        itens = new ArrayList<>();
    }

    public void addItem(ItensVistorias item) {
        itens.add(item);
    }

    public List<ItensVistorias> getItens() {
        return itens;
    }

}
