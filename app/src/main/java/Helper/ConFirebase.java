package Helper;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import Mode.Usuario;

public class ConFirebase {

    public static DatabaseReference referenciaFarebase;
    private   static FirebaseAuth referenciaAutencicacao;
    public static StorageReference referenciaStorage;
    public static final String CODIGO_ESPECIAL = "123";
    public static String getIdUsuario() {
        // pega o id que foi criando ao cadastrar o usuario
        FirebaseAuth aut = getReferenciaAutencicacao();
        FirebaseUser currentUser = aut.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }
        return null;
    }

    public  static  DatabaseReference getFirebaseDatabase(){

        if (referenciaFarebase== null){
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            referenciaFarebase= FirebaseDatabase.getInstance().getReference();





        }
        return referenciaFarebase;
    }


    public static FirebaseAuth getReferenciaAutencicacao(){

        if (referenciaAutencicacao == null) {
            referenciaAutencicacao= FirebaseAuth.getInstance();

        }
        return  referenciaAutencicacao;
    }
// retorna a intancia do firebaseStore
    public  static  StorageReference getFirebaseStorage(){
        if(  referenciaStorage==null){

            referenciaStorage= FirebaseStorage.getInstance().getReference();


        }
        return referenciaStorage;


    }
    public  static  String getIdentificarUsaurio(){
        FirebaseAuth usuario =  ConFirebase.getReferenciaAutencicacao();

        String email =usuario.getCurrentUser().getEmail();
        String identifcaUsuario = Base64Custon.codificarBase64(email);
        return  identifcaUsuario;

    }
    public  static FirebaseUser getUsuarioAtaul(){
        FirebaseAuth usuario = ConFirebase.getReferenciaAutencicacao();
        //String email = usuario.getCurrentUser().getEmail();

        return  usuario.getCurrentUser();
    }

    public  static boolean  AtualizarFotoUsuario(Uri url){

        try {

            FirebaseUser user = getUsuarioAtaul();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()

                    .setPhotoUri(url)
                    .build();

            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()){
                        Log.d("Perfil"," Erro");


                    }
                }
            });

        }catch (Exception e){
            e.printStackTrace();
            return  false;


        }

        return  true;




    }

    public  static boolean  AtualizarNomeUsuario(String nome){

        try {

            FirebaseUser user = getUsuarioAtaul();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()

                    .setDisplayName(nome)
                    .build();

            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()){
                        Log.d("Perfil"," Erro ao atualizar nome usuario");


                    }
                }
            });

        }catch (Exception e){
            e.printStackTrace();
            return  false;


        }

        return  true;




    }

    public  static Usuario getDadosUsarioLogado(){

        FirebaseUser firebaseuser =getUsuarioAtaul();
        Usuario usario = new Usuario();
        usario.setEmail(firebaseuser.getEmail());
        usario.setNome(firebaseuser.getDisplayName());
        if( firebaseuser.getPhotoUrl()==null){
            usario.setFoto("");
        }else {
            usario.setFoto(firebaseuser.getPhotoUrl().toString());

        }
        return usario;

    }

}

