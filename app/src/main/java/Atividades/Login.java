package Atividades;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;



import Helper.ConFirebase;
import Mode.Usuario;
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
                if(task.isSuccessful()){


                        abrirTelaAni();










                    String erroExe="";
                    try {
                        throw task.getException();

                    }catch (FirebaseAuthInvalidUserException e) {
                        erroExe = "Usuario não cadastrado";
                    }catch (FirebaseAuthInvalidCredentialsException e){
                        erroExe="E-mail e senha não corresponde ao usuario";
                    }catch (Exception e){
                        erroExe =" cadastrar usuário" +e.getMessage();
                        e.printStackTrace();

                    }
                    Toast.makeText(Login.this,
                            erroExe,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


    }




    public void cadastroChamaAt(View v){

        startActivity(new Intent(this,Cadastrar.class));

        inicioComponentes();


    }
    public  void recuperarSenha(View view){
        startActivity(new Intent(this,RecuperarSenha.class));


    }

    public void abrirTelaAni(){

        startActivity(new Intent(this,Animais.class));
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