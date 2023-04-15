package Modelos;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Ajuda.ConFirebase;

public class Usuario implements Serializable {

    private String idU;
    private String nome, idade, cpf, sexo,  sobrenome;;
    private String foto;
    private String endereco;
    private String email;
    private String senha;
    private String numeroPortaria;
    private String tipo;
    private String status;
    private boolean membroComissao;
    private String matricula;
    private String comissaoId;

    public String getIdade() {
        return idade;
    }

    public void setIdade(String idade) {
        this.idade = idade;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }



    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComissaoId() {
        return comissaoId;
    }

    public void setComissaoId(String comissaoId) {
        this.comissaoId = comissaoId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isMembroComissao() {
        return membroComissao;
    }

    public void setMembroComissao(boolean membroComissao) {
        this.membroComissao = membroComissao;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getNumeroPortaria() {
        return numeroPortaria;
    }

    public void setNumeroPortaria(String numeroPortaria) {
        this.numeroPortaria = numeroPortaria;
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

    public boolean isUserAdmin() {
        return "AD".equals(tipo);
    }

    public void salvarUsuario(String codigoEspecial) {
        String idU = ConFirebase.getIdUsuario();
        if (idU != null) {
            DatabaseReference refe = ConFirebase.getFirebaseDatabase();

            // Verifique se o código especial corresponde ao valor predefinido
            if (ConFirebase.CODIGO_ESPECIAL.equals(codigoEspecial)) {
                Log.d("SalvarUsuario", "Código especial válido. Definindo tipo como AD.");
                setTipo("AD");
            } else {
                Log.d("SalvarUsuario", "Código especial inválido. Definindo tipo como user.");
                setTipo("user");
            }

            refe.child("usuarios")
                    .child(idU)
                    .setValue(this);

            Log.d("SalvarUsuario", "Salvando usuário com ID: " + idU + " e tipo: " + getTipo());

            // Chame o novo método para inicializar o nó "vistoriasConcluidas" para este usuário
            inicializarVistoriasConcluidas(idU);

        } else {
            // Trate o caso em que o usuário não está autenticado
        }
    }

    private void inicializarVistoriasConcluidas(String userId) {
        DatabaseReference refe = ConFirebase.getFirebaseDatabase();
        refe.child("vistoriasConcluidas")
                .child(userId)
                .setValue(new ArrayList<>()); // Inicialize o nó com uma lista vazia
        Log.d("SalvarUsuario", "Inicializando o nó vistoriasConcluidas para o usuário: " + userId);
    }





    public void atualizar() {
        String indeUsu = ConFirebase.getIdentificarUsaurio();
        Log.d("AtualizarUsuario", "UID do usuário: " + indeUsu);
        DatabaseReference database = ConFirebase.getFirebaseDatabase();

        if (indeUsu != null) {
            DatabaseReference usuarioRef = database.child("usuarios")
                    .child(indeUsu);
            Map<String, Object> valoeresUsuario = converterParaMap();

            Log.d("AtualizarUsuario", "Chamando updateChildren com os seguintes valores: " + valoeresUsuario.toString());
            Log.d("AtualizarUsuario", "Usuário autenticado UID: " + indeUsu);

            usuarioRef.updateChildren(valoeresUsuario).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("AtualizarUsuario", "Dados atualizados com sucesso no Firebase.");
                    } else {
                        Log.e("AtualizarUsuario", "Erro ao atualizar dados no Firebase.", task.getException());
                    }
                }
            });
        } else {
            Log.e("Usuario", "Erro: usuário não autenticado.");
        }
    }




    @Exclude
    public Map<String, Object> converterParaMap() {
        HashMap<String, Object> usuaruiMap = new HashMap<>();
        usuaruiMap.put("email", getEmail());
        usuaruiMap.put("nome", getNome());
        usuaruiMap.put("foto", getFoto());
        usuaruiMap.put("tipo", getTipo());
        usuaruiMap.put("endereco", getEndereco());
        usuaruiMap.put("numeroPortaria", getNumeroPortaria());
        usuaruiMap.put("membroComissao", isMembroComissao());
        usuaruiMap.put("matricula", getMatricula());
        usuaruiMap.put("comissaoId",getComissaoId());
        usuaruiMap.put("token", getToken());
        // Adicione os novos campos ao HashMap
        usuaruiMap.put("sobrenome", getSobrenome());
        usuaruiMap.put("idade", getIdade());
        usuaruiMap.put("cpf", getCpf());
        usuaruiMap.put("sexo", getSexo());


        return usuaruiMap;
    }


    public static FirebaseUser getUsuarioAtual() {
        FirebaseAuth usuario = ConFirebase.getReferenciaAutencicacao();
        return usuario.getCurrentUser();
    }


    public static boolean AtualizarUsuario(String nome) {

        try {
            FirebaseUser user = getUsuarioAtual();
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
