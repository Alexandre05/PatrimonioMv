package Mode;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import Helper.ConFirebase;

public class Usuario implements Serializable {

    private String idU;
    private String nome;
    private String foto;
    private String endereco;
    private String email;
    private String senha;
    private  String status;

    private boolean isAdmin;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    private String token;


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    @Exclude
    public String getIdU() {
        return idU;
    }

    public void setIdU(String idU) {
        this.idU = idU;
    }

    public Usuario() {


    }

    public void salvarUsuario() {
        String idU = ConFirebase.getIdUsuario();
        if (idU != null) {
            DatabaseReference refe = ConFirebase.getFirebaseDatabase();

            refe.child("usuarios")
                    .child(idU)
                    .setValue(this);

            Log.d("SalvarUsuario", "Salvando usuário com ID: " + idU);
        } else {
            // Trate o caso em que o usuário não está autenticado
        }
    }



    public void atualizar() {
        String indeUsu = ConFirebase.getIdentificarUsaurio();
        DatabaseReference database = ConFirebase.getFirebaseDatabase();
        DatabaseReference usuarioRef = database.child("usuarios")
                .child(indeUsu);
        Map<String, Object> valoeresUsuario = converterParaMap();

        usuarioRef.updateChildren(valoeresUsuario);


    }

    @Exclude
    public Map<String, Object> converterParaMap() {
        HashMap<String, Object> usuaruiMap = new HashMap<>();
        usuaruiMap.put("email", getEmail());
        usuaruiMap.put("nome", getNome());
        usuaruiMap.put("foto", getFoto());


        return usuaruiMap;


    }

    public static FirebaseUser getUsuarioAutal() {
        FirebaseAuth usuario = ConFirebase.getReferenciaAutencicacao();
        return usuario.getCurrentUser();


    }


    public static boolean AtualizarUsuario(String nome) {

        try {

            FirebaseUser user = getUsuarioAutal();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(nome)
                    .build();

            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        Log.d("Perfil", "Erro ao atualizar nome de perfil.");
                    }
                }
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }


}

