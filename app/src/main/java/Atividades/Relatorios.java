package Atividades;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import Adapter.AdapterAnuncios;
import Helper.ConFirebase;
import Mode.ItensVistorias;
import br.com.patrimoniomv.R;

public class Relatorios extends AppCompatActivity {
    private EditText editTextLocation;
    private Button buttonSearch;
    private RecyclerView recyclerView;
    private List<ItensVistorias> anuncios = new ArrayList<>();
    private AdapterAnuncios adapterAnuncios;
    private DatabaseReference anunciosRef;
    private FirebaseUser currentUser;
    private EditText editTextLicensePlate;
    private RadioGroup radioGroupSearchCriteria;
    private RadioButton radioButtonLocation;
    private RadioButton radioButtonLicensePlate;
    private Button buttonGeneratePdf;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorios);

        editTextLicensePlate = findViewById(R.id.editText_licensePlate);
        radioGroupSearchCriteria = findViewById(R.id.radioGroup_searchCriteria);
        radioButtonLocation = findViewById(R.id.radioButton_location);
        editTextLocation = findViewById(R.id.editText_location);
        radioButtonLicensePlate = findViewById(R.id.radioButton_licensePlate);
        recyclerView = findViewById(R.id.recyclerView_results);
        buttonSearch = findViewById(R.id.button_search);

        anunciosRef = ConFirebase.getFirebaseDatabase().child("anunciosPu");
        Log.d("FIREBASE_STRUCTURE", "anunciosRef: " + anunciosRef);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapterAnuncios = new AdapterAnuncios(anuncios, this);
        recyclerView.setAdapter(adapterAnuncios);

        editTextLocation.setVisibility(View.GONE);
        radioButtonLicensePlate.setChecked(true);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        radioGroupSearchCriteria.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.radioButton_location) {
                    editTextLocation.setVisibility(View.VISIBLE);
                    editTextLicensePlate.setVisibility(View.GONE);
                } else if (i == R.id.radioButton_licensePlate) {
                    editTextLocation.setVisibility(View.GONE);
                    editTextLicensePlate.setVisibility(View.VISIBLE);
                }
            }
        });

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioGroup radioGroup = findViewById(R.id.radioGroup_searchCriteria);
                int selectedId = radioGroup.getCheckedRadioButtonId();

                if (selectedId == R.id.radioButton_location) {
                    String location = editTextLocation.getText().toString().trim().toUpperCase();
                    if (!location.isEmpty()) {
                        searchByCategory(location);
                    } else {
                        Toast.makeText(Relatorios.this, "Digite a localização", Toast.LENGTH_SHORT).show();
                    }
                } else if (selectedId == R.id.radioButton_licensePlate) {
                    String licensePlate = editTextLicensePlate.getText().toString().trim().toUpperCase();
                    if (!licensePlate.isEmpty()) {
                        searchByLicensePlate(licensePlate);
                    } else {
                        Toast.makeText(Relatorios.this, "Digite o número da placa", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        buttonGeneratePdf = findViewById(R.id.button_generate_pdf);

        buttonGeneratePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPdf(anuncios);
            }
        });


    }



    public void searchByCategory(String category) {
        ItensVistorias.recuperarAnunciosPorCategoria(category).addOnCompleteListener(new OnCompleteListener<List<ItensVistorias>>() {
            @Override
            public void onComplete(@NonNull Task<List<ItensVistorias>> task) {
                if (task.isSuccessful()) {
                    anuncios.clear();
                    anuncios.addAll(task.getResult());
                    adapterAnuncios.notifyDataSetChanged();

                    // Adicionando log para verificar os resultados da busca
                    Log.d("SEARCH_RESULTS", "Número de resultados: " + anuncios.size());
                } else {
                    // Caso ocorra algum erro na busca
                    Log.e("SEARCH_ERROR", "Erro na busca: " + task.getException().getMessage()); // Log adicional
                }
            }
        });
    }

    public void searchByLicensePlate(String licensePlate) {
        ItensVistorias.recuperarAnunciosPorPlaca(licensePlate).addOnCompleteListener(new OnCompleteListener<List<ItensVistorias>>() {
            @Override
            public void onComplete(@NonNull Task<List<ItensVistorias>> task) {
                if (task.isSuccessful()) {
                    anuncios.clear();
                    anuncios.addAll(task.getResult());
                    adapterAnuncios.notifyDataSetChanged();

                    // Adicionando log para verificar os resultados da busca
                    Log.d("SEARCH_RESULTS", "Número de resultados: " + anuncios.size());
                } else {
                    // Caso ocorra algum erro na busca
                    Log.e("SEARCH_ERROR", "Erro na busca: " + task.getException().getMessage());
                    Toast.makeText(Relatorios.this, "Erro na busca: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createPdf(List<ItensVistorias> anuncios) {
        if (anuncios.isEmpty()) {
            Toast.makeText(this, "Não há dados para gerar o PDF!", Toast.LENGTH_SHORT).show();
            return;
        }
        Document document = new Document();
        try {
            String fileName = "relatorio.pdf";
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);
            OutputStream outputStream = new FileOutputStream(file);
            PdfWriter.getInstance(document, outputStream);
            document.open();




            // Adicionando título e subtítulo
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Paragraph title = new Paragraph("Relatório de Vistoria de Patrimônio da Comissão", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph subtitle = new Paragraph("Detalhes da Vistoria", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(10f);
            document.add(subtitle);
            // Criação da tabela
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Adicionando cabeçalho da tabela
            table.addCell("Item");
            table.addCell("Nº Patrimonio");
            table.addCell("Ob.");
            table.addCell("Vistoriado.");
            table.addCell("Data Vistoria");

            // Adicionando dados à tabela
            for (ItensVistorias anuncio : anuncios) {
                table.addCell(anuncio.getNomeItem());
                table.addCell(anuncio.getPlaca());
                table.addCell(anuncio.getOutrasInformacoes());
                table.addCell(anuncio.getNomePerfilU());
                table.addCell(anuncio.getData());
            }

            // Adicionando tabela ao documento
            document.add(table);

            document.close();
            Toast.makeText(this, "PDF criado com sucesso!", Toast.LENGTH_SHORT).show();
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao criar o PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // storage-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }





}
