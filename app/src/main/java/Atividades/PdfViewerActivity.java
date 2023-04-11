package Atividades;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import br.com.patrimoniomv.R;

public class PdfViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);
        Intent intent = getIntent();
        Uri pdfUri = intent.getData();

        if (pdfUri != null) {
            // Exibir o PDF usando a biblioteca ou método de visualização de PDF de sua escolha
        } else {
            // Mostrar mensagem de erro, pois o URI do PDF não foi fornecido
        }
    }
}