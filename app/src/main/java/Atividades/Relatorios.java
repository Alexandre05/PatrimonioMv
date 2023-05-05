package Atividades;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Adaptadores.AdapterVistorias;
import Adaptadores.VistoriaAdapter;
import Adaptadores.VistoriaAndamentoAdapter;
import Modelos.Item;
import Modelos.Vistoria;
import br.com.patrimoniomv.R;

public class Relatorios extends AppCompatActivity {


    private EditText editTextLocation, editTextEndDate;
    private Button buttonSearch, btn_imprimir;
    private List<Vistoria> currentSearchResults = new ArrayList<>();

    private RecyclerView recyclerView;
    private List<Vistoria> vistoriasList;
    private VistoriaAndamentoAdapter vistoriaAndamentoAdapter;

    private FirebaseUser currentUser;
    private EditText editTextLicensePlate, editTextStartDate;
    private RadioGroup radioGroupSearchCriteria;
    private RadioButton radioButtonLocation;
    private RadioButton radioButtonLicensePlate;
    private Vistoria selectedItem;
    private Button buttonGeneratePdf, buttonGenerateQrCode;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 11;
    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int CREATE_FILE_REQUEST = 1;
    private static final int SCAN_QR_REQUEST_CODE = 100;

    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final String CHANNEL_ID = "PDF_NOTIFICATION_CHANNEL";

    private static final int REQUEST_CODE_SCAN_QR_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //showNotification("Teste de notificação", "Esta é uma notificação de teste", null);

        setContentView(R.layout.activity_relatorios);

        buttonGeneratePdf = findViewById(R.id.button_generate_pdf);
        editTextLicensePlate = findViewById(R.id.editText_licensePlate);
        radioGroupSearchCriteria = findViewById(R.id.radioGroup_searchCriteria);
        radioButtonLocation = findViewById(R.id.radioButton_location);
        editTextLocation = findViewById(R.id.editText_location);
        radioButtonLicensePlate = findViewById(R.id.radioButton_licensePlate);
        recyclerView = findViewById(R.id.recyclerView_results);
        buttonSearch = findViewById(R.id.button_search);
        editTextStartDate = findViewById(R.id.editText_startDate);
        editTextEndDate = findViewById(R.id.editText_endDate);
        buttonGenerateQrCode = findViewById(R.id.buttonGenerateQrCode);
        DatabaseReference vistoriasConcluidasRef = FirebaseDatabase.getInstance().getReference("vistoriasConcluidas");
        Log.d("FIREBASE_STRUCTURE", "anunciosRef: " + vistoriasConcluidasRef);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        vistoriaAndamentoAdapter = new VistoriaAndamentoAdapter(this, vistoriasList);

