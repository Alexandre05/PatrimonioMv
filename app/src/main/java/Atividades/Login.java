package Atividades;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import Ajuda.ConFirebase;
import Modelos.Usuario;
import br.com.patrimoniomv.R;

public class Login extends AppCompatActivity {
    private Button botaoAcessar,cadastroNovo;
    private EditText campoEmail, campoSenha,RecuEmail;
    private Usuario usuario;



    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        inicioComponentes();

        autenticacao= ConFirebase.getReferenciaAutencicacao();

        botaoAcessar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String email = campoEmail.getText().toString();
                String senha = campoSenha.getText().toString();


                if (!email.isEmpty()){
                    if (!senha.isEmpty()){

                        usuario= new Usuario();
                        usuario.setEmail(email);
                        usuario.setSenha(senha);
                        validarLogin();
                    }else {

                        Toast.makeText(Login.this,
                                "Preencha  a senha!",
                                Toast.LENGTH_SHORT).show();
                    }

                }else {
                    Toast.makeText(Login.this,
                            "Preencha  o E-mail!",
                            Toast.LENGTH_SHORT).show();


                }

            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser usuarioAtual = autenticacao.getCurrentUser();
        if (usuarioAtual!=null){

            abrirTelaAni();

        }
    }

    public  void validarLogin() {
        autenticacao= ConFirebase.getReferenciaAutencicacao();

        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String userId = ConFirebase.getIdUsuario();
                    DatabaseReference userRef = ConFirebase.getFirebaseDatabase().getDatabase().getReference("usuarios").child(userId);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Usuario user = dataSnapshot.getValue(Usuario.class);
                            if (user != null && "AD".equals(user.getTipo())) {
                                abrirTelaAdmin();
                            } else {
                                abrirTelaAni();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(Login.this,
                                    "Erro ao verificar função do usuário.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    String erroExe = "";
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        erroExe = "Usuario não cadastrado";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        erroExe = "E-mail e senha não corresponde ao usuario";
                    } catch (Exception e) {
                        erroExe = " cadastrar usuário" + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(Login.this,
                            erroExe,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });



    }


    public void abrirTelaAdmin() {
        startActivity(new Intent(this, Admininistrar.class));
    }


    public void cadastroChamaAt(View v){

        startActivity(new Intent(this,Cadastrar.class));

        inicioComponentes();


    }
    public  void recuperarSenha(View view){
        startActivity(new Intent(this,RecuperarSenha.class));


    }

    public void abrirTelaAni(){

        startActivity(new Intent(this, MostraVistorias.class));
    }


    private void inicioComponentes(){

        campoEmail= findViewById(R.id.emialLog);
        campoSenha= findViewById(R.id.senhaLog);
        cadastroNovo=findViewById(R.id.btnCadastrar);
        botaoAcessar= findViewById(R.id.botaoEntrar);
        RecuEmail=findViewById(R.id.RecuEmail);
        //campoAcesso= findViewById(R.id.switchAcesso);

    }

}