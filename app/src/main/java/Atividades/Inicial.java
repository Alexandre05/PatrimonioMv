package Atividades;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import br.com.patrimoniomv.R;

public class Inicial extends AppCompatActivity {
    private static final int SPLASH_SCREEN_DELAY = 3000; // 3 segundos de atraso

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicial);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Verificar se é a primeira vez que o aplicativo está sendo executado
                SharedPreferences sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
                boolean isFirstRun = sharedPreferences.getBoolean("is_first_run", true);

                if (isFirstRun) {
                    // Redirecionar para a tela de inserção de informações da empresa
                    startActivity(new Intent(Inicial.this, MostraVistorias.class));
                } else {
                    // Redirecionar para a tela principal
                    startActivity(new Intent(Inicial.this, MostraVistorias.class));
                }

                // Encerrar a Activity da tela de boas-vindas
                finish();
            }
        }, SPLASH_SCREEN_DELAY);
    }
}