        recyclerView.setAdapter(vistoriaAndamentoAdapter);
        editTextLocation.setVisibility(View.GONE);
        createNotificationChannel();
        vistoriasList= new ArrayList<>();
        radioButtonLicensePlate.setChecked(true);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                editTextStartDate.setText(dateFormat.format(calendar.getTime()));
            }
        };

        final DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                editTextEndDate.setText(dateFormat.format(calendar.getTime()));
            }
        };
        // Configure o DatePickerDialog para ser exibido quando o usuário clicar nos EditTexts
        editTextStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(Relatorios.this, startDateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        editTextEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(Relatorios.this, endDateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        radioGroupSearchCriteria.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.radioButton_location) {
                    editTextLocation.setVisibility(View.VISIBLE);
                    editTextLicensePlate.setVisibility(View.GONE);
                    editTextStartDate.setVisibility(View.VISIBLE);
                    editTextEndDate.setVisibility(View.VISIBLE);
                } else if (i == R.id.radioButton_licensePlate) {
                    editTextLocation.setVisibility(View.GONE);
                    editTextLicensePlate.setVisibility(View.VISIBLE);
                    editTextStartDate.setVisibility(View.GONE);
                    editTextEndDate.setVisibility(View.GONE);
                }
            }
        });

        // botão onde faz a busca.
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioGroup radioGroup = findViewById(R.id.radioGroup_searchCriteria);

                int selectedId = radioGroup.getCheckedRadioButtonId();
                Log.d("ButtonSearch", "onClick: selectedId=" + selectedId);
                if (selectedId == R.id.radioButton_location) {
                    Log.d("ButtonSearch", "onClick: radioButton_location selected");
                    handleLocationSearch();
                } else if (selectedId == R.id.radioButton_licensePlate) {
                    Log.d("ButtonSearch", "onClick: radioButton_licensePlate selected");
                    handleLicensePlateSearch();
                }
            }

            private void handleLocationSearch() {
                Log.d("handleLocationSearch", "handleLocationSearch() called");
                String location = editTextLocation.getText().toString().trim().toUpperCase();
                String startDate = editTextStartDate.getText().toString();
                String endDate = editTextEndDate.getText().toString();
                Log.d("SearchResults", "onClick: location=" + location + ", startDate=" + startDate + ", endDate=" + endDate);

                if (!location.isEmpty()) {
                    fetchDataAndFilterByLocation(location, startDate.isEmpty() ? null : startDate, endDate.isEmpty() ? null : endDate);
                } else {
                    Toast.makeText(Relatorios.this, "Digite a localização", Toast.LENGTH_SHORT).show();
                }
            }


            private void fetchDataByLicensePlate(String inspectorId, String licensePlate, String startDate, String endDate, SearchCallback callback) {
                if (startDate.isEmpty() || endDate.isEmpty()) {
                    searchByLicensePlate(inspectorId, licensePlate, callback);
                } else {
                    searchByLicensePlateAndDate(inspectorId, licensePlate, startDate, endDate, callback);
                }
            }
            private void fetchDataAndFilterByLocation(String location, @Nullable String startDate, @Nullable String endDate) {
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                Query query = rootRef.child("vistoriasConcluidas");

                query.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Vistoria> filteredResults = new ArrayList<>();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Vistoria vistoria = snapshot.getValue(Vistoria.class);

                            if (vistoria != null && vistoria.getLocalizacao() != null) {
                                boolean locationMatches = vistoria.getLocalizacao().toUpperCase().contains(location.toUpperCase());
                                boolean dateMatches = true;

                                if (startDate != null && endDate != null) {
                                    try {
                                        Date vistoriaDate = sdf.parse(vistoria.getData());
                                        Date start = sdf.parse(startDate);
                                        Date end = sdf.parse(endDate);

                                        // Verificar se a data da vistoria está dentro do intervalo especificado
                                        dateMatches = vistoriaDate.compareTo(start) >= 0 && vistoriaDate.compareTo(end) <= 0;

                                        // Se a data estiver fora do intervalo, ignore a vistoria
                                        if (!dateMatches) {
                                            continue;
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        Log.e("fetchDataAndFilterByLocation", "Erro ao analisar datas: " + e.getMessage());
                                    }
                                }

                                if (locationMatches) {
                                    Log.d("fetchDataAndFilterByLocation", "Vistoria filtrada: " + vistoria.getLocalizacao());

                                    // Extrair todos os itens para esta vistoria
                                    Map<String, Item> itensMap = new HashMap<>();
                                    DataSnapshot itensSnapshot = snapshot.child("itensMap");
                                    for (DataSnapshot itemSnapshot : itensSnapshot.getChildren()) {
                                        Item item = itemSnapshot.getValue(Item.class);
                                        itensMap.put(itemSnapshot.getKey(), item);
                                    }

                                    // Adicionar os itens extraídos à instância de Vistoria
                                    vistoria.setItensMap(itensMap);

                                    filteredResults.add(vistoria);
                                }
                            }
                        }

                        // Verificar se não há resultados para o filtro aplicado
                        if (filteredResults.isEmpty()) {
                            Toast.makeText(Relatorios.this, "Nenhum resultado encontrado", Toast.LENGTH_SHORT).show();
                        }

                        // Atualize a lista de vistorias e notifique o adaptador
                        updateRecyclerView(filteredResults);

                        // Atualize a variável currentSearchResults
                        currentSearchResults = filteredResults;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FirebaseError", "Erro ao buscar dados: " + databaseError.getMessage());
                    }
                });
            }


            private void handleLicensePlateSearch() {
                Log.d("lidarComBuscaDePlaca", "handleLicensePlateSearch() called");
                String inspectorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String licensePlate = editTextLicensePlate.getText().toString().trim().toUpperCase();
                String vistoriaStartDate = editTextStartDate.getText().toString();
                String vistoriaEndDate = editTextEndDate.getText().toString();
                Log.d("SearchResults", "onClick: licensePlate=" + licensePlate + ", vistoriaStartDate=" + vistoriaStartDate + ", vistoriaEndDate=" + vistoriaEndDate);

                SearchCallback searchCallback = new SearchCallback() {
                    @Override
                    public void onSearchCompleted(List<Vistoria> searchResults) {
                        updateRecyclerView(searchResults);
                        currentSearchResults = searchResults;
                    }

                    @Override
                    public void onSearchFailed(String errorMessage) {
                        // Trate o erro aqui
                    }
                };

                if (!licensePlate.isEmpty()) {
                    fetchDataByLicensePlate(inspectorId, licensePlate, vistoriaStartDate, vistoriaEndDate, searchCallback);
                } else {
                    if (!vistoriaStartDate.isEmpty() && !vistoriaEndDate.isEmpty()) {

                    } else {
                        Toast.makeText(Relatorios.this, "Por favor, preencha as datas para buscar todos os dados", Toast.LENGTH_SHORT).show();
                    }
                }
            }


        });


