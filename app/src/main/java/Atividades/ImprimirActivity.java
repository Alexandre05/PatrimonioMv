package Atividades;

import static com.google.firebase.crashlytics.internal.Logger.TAG;

import androidx.annotation.NonNull;
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
import android.widget.Toast;

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

import Helper.ConFirebase;
import Mode.ItensVistorias;
import br.com.patrimoniomv.R;
import android.util.Log;

public class ImprimirActivity extends AppCompatActivity {
    private FloatingActionButton floatingActionButtonImprimir;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int CREATE_FILE_REQUEST = 1;
    private List<ItensVistorias> anuncios;
    private Button buttonImprimir;
    private DatePicker startDatePicker, endDatePicker;
    private List<ItensVistorias> filteredAnuncios = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imprimir);
        startDatePicker = findViewById(R.id.startDatePicker);
        endDatePicker = findViewById(R.id.endDatePicker);
        buttonImprimir = findViewById(R.id.button_imprimir);
        anuncios = new ArrayList<>();
        loadDataFromFirebase();
        onDataLoaded();

        // Exemplo de chamada do método printByInspectorDate
        // Inicialize a lista de anúncios (adicione aqui os itens de vistoria conforme necessário)

        buttonImprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkWriteStoragePermission();
                String startDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                        startDatePicker.getDayOfMonth(), startDatePicker.getMonth() + 1, startDatePicker.getYear());
                String endDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                        endDatePicker.getDayOfMonth(), endDatePicker.getMonth() + 1, endDatePicker.getYear());

                // Obter o inspectorId do usuário autenticado
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String inspectorId = user.getUid();
                    // Filtra os dados do Firebase
                    onDataLoaded(); // Adicione esta chamada
                    filteredAnuncios = filterByInspectorDate(anuncios, inspectorId, startDate, endDate);
                    createPdf(filteredAnuncios);
                } else {
                    Toast.makeText(ImprimirActivity.this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void onDataLoaded() {
        Log.i("onDataLoaded", "Início do método onDataLoaded");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String inspectorId = user.getUid();
            String startDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                    startDatePicker.getDayOfMonth(), startDatePicker.getMonth() + 1, startDatePicker.getYear());
            String endDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                    endDatePicker.getDayOfMonth(), endDatePicker.getMonth() + 1, endDatePicker.getYear());

            // Filtra os dados do Firebase
            filteredAnuncios = filterByInspectorDate(anuncios, inspectorId, startDate, endDate);

            if (filteredAnuncios.size() > 0) {
                // Habilite o botão "Salvar PDF"
                buttonImprimir.setEnabled(true);

                // Verifica a permissão de escrita e, em seguida, gera o relatório em PDF com os dados filtrados
                checkWriteStoragePermission();
            } else {
                // Desabilite o botão "Salvar PDF"
                buttonImprimir.setEnabled(false);

                // Exibir mensagem informando que não há dados
                Toast.makeText(ImprimirActivity.this, "Não há dados disponíveis para as datas selecionadas.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ImprimirActivity.this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
        }
        Log.i("onDataLoaded", "Fim do método onDataLoaded");
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
                        ItensVistorias item = snapshot.getValue(ItensVistorias.class);
                        if (item != null) {
                            anuncios.add(item);
                        }
                    }
                    //onDataLoaded();
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


    // Outros métodos da classe



    public void printByInspectorDate(String inspectorId, String startDate, String endDate) {
        List<ItensVistorias> filteredAnuncios = filterByInspectorDate(anuncios, inspectorId, startDate, endDate);
        createPdf(filteredAnuncios);
        Log.d(TAG, "printByInspectorDate: filteredAnuncios size: " + filteredAnuncios.size()); // ADICIONADO
    }


    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "relatorio.pdf");
        startActivityForResult(intent, CREATE_FILE_REQUEST);
    }

    private void createPdf(List<ItensVistorias> filteredAnuncios) {
        if (filteredAnuncios.isEmpty()) {
            Toast.makeText(this, "Não há dados para gerar o PDF!", Toast.LENGTH_SHORT).show();
            return;
        }
        createFile();
    }

    private void checkWriteStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            createPdf(filteredAnuncios);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                savePdfToFile(uri, filteredAnuncios);
            }
        }
    }


    private List<ItensVistorias> filterByInspectorDate(List<ItensVistorias> anuncios, String inspectorId, String startDate, String endDate) {
        Log.i("Filter", "Iniciando a filtragem");
        Log.i("Filter", "Inspector ID: " + inspectorId);
        Log.i("Filter", "Data inicial: " + startDate);
        Log.i("Filter", "Data final: " + endDate);
        filteredAnuncios.clear();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);

            for (ItensVistorias anuncio : anuncios) {
                if (anuncio.getIdAnuncio().equals(inspectorId)) {
                    Date date = dateFormat.parse(anuncio.getData());
                    Log.i("Filter", "Data do anúncio: " + anuncio.getData());
                    if (date.compareTo(start) >= 0 && date.compareTo(end) <= 0) {
                        Log.i("Filter", "Anúncio filtrado: " + anuncio.toString());
                        filteredAnuncios.add(anuncio);
                    } else {
                        Log.i("Filter", "Anúncio fora do intervalo");
                    }
                } else {
                    Log.i("Filter", "Anúncio não corresponde ao usuário");
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return filteredAnuncios;
    }




    private void savePdfToFile(Uri uri ,List<ItensVistorias> filteredAnuncios) {
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
                for (ItensVistorias anuncio : filteredAnuncios) {
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
        Log.d(TAG, "savePdfToFile: uri: " + uri); // ADICIONADO
    }



}








