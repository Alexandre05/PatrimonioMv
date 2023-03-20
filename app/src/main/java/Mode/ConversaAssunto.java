package Mode;

import com.google.firebase.database.DatabaseReference;

import Helper.ConFirebase;

public class ConversaAssunto {

    private  String idRemetente;
    private String idDestinatario;
    private String Token;
    private String ultimaMensagem;
    private Usuario uruarioExibicao;
    private Usuario user;


    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        this.Token = token;
    }

    public Usuario getUser() {
        return user;
    }

    public void setUser(Usuario user) {
        this.user = user;
    }

    public Usuario getUruarioExibicao() {
        return uruarioExibicao;
    }

    public void setUruarioExibicao(Usuario uruarioExibicao) {
        this.uruarioExibicao = uruarioExibicao;
    }

    public  void salvar() {
        DatabaseReference databaseReference = ConFirebase.getFirebaseDatabase();
        DatabaseReference conversaRef = databaseReference.child("conversas");

        conversaRef
                .child(this.getIdRemetente())
                .child(this.getIdDestinatario())

                .setValue(this);




    }
    public void salvarConversaPrivadarUsuarioExibicaoOutro(){
        DatabaseReference ref = ConFirebase.getFirebaseDatabase();
        DatabaseReference conversaParearOutro = ref.child("conversas");
        user = ConFirebase.getDadosUsarioLogado();
        setUruarioExibicao(user);
        conversaParearOutro.child(getIdDestinatario())
                .child(getIdRemetente())



                .setValue(this);

    }




    public String getIdRemetente() {
        return idRemetente;
    }

    public void setIdRemetente(String idRemetente) {
        this.idRemetente = idRemetente;
    }

    public String getIdDestinatario() {
        return idDestinatario;
    }

    public void setIdDestinatario(String idDestinatario) {
        this.idDestinatario = idDestinatario;
    }

    public String getUltimaMensagem() {
        return ultimaMensagem;
    }

    public void setUltimaMensagem(String ultimaMensagem) {
        this.ultimaMensagem = ultimaMensagem;
    }



    public ConversaAssunto() {
    }
}

