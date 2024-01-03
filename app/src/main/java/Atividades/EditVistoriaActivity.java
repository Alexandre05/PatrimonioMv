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
import android.net.Uri;
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
import com.google.firebase.storage.FirebaseStorage;
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
    private ProgressDialog progressDialog;
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
        storage = FirebaseStorage.getInstance().getReference();
     

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
                .child("itens")
                .child(idVistoria)
                .child(nomeImagem);
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

                        // Incrementa o contador de itens
                        itemCount++;

                        // Atualiza o TextView com o novo valor do contador
                        updateItemCountText();

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
    private void updateItemCountText() {
        // Atualiza o TextView com o número atual de itens
        itemCountTextView.setText("Total de Itens: " + itemCount);
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

        if (requestCode == CODIGO_GALERIA && resultCode == RESULT_OK && data != null && data.getData() != null) {
            processarImagemSelecionada(data.getData());
        }
    }
    private void processarImagemSelecionada(Uri imageUri) {
        Log.d("INFO", "Imagem da galeria selecionada com sucesso.");
        // Faça algo com a imagem da galeria
        try {
            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            imagens.add(imageBitmap);
            // Exemplo: exibir a imagem em um ImageView
            imageCada1.setImageBitmap(imageBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void processarImagemCapturada(Bitmap imageBitmap) {
        Log.d("INFO", "Imagem capturada com sucesso.");
        // Faça algo com a imagem capturada
        imagens.add(imageBitmap);
        // Exemplo: exibir a imagem em um ImageView
        imageCada1.setImageBitmap(imageBitmap);
    }

    private Item criarItem() {
        String nomeItem = CampoNomeItem.getText().toString().trim();
        String observacao = CampoObservacoes.getText().toString().trim();
        String placa = campoPlaca.getText().toString().trim();

        if (nomeItem.isEmpty() || observacao.isEmpty() || placa.isEmpty()) {
            // Adicione a lógica de tratamento para valores vazios
            Toast.makeText(EditVistoriaActivity.this, "Preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return null; // Retorna null para indicar que o item não pode ser criado
        }

        Item item = new Item();
        item.setNome(nomeItem);
        item.setObservacao(observacao);
        item.setPlaca(placa);
        //item.setLocalizacao(vistoriaAtual.getLocalizacao());
        item.setFotos(listaURLFotos);
        // Definir latitude e longitude do item
        item.setLatitude(currentLatitude);
        item.setLongitude(currentLongitude);

        // Gere um ID único para o item usando o Firebase
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("itens");
        String itemId = itemsRef.push().getKey();
        item.setId(itemId);

        return item;
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
    public void FinalizarVistoria(View view) {
        if (vistoria != null && vistoria.getIdVistoria() != null) {
            // Salve os itens da lista no banco de dados
            salvarItensNoBanco(vistoria, listaItens);

            // Adicione a lógica para finalizar a vistoria aqui
            // Por exemplo, você pode exibir uma mensagem ou iniciar uma nova atividade
            Toast.makeText(this, "Vistoria finalizada com sucesso!", Toast.LENGTH_SHORT).show();

            // Aqui você pode adicionar qualquer lógica adicional que precisa ser executada ao finalizar a vistoria

            // Por exemplo, você pode iniciar uma nova atividade
            Intent intent = new Intent(this, EditVistoriaActivity.class);
            startActivity(intent);

            // Ou você pode finalizar a atividade atual
            finish();
        } else {
            // Lógica de tratamento caso a vistoria seja nula
            Toast.makeText(this, "Erro: Vistoria ou ID de Vistoria nulos.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para salvar os itens no banco de dados
    /**
     * Salva os itens da vistoria no banco de dados Firebase.
     *
     * @param vistoria   A vistoria contendo os itens a serem salvos.
     * @param listaItens Lista de itens a serem salvos.
     *                   Comentado por Alexandre B. Menna
     */
    private void salvarItensNoBanco(Vistoria vistoria, List<Item> listaItens) {
        // Verifica se a vistoria e o ID da vistoria não são nulos
        if (vistoria != null && vistoria.getIdVistoria() != null) {
            // Obtém a referência da vistoria no Firebase
            DatabaseReference vistoriaRef = Vistoria.getVistoriaReference(vistoria.getIdVistoria());

            // Verifica se a referência não é nula
            if (vistoriaRef != null) {
                // Define o caminho para os itens no banco de dados (Firebase)
                String caminhoItens = "Itens";

                // Verifica se a string de caminho é válida
                if (isStringValid(caminhoItens)) {
                    // Chama o método para salvar os itens no Firebase
                    salvarItensFirebase(vistoriaRef.child(caminhoItens), vistoria.getItensMap());
                } else {
                    // Lógica de tratamento se a string de caminho não for válida
                    Log.e("ERROR", "Caminho de itens inválido");
                }
            } else {
                // Lógica de tratamento se a referência for nula
                Log.e("ERROR", "Referência de vistoria nula");
            }
        } else {
            // Lógica de tratamento se vistoria ou ID de vistoria forem nulos
            Log.e("ERROR", "Vistoria ou ID de vistoria nulos");
        }
    }

    /**
     * Verifica se a string é válida (não nula e não vazia).
     *
     * @param str A string a ser verificada.
     * @return True se a string for válida, False caso contrário.
     *         Comentado por Alexandre B. Menna
     */
    private boolean isStringValid(String str) {
        return str != null && !str.isEmpty();
    }

    /**
     * Salva os itens no Firebase Database.
     *
     * @param itensRef Referência do Firebase onde os itens serão salvos.
     * @param itensMap Mapa de itens a serem salvos.
     *                 Comentado por Alexandre B. Menna
     */
    private void salvarItensFirebase(DatabaseReference itensRef, Map<String, Item> itensMap) {
        // Verifica se a referência e o mapa de itens não são nulos
        if (itensRef != null && itensMap != null) {
            try {
                // Tenta salvar os itens no Firebase
                itensRef.setValue(itensMap)
                        .addOnSuccessListener(aVoid -> Log.d("INFO", "Itens salvos no Firebase com sucesso"))
                        .addOnFailureListener(e -> Log.e("ERROR", "Erro ao salvar itens no Firebase: " + e.getMessage()));
            } catch (Exception e) {
                // Lógica de tratamento se ocorrer uma exceção
                Log.e("ERROR", "Erro ao salvar itens no Firebase: " + e.getMessage());
            }
        } else {
            // Lógica de tratamento se a referência ou mapa de itens for nulo
            Log.e("ERROR", "Referência ou mapa de itens nulos");
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
        updateItemCountText();
    }



}
