package Modelos;

public class Notificacao {
    private String title;
    private String body;
    private String mensagem;

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Notificacao(String title, String body) {
        this.title = title;
        this.body = body;

    }
}
