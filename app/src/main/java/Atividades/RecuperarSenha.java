package Atividades;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import Ajuda.ConFirebase;
import br.com.patrimoniomv.R;

public class RecuperarSenha extends AppCompatActivity {
    private FirebaseAuth autenticacao;
    private TextView RecuEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_recuperar_senha);
        autenticacao= ConFirebase.getReferenciaAutencicacao();
        RecuEmail=findViewById(R.id.RecuEmail);
    }

    public void reset(View view) {
        autenticacao.sendPasswordResetEmail(RecuEmail.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {




                        if (task.isSuccessful()) {
                            RecuEmail.setText("");
                            Toast.makeText(
                                    RecuperarSenha.this,
                                    "Recuperação de acesso iniciada. Olhe seu E-mail.",
                                    Toast.LENGTH_SHORT

                            ).show();
                        } else {
                            Toast.makeText(
                                    RecuperarSenha.this,
                                    "Falhou! Tente novamente",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                });

    }
    public void callReset(View view){
        Intent intent = new Intent( this, Login.class );
        startActivity(intent);
    }
}