// metodo gerar QRC

        buttonGenerateQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("QRCode", "Botão Gerar QR Code clicado");
                if (!currentSearchResults.isEmpty()) {
                    String searchResultsData = itemListToJson(currentSearchResults);
                    Log.d("QRCode", "Resultados da pesquisa não estão vazios");
                    // Gere o QR Code com as informações dos resultados da pesquisa
                    Bitmap qrCodeBitmap = gerarteQRCode(searchResultsData, currentSearchResults);

                    Log.d("QRCode", "Generated QR Code bitmap: " + qrCodeBitmap);

                    // Verifique a permissão de armazenamento
                    if (ContextCompat.checkSelfPermission(Relatorios.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        // Salve o QR Code no dispositivo
                        String filePath = saveQRCodeToStorage(qrCodeBitmap);

                        Log.d("QRCode", "Saved QR Code to file path: " + filePath);

                        // Compartilhe o QR Code
                        showQRCodeDialog(qrCodeBitmap);
                    } else {
                        // Solicite a permissão de armazenamento
                        ActivityCompat.requestPermissions(Relatorios.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                    }
                } else {
                    Log.d("QRCode", "Resultados da pesquisa estão vazios");
                    Toast.makeText(Relatorios.this, "Realize uma pesquisa primeiro", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // metodo gerar PDF
        buttonGeneratePdf.setOnClickListener(view -> {
            Log.d("PDF_CLICK", "Botão Gerar PDF");
            String startDate = "01/01/2023";
            String endDate = "31/12/2023";

            List<Vistoria> filteredVistorias = filterByDate(vistoriasList, startDate, endDate);
            if (ContextCompat.checkSelfPermission(Relatorios.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d("PDF_CLICK", "Permissão concedida, chamando createPdf");
                createPdf(filteredVistorias);
            } else {
                Log.d("PDF_CLICK", "Permissão não concedida, solicitando permissão");
                ActivityCompat.requestPermissions(Relatorios.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            }
        });
    }




    private void searchByLicensePlate(String inspectorId, String licensePlate, SearchCallback callback) {
        searchByLicensePlateAndDate(inspectorId, licensePlate, "", "", callback);
    }


    private void searchByLicensePlateAndDate(String inspectorId, String licensePlate, String vistoriaStartDate, String vistoriaEndDate, SearchCallback callback) {
        DatabaseReference vistoriasConcluidasRef = FirebaseDatabase.getInstance().getReference("vistoriasConcluidas");

        Query query;
        if (vistoriaStartDate.isEmpty() || vistoriaEndDate.isEmpty()) {
            query = vistoriasConcluidasRef; // Buscará todos os dados
        } else {
            query = vistoriasConcluidasRef.orderByChild("data").startAt(vistoriaStartDate).endAt(vistoriaEndDate);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Vistoria> vistorias = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Vistoria vistoria = childSnapshot.getValue(Vistoria.class);
                    if (vistoria != null && vistoria.getItensMap() != null) {
                        for (String itemId : vistoria.getItensMap().keySet()) {
                            Item item = vistoria.getItensMap().get(itemId);
                            if (item != null && item.getPlaca().equals(licensePlate)) {
                                vistorias.add(vistoria);
                                break;
                            }
                        }
                    }
                }
                callback.onSearchCompleted(vistorias);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onSearchFailed(databaseError.getMessage());
            }
        });
    }


    private void updateRecyclerView(List<Vistoria> searchResults) {
        vistoriasList.clear();
        vistoriaAndamentoAdapter.clear();

        vistoriasList.addAll(searchResults);
        vistoriaAndamentoAdapter.addAll(vistoriasList);

        Log.d("SearchResults", "updateRecyclerView: searchResults=" + searchResults);
        Log.d("SearchResults", "updateRecyclerView: vistoriasList=" + vistoriasList);
        vistoriaAndamentoAdapter.notifyDataSetChanged();
    }


    private List<Vistoria> filterByDate(List<Vistoria> anuncios, String startDate, String endDate) {
        List<Vistoria> filteredAnuncios = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            for (Vistoria vistoria : anuncios) {
                Date anuncioDate = sdf.parse(vistoria.getData());

                if ((anuncioDate.equals(start) || anuncioDate.after(start)) && (anuncioDate.equals(end) || anuncioDate.before(end))) {
                    filteredAnuncios.add(vistoria);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return filteredAnuncios;
    }

    private void fetchDataAndFilterByLocationAndDate(String location, String startDate, String endDate) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            Query query = rootRef.child("vistoriasConcluidas");

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<Vistoria> filteredResults = new ArrayList<>();

                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot snapshot : userSnapshot.getChildren()) {
                            Vistoria item = snapshot.getValue(Vistoria.class);

                            if (item != null && item.getData() != null && item.getLocalizacao() != null) {
                                try {
                                    Date itemDate = sdf.parse(item.getData());
                                    // Verifique se a localização contém a substring digitada pelo usuário (ignorando a diferença entre maiúsculas e minúsculas)
                                    if (itemDate != null && (itemDate.after(start) || itemDate.equals(start)) && (itemDate.before(end) || itemDate.equals(end)) && item.getLocalizacao().toUpperCase().contains(location.toUpperCase())) {
                                        filteredResults.add(item);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    // Atualize a lista de vistorias e notifique o adaptador
                    updateRecyclerView(filteredResults);

                    // Atualize a variável currentSearchResults
                    currentSearchResults = filteredResults;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FirebaseError", "Erro ao buscar dados: " + databaseError.getMessage());
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }



    // fim do oncrete
    private void fetchDataAndFilterByLocation(String location) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        Query query = rootRef.child("vistoriasConcluidas");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Vistoria> filteredResults = new ArrayList<>();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot snapshot : userSnapshot.getChildren()) {
                        Vistoria item = snapshot.getValue(Vistoria.class);

                        if (item != null && item.getLocalizacao() != null) {
                            // Verifique se a localização contém a substring digitada pelo usuário (ignorando a diferença entre maiúsculas e minúsculas)
                            if (item.getLocalizacao().toUpperCase().contains(location.toUpperCase())) {
                                filteredResults.add(item);
                            }
                        }
                    }
                }

                // Atualize a lista de vistorias e notifique o adaptador
                updateRecyclerView(filteredResults);

                // Atualize a variável currentSearchResults
                currentSearchResults = filteredResults;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Erro ao buscar dados: " + databaseError.getMessage());
            }
        });
    }


    private String itemListToJson(List<Vistoria> itemList) {
        Gson gson = new Gson();
        String json = gson.toJson(itemList);
        json = json.replace("\\u003d", "=").replace("\\u0026", "&");
        return json;
    }


    private Bitmap gerarteQRCode(String data, List<Vistoria> itemList) {
        Log.d("QRCode", "Generating QR Code with data: " + data + " and item list: " + itemList);

        Gson gson = new Gson();
        String json = gson.toJson(itemList);
        json = json.replace("\\u003d", "=").replace("\\u0026", "&");
        data = data.replace("\\u003d", "=").replace("\\u0026", "&");

        // Codifique a lista de itens como um parâmetro de URL
        String encodedJson = Uri.encode(json);

        // Substitua a URL do esquema do aplicativo pela URL da sua página web
        String webUrl = "https://ppmv-78b65.web.app" + encodedJson;

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(webUrl, BarcodeFormat.QR_CODE, 200, 200);
            Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
            for (int x = 0; x < 200; x++) {
                for (int y = 0; y < 200; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            Log.d("QRCode", "QR Code generated successfully");
            String action = "com.google.zxing.client.android.SCAN";
            Intent intent = new Intent(action);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            intent.putExtra("SAVE_HISTORY", false);
            intent.putExtra("RESULT_DISPLAY_DURATION_MS", 0L);
            intent.putExtra("PROMPT_MESSAGE", "Aponte a câmera para o QR Code para abrir a página da web");
            startActivityForResult(intent, 0);
            return bitmap;
        } catch (WriterException e) {
            Log.e("QRCode", "Error generating QR Code", e);
        }
        return null;
    }


    private void showQRCodeDialog(Bitmap qrCodeBitmap) {
        Log.d("QRCode", "Showing QR Code dialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_qr_code, null);
        builder.setView(dialogView);

        ImageView imageViewQRCode = dialogView.findViewById(R.id.imageView_qr_code);
        Button buttonSaveQRCode = dialogView.findViewById(R.id.button_save_qr_code);
        Button buttonPrintQRCode = dialogView.findViewById(R.id.button_print_qr_code);

        imageViewQRCode.setImageBitmap(qrCodeBitmap);

        final AlertDialog qrCodeDialog = builder.create();

        buttonSaveQRCode.setOnClickListener(view -> {
            Log.d("QRCode", "Botão Gerar QR Code clicado");
            if (ContextCompat.checkSelfPermission(Relatorios.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // Salve o QR Code no dispositivo
                String filePath = saveQRCodeToStorage(qrCodeBitmap);

                // Compartilhe o QR Code
                shareQRCode(filePath);

                // Feche o diálogo
                qrCodeDialog.dismiss();
            } else {
                // Solicite a permissão de armazenamento
                ActivityCompat.requestPermissions(Relatorios.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            }
        });

        buttonPrintQRCode.setOnClickListener(view -> {
            // Implemente a função para imprimir o QR Code
            printQRCode(qrCodeBitmap);

            // Feche o diálogo
            qrCodeDialog.dismiss();
        });

        qrCodeDialog.show();
    }


    private void printQRCode(Bitmap qrCodeBitmap) {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

        PrintDocumentAdapter printDocumentAdapter = new PrintDocumentAdapter() {
            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
                if (cancellationSignal.isCanceled()) {
                    callback.onLayoutCancelled();
                } else {
                    PrintDocumentInfo.Builder builder = new PrintDocumentInfo
                            .Builder("qr_code_print_job")
                            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .setPageCount(1);

                    PrintDocumentInfo info = builder.build();
                    callback.onLayoutFinished(info, true);
                }
            }


            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
                PdfDocument document = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(qrCodeBitmap.getWidth(), qrCodeBitmap.getHeight(), 1).create();
                PdfDocument.Page page = document.startPage(pageInfo);

                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();
                canvas.drawBitmap(qrCodeBitmap, 0, 0, paint);

                document.finishPage(page);

                try {
                    OutputStream out = new FileOutputStream(destination.getFileDescriptor());
                    document.writeTo(out);
                    callback.onWriteFinished(new PageRange[]{new PageRange(0, 0)});
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onWriteFailed(e.getMessage());
                } finally {
                    document.close();
                }
            }
        };

        String jobName = getString(R.string.app_name) + " Document";
        printManager.print(jobName, printDocumentAdapter, null);
    }






    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "PDF Notifications";
            String description = "Notificações relacionadas a PDF";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(String title, String message, Uri uri) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.pdf)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Cria um Intent para abrir o PDF quando a notificação for clicada
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationId = (int) System.currentTimeMillis(); // Gera um ID único para a notificação
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(notificationId, builder.build());
    }

    private void createFile() {
        Log.d("PDF_CREATE", "create file");
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "");
        startActivityForResult(intent, CREATE_FILE_REQUEST);
    }

    private void createPdf(List<Vistoria> vistoriasList) {
        Log.d("PDF_CREATE", "createPdf() called");

        if (vistoriasList.isEmpty()) {
            Toast.makeText(this, "Não há dados para gerar o PDF!", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("PDF", "Número de vistorias filtradas: " + vistoriasList.size());

        // Solicitar permissão de armazenamento externo antes de criar o arquivo
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        } else {
            createFile();
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("PDF_RESULT", "onActivityResult() called");

        if (data == null) return;

        if (requestCode == CREATE_FILE_REQUEST && resultCode == RESULT_OK) {
            handleCreateFileRequestResult(data);
        } else if (requestCode == SCAN_QR_REQUEST_CODE && resultCode == RESULT_OK) {
            handleScanQrRequestResult(data);
        }
    }

    private void handleCreateFileRequestResult(Intent data) {
        Log.d("PDF_RESULT", "Uri obtido com sucesso");
        Uri uri = data.getData();
        Log.d("PDF_RESULT", "Uri: " + uri.toString());

        String startDate = "01/01/2023";
        String endDate = "31/12/2023";

        List<Vistoria> filteredAnuncios = filterByDate(vistoriasList, startDate, endDate);
        salvaPdfToFile(uri, filteredAnuncios);

        String filePath = uri.getPath();
        Toast.makeText(Relatorios.this, "PDF salvo em: " + filePath, Toast.LENGTH_LONG).show();

        showNotification("PDF salvo com sucesso!", "O PDF foi salvo em: " + filePath, uri);
    }

    private void handleScanQrRequestResult(Intent data) {
        Log.d("PDF_RESULT", "Uri obtido com sucesso");
        String qrContent = data.getStringExtra("SCAN_RESULT");

        if (qrContent == null || !qrContent.startsWith("vistoriaapp://scan?data=")) return;

        String jsonPart = qrContent.substring("vistoriaapp://scan?data=".length());

        Gson gson = new Gson();
        Type listType = new TypeToken<List<Vistoria>>() {}.getType();
        List<Vistoria> itemList = gson.fromJson(jsonPart, listType);

        Intent displayInfoIntent = new Intent(this, DisplayInfoActivity.class);
        displayInfoIntent.putExtra(DisplayInfoActivity.EXTRA_ITEM_LIST_JSON, jsonPart);
        startActivity(displayInfoIntent);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PDF", "Permissão de escrita em armazenamento externo concedida após solicitação do usuário!");
                createPdf(vistoriasList);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Log.d("STORAGE_PERMISSION", "Permissão de armazenamento externo negada pelo usuário");
                    Toast.makeText(this, "A permissão de armazenamento externo é necessária para gerar o relatório", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("STORAGE_PERMISSION", "Permissão de armazenamento externo negada permanentemente pelo usuário");
                    Toast.makeText(this, "A permissão de armazenamento externo é necessária para gerar o relatório. Por favor, conceda a permissão nas configurações do aplicativo.", Toast.LENGTH_LONG).show();

                    // Encaminhar o usuário para as configurações do aplicativo
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
        }
    }
    private void salvaPdfToFile(Uri uri, List<Vistoria> filteredAnuncios) {
        Log.d("PDF_SAVE", "PdfDocument criado e iniciado");
        try {
            PdfDocument document = new PdfDocument();
            int pageWidth = 792;
            int pageHeight = 1122;

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            Paint paint = new Paint();
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setTextSize(20);
            paint.setColor(Color.BLACK);
            canvas.drawText("Relatório de Vistorias Concluídas", 50, 100, paint);

            paint.setTextSize(16);
            canvas.drawText("Detalhes da Vistoria", 50, 150, paint);

            int startY = 200;
            int startX = 50;
            int cellWidth = 100;

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);

            for (int i = 0; i < filteredAnuncios.size() + 2; i++) {
                canvas.drawLine(startX, startY + i * 50, startX + 6 * cellWidth, startY + i * 50, paint);
            }

            for (int i = 0; i < 7; i++) {
                canvas.drawLine(startX + i * cellWidth, startY, startX + i * cellWidth, startY + (filteredAnuncios.size() + 1) * 50, paint);
            }

            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(12);

            canvas.drawText("Item", startX + 10, startY + 30, paint);
            canvas.drawText("Nº Patrimônio", startX + cellWidth + 10, startY + 30, paint);
            canvas.drawText("Observações", startX + 2 * cellWidth + 10, startY + 30, paint);
            canvas.drawText("Vistoriador", startX + 3 * cellWidth + 10, startY + 30, paint);
            canvas.drawText("Data da Vistoria", startX + 4 * cellWidth + 10, startY + 30, paint);
            canvas.drawText("Localização", startX + 5 * cellWidth + 10, startY + 30, paint);

            int rowIndex = 1;
            for (Vistoria vistoria : filteredAnuncios) {
                canvas.drawText(vistoria.getNomePerfilU(), startX + 3 * cellWidth + 10, startY + 30 + rowIndex * 50, paint);
                canvas.drawText(vistoria.getData(), startX + 4 * cellWidth + 10, startY + 30 + rowIndex * 50, paint);
                canvas.drawText(vistoria.getLocalizacao(), startX + 5 * cellWidth + 10, startY + 30 + rowIndex * 50, paint);
                rowIndex++;

            }       document.finishPage(page);

            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w");
            Log.d("PDF_SAVE", "Tentativa de salvar o documento no arquivo Uri");
            if (pfd != null) {
                Log.d("PDF_SAVE", "ParcelFileDescriptor obtido com sucesso");
                try (FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor())) {
                    Log.d("PDF_SAVE", "Tentativa de escrever o documento no arquivo");
                    document.writeTo(fos);
                    Toast.makeText(this, "PDF criado com sucesso!", Toast.LENGTH_SHORT).show();

                    // Adicione o Toast para informar o usuário sobre a localização do arquivo PDF salvo
                    String filePath = uri.getPath();
                    Toast.makeText(Relatorios.this, "PDF salvo em: " + filePath, Toast.LENGTH_LONG).show();

                    // Exibir a notificação após salvar o PDF
                    showNotification("PDF salvo com sucesso!", "O PDF foi salvo em: " + filePath, uri);

                } catch (IOException e) {
                    Log.e("PDF_SAVE", "Erro ao escrever o documento no arquivo: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(this, "Erro ao criar o PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    document.close();
                    pfd.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("PDF_SAVE", "Erro ao criar o PDF: " + e.getMessage());
            Toast.makeText(this, "Erro ao criar o PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String saveQRCodeToStorage(Bitmap qrCodeBitmap) {
        try {
            File directory = new File(getExternalFilesDir(null), "QRCode");
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    throw new IOException("Failed to create directory");
                }
            }

            File file = new File(directory, "QRCode_" + System.currentTimeMillis() + ".png");
            FileOutputStream outputStream = new FileOutputStream(file);
            qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void shareQRCode(String filePath) {
        File fileToShare = new File(filePath);
        Uri contentUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", fileToShare);

        // Compartilhar o arquivo usando um Intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Adicione esta flag
        startActivity(Intent.createChooser(shareIntent, "Compartilhar QR Code via"));
    }


    public interface SearchCallback {
        void onSearchCompleted(List<Vistoria> vistorias);
        void onSearchFailed(String errorMessage);
    }



}







