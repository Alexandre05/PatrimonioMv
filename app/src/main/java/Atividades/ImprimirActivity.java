package Atividades;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Helper.ConFirebase;
import Mode.ItensVistorias;
import br.com.patrimoniomv.R;

public class ImprimirActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int CREATE_FILE_REQUEST = 1;
    private List<ItensVistorias> anuncios;
    private Button buttonImprimir;
    private EditText editTextStartDate, editTextEndDate;
    private DatePicker startDatePicker, endDatePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imprimir);
        startDatePicker = findViewById(R.id.startDatePicker);
        endDatePicker = findViewById(R.id.endDatePicker);
        buttonImprimir = findViewById(R.id.button_imprimir);
        // Exemplo de chamada do método printByInspectorDate
        // Inicialize a lista de anúncios (adicione aqui os itens de vistoria conforme necessário)
        anuncios = new ArrayList<>();
        buttonImprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inspectorId = ConFirebase.getIdUsuario();
                String startDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                        startDatePicker.getYear(), startDatePicker.getMonth() + 1, startDatePicker.getDayOfMonth());
                String endDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                        endDatePicker.getYear(), endDatePicker.getMonth() + 1, endDatePicker.getDayOfMonth());
                printByInspectorDate(inspectorId, startDate, endDate);
                loadDataFromFirebase();
            }
        });
    }
    private void loadDataFromFirebase() {
        String inspectorId = ConFirebase.getIdUsuario();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("vistoriasConcluidas");
        databaseReference.orderByChild("campoIdUsuario").equalTo(inspectorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                anuncios.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ItensVistorias item = snapshot.getValue(ItensVistorias.class);
                    if (item != null) {
                        anuncios.add(item);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ImprimirActivity.this, "Erro ao carregar os dados: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Outros métodos da classe



    public void printByInspectorDate(String inspectorId, String startDate, String endDate) {
        List<ItensVistorias> filteredAnuncios = filterByInspectorDate(anuncios, inspectorId, startDate, endDate);
        createPdf(filteredAnuncios);
    }
    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "relatorio.pdf");
        startActivityForResult(intent, CREATE_FILE_REQUEST);
    }

    private void createPdf(List<ItensVistorias> anuncios) {
        if (anuncios.isEmpty()) {
            Toast.makeText(this, "Não há dados para gerar o PDF!", Toast.LENGTH_SHORT).show();
            return;
        }
        createFile();
    }

    private void checkWriteStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            createPdf(anuncios);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

    }
    private List<ItensVistorias> filterByInspectorDate(List<ItensVistorias> anuncios, String inspectorId, String startDate, String endDate) {
        List<ItensVistorias> filteredAnuncios = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date start;
        Date end;

        try {
            start = dateFormat.parse(startDate);
            end = dateFormat.parse(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao analisar as datas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return filteredAnuncios;
        }

        for (ItensVistorias anuncio : anuncios) {
            if (anuncio.getNomePerfilU().equals(inspectorId)) {
                try {
                    Date anuncioDate = dateFormat.parse(anuncio.getData());
                    if (anuncioDate != null && !anuncioDate.before(start) && !anuncioDate.after(end)) {
                        filteredAnuncios.add(anuncio);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        return filteredAnuncios;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                savePdfToFile(uri);
            }
        }
    }

    private void savePdfToFile(Uri uri) {
        Document document = new Document();
        try {
            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
            if (pfd != null) {
                FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());
                PdfWriter.getInstance(document, fos);
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
                PdfPTable table = new PdfPTable(6);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);

                // Adicionando cabeçalho da tabela
                table.addCell("Item");
                table.addCell("Nº Patrimonio");
                table.addCell("Ob.");
                table.addCell("Vistoriado.");
                table.addCell("Data Vistoria");
                table.addCell("Localização");

                // Adicionando dados à tabela
                for (ItensVistorias anuncio : anuncios) {
                    table.addCell(anuncio.getNomeItem());
                    table.addCell(anuncio.getPlaca());
                    table.addCell(anuncio.getOutrasInformacoes());
                    table.addCell(anuncio.getNomePerfilU());
                    table.addCell(anuncio.getData());
                    table.addCell(anuncio.getLocalizacao());
                }

                // Adicionando tabela ao documento
                document.add(table);

                document.close();

                fos.close();
                pfd.close();
                Toast.makeText(this, "PDF criado com sucesso!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao criar o PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}








