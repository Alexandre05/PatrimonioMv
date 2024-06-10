package Atividades;

import static Modelos.Utils.containsNullItems;

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
import android.os.Handler;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

import Modelos.Item;
import Modelos.Vistoria;
import br.com.patrimoniomv.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditVistoriaActivity extends AppCompatActivity {
    private Vistoria vistoria;

    private CountDownLatch countDownLatch;

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
    private String vistoriaId;
    private StorageReference storage;
    private final List<String> listaURLFotos = new ArrayList<>();
    private List<Bitmap> imagens = new ArrayList<>();
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 1;
    private final List<Item> listaItens = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_vistoria);

        IniciarComponentes();
        solicitarPermissaoCamera();
        Intent intent = getIntent();

        storage = FirebaseStorage.getInstance().getReference();
        vistoriaId = intent.getStringExtra("idVistoria");

        // Utilize a instância de vistoria que está sendo usada na edição
        if (vistoriaId != null) {
            buscarVistoriaParaEdicao(vistoriaId, new VistoriaCallback() {
                @Override
                public void onVistoriaLoaded(Vistoria loadedVistoria) {
                    vistoria = loadedVistoria;

                    // Declare e utilize a lista de itens da vistoria
                    final ArrayList<Item> listaItens = new ArrayList<>(vistoria.getItensMap().values());

                    // Agora você pode usar a listaItens como precisar
                    Log.d("EditVistoriaActivity", "Lista de Itens: " + listaItens.toString());
                    for (Item item : listaItens) {
                        Log.d("EditVistoriaActivity", "Item: " + item.toString());
                        // Faça o que precisar com cada item
                    }

                    // Crie uma cópia final da lista para uso no OnClickListener
                    final ArrayList<Item> listaItensFinal = new ArrayList<>(listaItens);

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

                @Override
                public void onVistoriaNotFound() {
                    Log.d("INFO", "ID da Vistoria é nulo");
                    // Se o ID da vistoria for nulo, crie uma nova instância
                    vistoria = new Vistoria();

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

                @Override
                public void onVistoriaLoadError() {
                    // Lógica de tratamento em caso de erro ao carregar a vistoria
                    Log.e("ERROR", "Erro ao carregar a vistoria");

                    // Trate o erro conforme necessário
                }
            });
        } else {
            // Se o ID da vistoria for nulo, crie uma nova instância
            vistoria = new Vistoria();

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
    }


    // busca vistoria para manipular
    private void buscarVistoriaParaEdicao(String vistoriaId, final VistoriaCallback callback) {
        DatabaseReference vistoriaRef = Vistoria.getVistoriaReference(vistoriaId);

        vistoriaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Vistoria vistoria = dataSnapshot.getValue(Vistoria.class);
                    if (vistoria != null) {
                        // Carregue os itens manualmente
                        loadItensForVistoria(vistoria, new ItensCallback() {
                            @Override
                            public void onItensLoaded(List<Item> itens) {
                                vistoria.setItensMap(itensToMap(itens));
                                callback.onVistoriaLoaded(vistoria);
                            }

                            @Override
                            public void onItensLoadError() {
                                Log.e("ERROR", "Erro ao carregar itens para a vistoria");
                                callback.onVistoriaLoadError();
                            }
                        });
                    } else {
                        Log.d("INFO", "Falha ao converter dataSnapshot para Vistoria");
                        callback.onVistoriaLoadError();
                    }
                } else {
                    Log.d("INFO", "Nenhuma vistoria encontrada com o ID fornecido");
                    callback.onVistoriaNotFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ERRO", "Erro ao buscar vistoria: " + databaseError.getMessage());
                callback.onVistoriaLoadError();
            }
        });
    }

    // Método para carregar os itens associados à vistoria
    private void loadItensForVistoria(Vistoria vistoria, final ItensCallback callback) {
        DatabaseReference itensRef = Vistoria.getVistoriaReference(vistoria.getIdVistoria()).child("itens");

        itensRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Item> itens = new ArrayList<>();
                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                        Item item = itemSnapshot.getValue(Item.class);
                        itens.add(item);
                    }
                    callback.onItensLoaded(itens);
                } else {
                    callback.onItensLoadError();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ERRO", "Erro ao buscar itens para a vistoria: " + databaseError.getMessage());
                callback.onItensLoadError();
            }
        });
    }

    // Callback para notificar sobre o carregamento dos itens
    interface ItensCallback {
        void onItensLoaded(List<Item> itens);

        void onItensLoadError();
    }

    // Converte a lista de itens em um mapa para ser compatível com o modelo de dados
    private Map<String, Item> itensToMap(List<Item> itens) {
        Map<String, Item> itensMap = new HashMap<>();
        for (Item item : itens) {
            itensMap.put(item.getId(), item);
        }
        return itensMap;
    }


    // Adicione esta interface
    public interface VistoriaCallback {
        void onVistoriaLoaded(Vistoria vistoria);
        void onVistoriaNotFound();
        void onVistoriaLoadError();
    }


    private void exibirToastPorAlgunsSegundos(String mensagem) {
        final Toast toast = Toast.makeText(getApplicationContext(), mensagem, Toast.LENGTH_SHORT);
        toast.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 3000); // 2000 milissegundos (2 segundos)
    }


    private void salvarFotoStorage(Bitmap imagem, int contador, String vistoriaId, OnUploadCompleteListener listener) {
        if (vistoriaId == null || listener == null) {
            Log.e("INFO", "ID da Vistoria ou Listener não estão disponíveis!");
            listener.onUploadComplete(); // Notifica o listener mesmo em caso de erro
            return;
        }

        byte[] dadosImagem = convertBitmapToByteArray(imagem);
        StorageReference imagemRef = createImageStorageReference(vistoriaId, contador);

        UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
                    imagemRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String urlConverted = uri.toString();
                                listaURLFotos.add(urlConverted);
                            })
                            .addOnFailureListener(e -> {
                                exibirMensagemErro("Falha ao obter URL da imagem");
                                Log.e("INFO", "Falha ao obter URL da imagem: " + e.getMessage());
                            })
                            .addOnCompleteListener(task -> {
                                listener.onUploadComplete(); // Notifica o listener quando o upload é concluído
                            });
                })
                .addOnFailureListener(e -> {
                    exibirMensagemErro("Falha ao fazer upload");
                    Log.e("INFO", "Falha ao fazer upload:" + e.getMessage());
                    listener.onUploadComplete(); // Notifica o listener em caso de erro
                });
    }
    public interface OnUploadCompleteListener {
        void onUploadComplete();
    }





    private StorageReference createImageStorageReference(String vistoriaId, int contador) {
        String nomeImagem = "imagem_" + contador + "_" + System.currentTimeMillis() + ".jpeg";

        return storage
                .child("imagens")
                .child("itens")
                .child(vistoriaId)
                .child(nomeImagem);
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
// adiciona item a vistoria existente
private void adicionarItem() {
    ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setMessage("Adicionando item à lista, aguarde...");
    progressDialog.setCancelable(false);
    progressDialog.show();
    uploadedImagesCount = 0;

    String nomeItem = CampoNomeItem.getText().toString();
    String placa = campoPlaca.getText().toString();
    String ob = CampoObservacoes.getText().toString();

    if (nomeItem.trim().isEmpty()) {
        Toast.makeText(EditVistoriaActivity.this, "Nome do Item é obrigatório.", Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
        return;
    }

    if (vistoria != null && vistoria.getIdVistoria() != null) {
        if (imagens.isEmpty()) {
            progressDialog.dismiss();
            exibirDialogSemImagens();
            return;
        }

        Item item = new Item();
        item.setNome(nomeItem);
        item.setPlaca(placa);
        item.setObservacao(ob);
        item.setIdVistoria(vistoria.getIdVistoria());
        item.setFotos(new ArrayList<>(listaURLFotos));
        item.setLatitude(currentLatitude);
        item.setLongitude(currentLongitude);

        DatabaseReference vistoriaRef = Vistoria.getVistoriaReference(vistoria.getIdVistoria());
        vistoriaRef.child("itens").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Item> itemList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Item existingItem = snapshot.getValue(Item.class);
                    itemList.add(existingItem);
                }
                itemList.add(item);

                vistoriaRef.child("itens").setValue(itemList)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EditVistoriaActivity.this, "Item adicionado à vistoria!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            handleError("Erro ao salvar itens no Firebase: " + e.getMessage());
                            progressDialog.dismiss();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleError("Erro ao buscar itens para a vistoria: " + databaseError.getMessage());
                progressDialog.dismiss();
            }
        });
    } else {
        progressDialog.dismiss();
        Toast.makeText(EditVistoriaActivity.this, "Erro: Vistoria ou ID de Vistoria nulos.", Toast.LENGTH_SHORT).show();
    }
}


    // Método para exibir a lista de itens (apenas para teste)
    // Método para exibir a lista de itens (apenas para teste)
    private void exibirListaItensSalva(Vistoria loadedVistoria, List<Item> listaItensNovoItem) {
        if (loadedVistoria != null) {
            // Obtenha a lista de itens da vistoria carregada (apenas para teste)
            ArrayList<Item> listaItensCarregada = new ArrayList<>(loadedVistoria.getItensMap().values());

            // Adicione a lista do novo item à lista carregada (apenas para teste)
            listaItensCarregada.addAll(listaItensNovoItem);

            // Exiba a lista de itens no Log ou de outra forma conforme necessário (apenas para teste)
            Log.d("INFO", "Lista de Itens Salva no Banco (Apenas para Teste): " + listaItensCarregada);
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
// metodo que finaliza a vistoria
public void FinalizarVistoria(View view) {
    if (vistoria != null) {
        String idVistoria = vistoria.getIdVistoria();
        if (idVistoria != null) {
            if (!listaItens.isEmpty()) {
                salvarItensNoBanco(vistoria, listaItens);
            }

            Item novoItem = new Item();
            novoItem.setNome("Novo Item");
            novoItem.setPlaca("Nova Placa");
            novoItem.setObservacao("Nova Observação");
            novoItem.setIdVistoria(idVistoria);
            novoItem.setFotos(new ArrayList<>(listaURLFotos));

            ArrayList<Item> listaItensExistente = new ArrayList<>(vistoria.getItensMap().values());
            listaItensExistente.add(novoItem);

            vistoria.getItensMap().clear();
            for (Item item : listaItensExistente) {
                vistoria.adicionarItem(item);
            }

            Toast.makeText(this, "Vistoria finalizada com sucesso!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, EditVistoriaActivity.class));
            finish();
        } else {
            handleError("Erro: ID de Vistoria nulo.");
        }
    } else {
        handleError("Erro: Vistoria nula.");
    }
}


    private void handleError(String errorMessage) {
        Log.e("ERROR", errorMessage);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    // Método auxiliar para verificar se uma string é válida (não nula e não vazia)


    private void salvarItensNoBanco(Vistoria vistoria, List<Item> listaItens) {
        // Verifica se a vistoria e o ID da vistoria não são nulos
        if (vistoria != null && isStringValid(vistoria.getIdVistoria())) {
            // Obtém a referência da vistoria no Firebase
            DatabaseReference vistoriaRef = Vistoria.getVistoriaReference(vistoria.getIdVistoria());

            // Verifica se a referência não é nula
            if (vistoriaRef != null) {
                // Define o caminho para os itens no banco de dados (Firebase)
                String caminhoItens = "Itens";

                // Verifica se a string de caminho é válida
                if (isStringValid(caminhoItens)) {
                    // Logs adicionais para verificar o estado da lista antes de salvar no Firebase
                    Log.d("INFO", "Lista de Itens antes de salvar no Firebase: " + listaItens);

                    // Chama o método para salvar os itens no Firebase
                    salvarItensFirebase(vistoriaRef.child(caminhoItens), listaItens);
                } else {
                    // Lógica de tratamento se a string de caminho não for válida
                    handleError("Caminho de itens inválido");
                }
            } else {
                // Lógica de tratamento se a referência for nula
                handleError("Referência de vistoria nula");
            }
        } else {
            // Lógica de tratamento se vistoria ou ID de vistoria forem nulos
            handleError("Vistoria ou ID de vistoria nulos");
        }
    }

    /**
     * Salva os itens no Firebase Database.
     *
     * @param itensRef Referência do Firebase onde os itens serão salvos.
     * @param listaItens Lista de itens a serem salvos.
     *                 Comentado por Alexandre B. Menna
     */
    private void salvarItensFirebase(DatabaseReference itensRef, List<Item> listaItens) {
        // Verifica se a referência e a lista de itens não são nulos
        if (itensRef != null && listaItens != null) {
            try {
                // Verifica se listaItens não contém itens nulos
                if (!containsNullItems(listaItens)) {
                    // Cria um mapa para armazenar os itens usando os IDs como chaves
                    Map<String, Item> itensMap = new HashMap<>();
                    for (Item item : listaItens) {
                        // Verifica se o item não é nulo
                        if (item != null) {
                            // Verifica se o ID do item não é nulo ou vazio
                            if (isStringValid(item.getId())) {
                                itensMap.put(item.getId(), item);
                            } else {
                                // Lógica de tratamento se o ID do item for nulo ou vazio
                                handleError("ID do item é nulo ou vazio: " + item.toString());
                            }
                        } else {
                            // Lógica de tratamento se o item for nulo
                            handleError("Item na lista é nulo");
                        }
                    }

                    // Log da lista de itens que será salva no banco após processamento
                    Log.d("INFO", "Lista de Itens que será salva no banco (após processamento): " + itensMap);

                    // Tenta salvar os itens no Firebase
                    itensRef.setValue(itensMap)
                            .addOnSuccessListener(aVoid -> Log.d("INFO", "Itens salvos no Firebase com sucesso"))
                            .addOnFailureListener(e -> {
                                handleError("Erro ao salvar itens no Firebase: " + e.getMessage());
                                Log.e("ERROR", "Detalhes do Erro:", e);
                            });
                } else {
                    // Lógica de tratamento se listaItens contiver itens nulos
                    handleError("Lista de itens contém itens nulos");
                }
            } catch (Exception e) {
                // Lógica de tratamento se ocorrer uma exceção
                handleError("Erro ao salvar itens no Firebase: " + e.getMessage());
            }
        } else {
            // Lógica de tratamento se a referência ou lista de itens for nula
            handleError("Referência ou lista de itens nulos");
        }
    }



    private boolean isStringValid(String str) {
        return str != null && !str.isEmpty();
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
