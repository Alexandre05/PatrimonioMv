package Atividades;

import static com.google.firebase.crashlytics.internal.Logger.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import Modelos.Vistoria;
import br.com.patrimoniomv.R;

public class ImprimirActivity extends AppCompatActivity {
    private FloatingActionButton floatingActionButtonImprimir;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int CREATE_FILE_REQUEST = 1;
    private List<Vistoria> anuncios;
    private Button buttonImprimir;
    private DatePicker startDatePicker, endDatePicker;
    private List<Vistoria> filteredAnuncios = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imprimir);
        startDatePicker = findViewById(R.id.startDatePicker);
        endDatePicker = findViewById(R.id.endDatePicker);
        buttonImprimir = findViewById(R.id.button_imprimir);
        anuncios = new ArrayList<>();
        loadDataFromFirebase();
        updateButtonStatus();
        buttonImprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("PDF", "Botão imprimir clicado");

                String startDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                        startDatePicker.getDayOfMonth(), startDatePicker.getMonth() + 1, startDatePicker.getYear());
                String endDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                        endDatePicker.getDayOfMonth(), endDatePicker.getMonth() + 1, endDatePicker.getYear());

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String inspectorId = user.getUid();
                    Log.d("Lista Imprimir", "ID" + inspectorId);
                    filteredAnuncios = filterByInspectorDate(anuncios, inspectorId, startDate, endDate);
                    Log.d("Lista Imprimir", "T" + anuncios);
                    if (!filteredAnuncios.isEmpty()) {
                        Log.d("PDF", "filteredAnuncios não está vazio"); // Adicione este log
                        checkWriteStoragePermission();
                        Log.d("PDF", "Chamando createFile() a partir do onClick");

                        createFile();


                    } else {
                        Log.d("PDF", "filteredAnuncios está vazio"); // Adicione este log
                        Toast.makeText(ImprimirActivity.this, "Não há dados para gerar o PDF!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("PDF", "Usuário não autenticado"); // Adicione este log
                    Toast.makeText(ImprimirActivity.this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void generatePDF() {
        String startDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                startDatePicker.getDayOfMonth(), startDatePicker.getMonth() + 1, startDatePicker.getYear());
        String endDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                endDatePicker.getDayOfMonth(), endDatePicker.getMonth() + 1, endDatePicker.getYear());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String inspectorId = user.getUid();
            Log.d("Lista Imprimir", "ID" + inspectorId);
            filteredAnuncios = filterByInspectorDate(anuncios, inspectorId, startDate, endDate);
            Log.d("Lista Imprimir", "T" + anuncios);
            if (!filteredAnuncios.isEmpty()) {
                createFile();
            } else {
                Toast.makeText(ImprimirActivity.this, "Não há dados para gerar o PDF!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ImprimirActivity.this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateButtonStatus() {
        if (anuncios.size() > 0) {
            Log.d("Lista","metodoUp"+anuncios);
            buttonImprimir.setEnabled(true);
        } else {
            buttonImprimir.setEnabled(false);
            Toast.makeText(ImprimirActivity.this, "Não há dados disponíveis para as datas selecionadas.", Toast.LENGTH_SHORT).show();
        }
    }
    private void loadDataFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String inspectorId = user.getUid();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("vistoriasConcluidas").child(inspectorId);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    anuncios.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Vistoria item = snapshot.getValue(Vistoria.class);
                        if (item != null) {
                            anuncios.add(item);
                            Log.d("Lista","Mensa"+anuncios);
                        }
                    }
                    updateButtonStatus(); // Adicione esta chamada

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ImprimirActivity.this, "Erro ao carregar os dados: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ImprimirActivity.this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
        }
    }



    private void createFile() {
        Log.d("PDF", "Chamando createFile()");
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "");
        startActivityForResult(intent, CREATE_FILE_REQUEST);


    }


    private boolean checkWriteStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permissão", "Permissão de escrita concedida.");
            return true;
        } else {
            return false;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                savePdfToFile(uri, filteredAnuncios);
                Log.d("PDF", "Arquivo PDF criado com sucesso: " + uri.toString());
            } else {
                Log.d("PDF", "URI nulo.");
            }
        } else {
            Log.d("PDF", "onActivityResult: requestCode: " + requestCode + ", resultCode: " + resultCode + ", data: " + data);
        }
    }



    public static List<Vistoria> filterByInspectorDate(List<Vistoria> anuncios, String inspectorId, String startDate, String endDate) {
        List<Vistoria> filteredAnuncios = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            Log.d("Filtro", "Data inicial: " + start + ", Data final: " + end);

            for (Vistoria anuncio : anuncios) {
                Date date = dateFormat.parse(anuncio.getData());
                Log.d("Filtro", "Anúncio: " + anuncio.getLocalizacao() + ", Data: " + date);

                boolean isInspectorMatch = anuncio.getIdInspector().equals(inspectorId);
                boolean isDateInRange = date.compareTo(start) >= 0 && date.compareTo(end) <= 0;

                if (isInspectorMatch) {
                   // Log.d("Filtro", "Anúncio corresponde ao inspetor: " + anuncio.getNomeItem() + ", Data: " + date);
                } else {
                    //Log.d("Filtro", "Anúncio não corresponde ao inspetor: " + anuncio.getNomeItem() + ", Data: " + date);
                }

                if (isDateInRange) {
                   // Log.d("Filtro", "Anúncio está no intervalo de datas: " + anuncio.getNomeItem() + ", Data: " + date);
                } else {
                    //Log.d("Filtro", "Anúncio não está no intervalo de datas: " + anuncio.getNomeItem() + ", Data: " + date);
                }

                if (isInspectorMatch && isDateInRange) {
                   // Log.d("Filtro", "Anúncio adicionado à lista filtrada: " + anuncio.getNomeItem() + ", Data: " + date);
                    filteredAnuncios.add(anuncio);
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return filteredAnuncios;
    }




    private void savePdfToFile(Uri uri ,List<Vistoria> filteredAnuncios) {
        Log.d("PDF","");
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
                Log.d("PDF", "Total de anúncios filtrados: " + filteredAnuncios.size());
                for (Vistoria anuncio : filteredAnuncios) {
                    table.addCell(anuncio.getLocalizacao());
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
        Log.d(TAG, "savePdfToFile: uri: " + uri); // ADICIONADO
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissão", "Permissão de escrita externa concedida.");
            } else {
                Log.d("Permissão", "Permissão de escrita externa negada.");
                Toast.makeText(this, "A permissão de escrita externa é necessária para gerar o PDF.", Toast.LENGTH_SHORT).show();
            }
        }
    }




}








