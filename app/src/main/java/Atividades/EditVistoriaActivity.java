package Atividades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Modelos.Item;
import Modelos.Vistoria;
import br.com.patrimoniomv.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditVistoriaActivity extends AppCompatActivity {
    private Vistoria vistoria;
    private static final int CODIGO_GALERIA = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private EditText campoPlaca, CampoNomeItem, CampoObservacoes;
    private CircleImageView imageCada1;
    private TextView itemCountTextView;
    private int itemCount = 0;
    private TextView CampoNovoLocalizacao;
    private double currentLatitude;

    private double currentLongitude;
    private Button botaoAdicionarItem;
    private Vistoria vistoriaAtual;
    private static final int PERMISSAO_CAMERA = 1;
    private int uploadedImagesCount = 0;
    private StorageReference storage;
    private List<String> listaURLFotos = new ArrayList<>();
    private List<Bitmap> imagens = new ArrayList<>();
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 1;
    private List<Item> listaItens = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_vistoria);
        IniciarComponentes();
        solicitarPermissaoCamera();
        Intent intent = getIntent();
        ArrayList<Item> listaItens = getIntent().getParcelableArrayListExtra("listaItens");
// Agora você pode usar a listaItens como precisar
        if (listaItens != null) {
            for (Item item : listaItens) {
                Log.d("EditVistoriaActivity", "Item: " + item.toString());
                // Faça o que precisar com cada item
            }
        }
        if (intent != null) {
            String vistoriaId = intent.getStringExtra("idVistoria");
        }
        if (intent != null) {
            String vistoriaId = intent.getStringExtra("idVistoria");
            Log.d("ID", "MENSAGEM" + vistoriaId);
            listaItens = new ArrayList<>();

            if (vistoriaId != null) {
                buscarVistoriaParaEdicao(vistoriaId);
            } else {
                Log.d("INFO", "ID da Vistoria é nulo");
            }
        } else {
            Log.d("INFO", "Intent é nula");
        }

        botaoAdicionarItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adicionarItem();
            }
        });
        imageCada1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exibirDialogEscolherFonteImagem();
            }
        });

    }
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        Log.e("INFO", "onLocationChanged - Latitude: " + currentLatitude);
        Log.e("INFO", "onLocationChanged - Longitude: " + currentLongitude);
        // Aqui você pode atualizar a localização do item no objeto Anuncios
    }
    private void solicitarPermissaoCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("Permissões de Localização");
                builder.setMessage("Este aplicativo requer permissões de localização para funcionar corretamente. Por favor, permita o acesso à localização.");
                builder.setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION_CODE));
                builder.setNegativeButton("Cancelar", null);
                builder.create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION_CODE);
            }
        }
    }
    private void buscarVistoriaParaEdicao(String vistoriaId) {
        DatabaseReference vistoriaRef = Vistoria.getVistoriaReference(vistoriaId);

        // Adicione um ValueEventListener para receber os dados da vistoria
        vistoriaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    vistoria = dataSnapshot.getValue(Vistoria.class);

                    // Adicione logs para verificar o estado da vistoria e seu ID
                    Log.d("INFO", "Vistoria não é nula");

                    if (vistoria != null && vistoria.getIdVistoria() != null) {
                        Log.d("INFO", "ID da Vistoria: " + vistoria.getIdVistoria());
                        Map<String,Item> itensMap=vistoria.getItensMap();
                        Item novoItem = new Item();
                        novoItem.setNome("Novo Item");
                        novoItem.setObservacao("Nova Observação");
                        itensMap.put(novoItem.getId(),novoItem);

                        // Restante do código para edição da vistoria...
                    } else {
                        Log.d("INFO", "Vistoria ou ID de Vistoria nulos.");
                    }
                } else {
                    Log.d("INFO", "Vistoria não encontrada");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Trate erros de leitura do Firebase, se necessário
                Log.e("ERRO", "Erro ao buscar vistoria: " + databaseError.getMessage());
            }
        });
    }
    private void salvarFotoStorage(Bitmap imagem, int totalFotos, int contador, Vistoria vistoria, Runnable onSuccess) {
        if (vistoria.getIdVistoria() == null) {
            Log.e("INFO", "Vistoria ID não está disponível!");
            return;
        }

        byte[] dadosImagem = convertBitmapToByteArray(imagem);
        StorageReference imagemRef = createImageStorageReference(vistoria, contador);

        UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
        uploadTask.addOnSuccessListener(taskSnapshot -> imagemRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String urlConverted = uri.toString();
                            listaURLFotos.add(urlConverted);

                            onSuccess.run();
                        })
                        .addOnFailureListener(e -> {
                            exibirMensagemErro("Falha ao fazer upload");
                            Log.e("INFO", "Falha ao fazer upload:" + e.getMessage());
                        }))
                .addOnFailureListener(e -> {
                    exibirMensagemErro("Falha ao fazer upload");
                    Log.e("INFO", "Falha ao fazer upload:" + e.getMessage());
                });
    }

    private byte[] convertBitmapToByteArray(Bitmap imagem) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        imagem.compress(Bitmap.CompressFormat.JPEG, 80, bao);
        return bao.toByteArray();
    }

    private StorageReference createImageStorageReference(Vistoria vistoria, int contador) {
        String idVistoria = vistoria.getIdVistoria();
        String nomeImagem = "imagem_" + contador + "_" + System.currentTimeMillis() + ".jpeg";

        return storage
                .child("imagens")
                .child("Itens")
                .child(idVistoria)
                .child(nomeImagem);
    }

    private void uploadImageToStorage(byte[] dadosImagem, StorageReference imagemRef, Runnable onSuccess) {
        UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
        uploadTask.addOnSuccessListener(taskSnapshot -> imagemRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    String urlConverted = uri.toString();
                    // Certifique-se de que você está adicionando a URL da imagem ao item correto na vistoria
                    // Exemplo: item.setFotoUrl(urlConverted);

                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    exibirMensagemErro("Falha ao fazer upload");
                    Log.e("INFO", "Falha ao fazer upload:" + e.getMessage());
                }));
    }

    private void exibirMensagemErro(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }

    private void adicionarItem() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adicionando item à lista, aguarde...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        uploadedImagesCount = 0;

        String nomeItem = CampoNomeItem.getText().toString();
        String placa = campoPlaca.getText().toString();
        String ob = CampoObservacoes.getText().toString();
        Log.d("INFO", "Nome do Item: " + nomeItem + placa + ob);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            if (nomeItem.trim().isEmpty()) {
                Toast.makeText(EditVistoriaActivity.this, "Nome do Item é obrigatório.", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                return;
            }
        }

        // Verifica se a vistoria e o ID da vistoria não são nulos
        if (vistoria != null && vistoria.getIdVistoria() != null) {
            if (imagens.isEmpty()) {
                progressDialog.dismiss();
                exibirDialogSemImagens();
                return;
            }

            for (int i = 0; i < imagens.size(); i++) {
                Bitmap imagem = imagens.get(i);
                int tamanhoLista = imagens.size();

                int finalI = i;
                salvarFotoStorage(imagem, tamanhoLista, i, vistoria, () -> {
                    uploadedImagesCount++;
                    Log.d("INFO", "Imagem " + finalI + " enviada com sucesso. Total de imagens enviadas: " + uploadedImagesCount);
                    if (uploadedImagesCount == imagens.size()) {
                        Item item = criarItem();
                        item.setFotos(new ArrayList<>(listaURLFotos));

                        // Adiciona o item à vistoria existente
                        vistoria.getItensMap().put(item.getId(), item);

                        // Adicionando item na listaItens
                        listaItens.add(item);

                        Toast.makeText(EditVistoriaActivity.this, "Item adicionado à vistoria!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }
        } else {
            progressDialog.dismiss();
            Toast.makeText(EditVistoriaActivity.this, "Erro: Vistoria ou ID de Vistoria nulos.", Toast.LENGTH_SHORT).show();
        }
    }

    private void exibirDialogSemImagens() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sem Imagens Adicionadas");
        builder.setMessage("Você não adicionou nenhuma imagem. Deseja continuar sem imagens?");
        builder.setPositiveButton("Continuar sem Imagens", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Continuar sem imagens
                dialog.dismiss();
                // Aqui você pode adicionar lógica adicional se necessário
            }
        });
        builder.setNegativeButton("Adicionar Imagens", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Adicionar lógica para voltar e adicionar imagens
                dialog.dismiss();
                // Por exemplo, você pode chamar o método para abrir a galeria novamente
                abrirGaleria();
            }
        });
        builder.show();
    }


    private void exibirDialogEscolherFonteImagem() {
        final CharSequence[] opcoes = {"Câmera", "Galeria", "Cancelar"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolher Fonte da Imagem");
        builder.setItems(opcoes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int opcao) {
                if (opcoes[opcao].equals("Câmera")) {
                    // Código para abrir a câmera
                    abrirCamera();
                } else if (opcoes[opcao].equals("Galeria")) {
                    // Código para abrir a galeria de fotos
                    abrirGaleria();
                } else if (opcoes[opcao].equals("Cancelar")) {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }
    private void abrirCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Não há aplicativo de câmera disponível", Toast.LENGTH_SHORT).show();
        }
    }


    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, CODIGO_GALERIA);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Log.d("INFO", "Imagem capturada com sucesso.");
                // A imagem foi capturada com sucesso, você pode processar a imagem aqui
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                // Faça algo com a imagem capturada
            } else {
                Log.e("ERROR", "Data ou Extras são nulos.");
            }
        }
    }

    private Item criarItem() {
        Item item = new Item();
        item.setNome(CampoNomeItem.getText().toString());
        item.setObservacao(CampoObservacoes.getText().toString());
        item.setPlaca(campoPlaca.getText().toString());
        //item.setLocalizacao(vistoriaAtual.getLocalizacao());
        item.setFotos(listaURLFotos);
        // Definir latitude e longitude do item
        item.setLatitude(currentLatitude);
        item.setLongitude(currentLongitude);
        Log.e("INFO", "Latitude"+currentLatitude);
        Log.e("INFO", "Longe"+currentLongitude);
        // Gere um ID único para o item usando o Firebase
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("itens");
        String itemId = itemsRef.push().getKey();
        item.setId(itemId);

        return item;
    }



    public void adicionarItemVistoriaExistente(View view) {
        listaURLFotos.clear();

        if (!CampoNomeItem.getText().toString().trim().isEmpty() &&
                !CampoObservacoes.getText().toString().trim().isEmpty() &&
                imagens.size() > 0) {

            String placa = campoPlaca.getText().toString();
            if (placa != null && !placa.trim().isEmpty()) {
                isPlacaAlreadyInDatabase(placa, placaExists -> {
                    if (placaExists || isPlacaInItemList(placa)) {
                        Toast.makeText(EditVistoriaActivity.this, "Número de Patrimônio já adicionado! Por favor, Verifique novamente.", Toast.LENGTH_SHORT).show();
                    } else {
                        adicionarNovoItemVistoria(placa);
                    }
                });
            } else {
                adicionarNovoItemVistoria(placa);
            }
        } else {
            Toast.makeText(EditVistoriaActivity.this, "Preencha todos os campos e adicione pelo menos uma imagem.", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean isPlacaInItemList(String placa) {
        for (Item item : listaItens) {
            if (item.getPlaca().equalsIgnoreCase(placa)) {
                return true;
            }
        }
        return false;
    }
    private void isPlacaAlreadyInDatabase(String placa, CadastrarItens.OnPlacaCheckCompleteListener listener) {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("Itens");
        Query query = itemsRef.orderByChild("placa").equalTo(placa);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    listener.onComplete(true);
                } else {
                    listener.onComplete(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onComplete(false);
            }
        });
    }
    private void adicionarNovoItemVistoria(String placa) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adicionando novo item à vistoria, aguarde...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        uploadedImagesCount = 0;

        for (int i = 0; i < imagens.size(); i++) {
            Bitmap imagem = imagens.get(i);
            int tamanhoLista = imagens.size();
            salvarFotoStorage(imagem, tamanhoLista, i, vistoriaAtual, () -> {
                uploadedImagesCount++;
                if (uploadedImagesCount == imagens.size()) {
                    Item novoItem = criarItem();
                    novoItem.setFotos(new ArrayList<>(listaURLFotos));

                    // Adiciona o novo item à vistoria existente
                    vistoriaAtual.getItensMap().put(novoItem.getId(), novoItem);

                    // Adicionando o novo item na listaItens
                    listaItens.add(novoItem);

                    // Incrementa a contagem de itens
                    itemCount++;

                    // Atualiza o texto do TextView com a nova contagem
                    itemCountTextView.setText("Itens adicionados: " + itemCount);

                    // Exibindo o novo item no log para verificação
                    Log.d("INFO", "Novo Item adicionado à listaItens: " + novoItem.toString());

                    Toast.makeText(EditVistoriaActivity.this, "Novo item adicionado à vistoria existente!", Toast.LENGTH_SHORT).show();

                    progressDialog.dismiss();
                }
            });
        }
    }
    private void IniciarComponentes() {
        CampoNomeItem = findViewById(R.id.NovoNome);
        campoPlaca = findViewById(R.id.NovaPlaca);
        CampoObservacoes = findViewById(R.id.NovaObs);
        botaoAdicionarItem = findViewById(R.id.NovoadicionarItemVistoria);
        CampoNovoLocalizacao= findViewById(R.id.Novolocalizacao);
        imageCada1=findViewById(R.id.NovimageCada1);
        // Inicialize o TextView
        itemCountTextView = findViewById(R.id.NovoitemCountTextView);
    }



}
