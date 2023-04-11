package Mode;

public class Empresa {
    private String nome;
    private String telefone;
    private String cnpj;
    private String estado;
    private String cidade;
    private String tipo;
    public Empresa(String nome, String telefone, String cnpj, String estado, String cidade, String tipo) {
        this.nome = nome;
        this.telefone = telefone;
        this.cnpj = cnpj;
        this.estado = estado;
        this.cidade = cidade;
        this.tipo = tipo;
    }
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
