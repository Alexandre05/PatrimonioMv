package Atividades;

import static android.graphics.ImageDecoder.decodeBitmap;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


import Ajuda.ConFirebase;
import Ajuda.DataCuston;
import Modelos.Item;
import Modelos.Vistoria;
import Ajuda.Permissoes;
import Modelos.Usuario;
import br.com.patrimoniomv.R;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class CadastrarItens extends AppCompatActivity
        implements android.view.View.OnClickListener, LocationListener {
    private EditText campoNome, campoPlaca, campoObs,campoNomeRes;
    private String ultimoUrlImagem = null;
    private List<Item> listaItens = new ArrayList<>();
    private static final int REQUEST_LOCATION_PERMISSION_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private boolean itemAdicionado = false;
    private CircleImageView imageCada1;
    private HorizontalScrollView imageContainer;
    private List<Vistoria> itensVistoria;
    private Vistoria vistorias;
    private Item item;
    private Usuario usuario;
    private int imageSize;
    private double currentLatitude;
    private double currentLongitude;

    private AlertDialog dialog;
    private Usuario usuarioLogado;
    Bitmap imagem=null;
    Bitmap imagem2=null;
    private Button salvar,botaoAdicionarItem;
    private TextView campoData;
    private int uploadedImagesCount = 0;
    private List<Bitmap> imagens = new ArrayList<>();

    private static final int seleCame = 100;
    private static final int seleGale = 200;
    private Spinner campoLocalizacao;
    private StorageReference storage;
    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

    private List<String> listasFotoRe = new ArrayList<>();
    private AtomicInteger successfulUploads = new AtomicInteger(0);


    private List<String> listaURLFotos = new ArrayList<>();
    private LocationManager locationManager;
    private Vistoria vistoriaAtual;
    private double latitude;
    private int itemCount = 0;
    private TextView itemCountTextView;
    private double longitude;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageSize = getResources().getDimensionPixelSize(R.dimen.image_size);
        vistoriaAtual = new Vistoria();
        vistoriaAtual.setData(DataCuston.dataAtual());
        vistoriaAtual.setItens(new ArrayList<Item>());
        setContentView(R.layout.activity_cadastrar_itens);
        Permissoes.validarPermissoes(permissoes, this, 1);
        FirebaseUser usuario = ConFirebase.getUsuarioAtaul();
        iniciarCamponentes();
        setupLocationManager();
        verificarPermissoesLocalizacao();
        carregarSpi();
        storage = FirebaseStorage.getInstance().getReference();
        vistoriaAtual.setLocalizacao(campoLocalizacao.getSelectedItem().toString());
        itensVistoria = new ArrayList<>();
        storage = ConFirebase.getFirebaseStorage();
        usuarioLogado= ConFirebase.getDadosUsarioLogado();
        campoNomeRes.setText(usuario.getDisplayName());
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }
// limpa os capos
    private void limparCampos() {
        campoNome.setText("");
        campoObs.setText("");
        campoPlaca.setText("");
        imagens.clear();
        uploadedImagesCount = 0;
    }
    private void verificarPermissoesLocalizacao() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
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


    interface OnPlacaCheckCompleteListener {
        void onComplete(boolean placaExists);
    }



    private Item criarItem() {
        Item item = new Item();
        item.setNomeItem(campoNome.getText().toString());
        item.setObservacao(campoObs.getText().toString());
        item.setPlaca(campoPlaca.getText().toString());
        item.setFotos(listaURLFotos);
        // Definir latitude e longitude do item
        item.setLatitude(currentLatitude);
        item.setLongitude(currentLongitude);
        Log.e("INFO", "Latitude"+currentLatitude);
        Log.e("INFO", "Longe"+currentLongitude);
        // Gere um ID único para o item usando o Firebase
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("Itens");
        String itemId = itemsRef.push().getKey();
        item.setId(itemId);

        return item;
    }


    private Vistoria configurarVistoria(Item item) {
        String nomeCampo = campoNomeRes.getText().toString();
        usuarioLogado.setNome(nomeCampo);
        Vistoria vistoria = new Vistoria();
        vistoria.setLocalizacao(campoLocalizacao.getSelectedItem().toString());
        vistoria.setNomePerfilU(nomeCampo);
        vistoria.setIdInspector(usuarioLogado.getIdU());
        vistoria.setData(DataCuston.dataAtual());
        vistoria.setLocalizacao_data(vistoria.getLocalizacao() + "_" + vistoria.getData());
        vistoria.getItens().add(item);
        //vistoria.setFotos(item.getFotos());
        return vistoria;
    }
    private void salvarVistoriaNoFirebase(Vistoria vistoria, String localizacaoSelecionada, String nomePerfilUsuario) {
        DatabaseReference vistoriasRef = FirebaseDatabase.getInstance().getReference("vistorias");
        DatabaseReference anuncioPuRef = FirebaseDatabase.getInstance().getReference("vistoriaPu");
        DatabaseReference localizacaoRef = vistoriasRef.child(localizacaoSelecionada);
        DatabaseReference localizacaoPuRef = anuncioPuRef.child(localizacaoSelecionada);

        // Adicionar o nome do perfil do usuário ao objeto Vistorias
        vistoria.setNomePerfilU(nomePerfilUsuario);

        // Converter a lista de itens em um mapa
        Map<String, Object> itensMap = new HashMap<>();
        for (Item item : vistoria.getItens()) {
            itensMap.put(item.getId(), item.toMap());
        }

        // Adicionar o mapa de itens ao objeto Vistorias
        vistoria.setItensMap(itensMap);

        // Salvar Vistorias em "vistorias"
        localizacaoRef.setValue(vistoria.toMap());

        // Salvar Vistorias em "anuncioPu"
        localizacaoPuRef.setValue(vistoria.toMap());
    }


    public void adicionarItemVistoria(View view) {
        listaURLFotos.clear();

        if (!campoNome.getText().toString().trim().isEmpty() &&
                !campoObs.getText().toString().trim().isEmpty() &&
                imagens.size() > 0) {

            String placa = campoPlaca.getText().toString();
            if (isPlacaDuplicada(placa)) {
                Toast.makeText(CadastrarItens.this, "Placa já adicionada! Por favor, insira uma placa diferente.", Toast.LENGTH_SHORT).show();
                return;
            }



                // Adicione o diálogo de progresso aqui
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Adicionando item à lista, aguarde...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                uploadedImagesCount = 0;
                for (int i = 0; i < imagens.size(); i++) {
                    Bitmap imagem = imagens.get(i);
                    int tamanhoLista = imagens.size();
                    salvarFotoStorage(imagem, tamanhoLista, i, vistoriaAtual, () -> {
                        uploadedImagesCount++;
                        if (uploadedImagesCount == imagens.size()) {
                            Item item = criarItem();
                            item.setFotos(new ArrayList<>(listaURLFotos));
                            listaItens.add(item);
                            vistoriaAtual.setItens(listaItens);
                            Toast.makeText(CadastrarItens.this, "Item adicionado à vistoria!", Toast.LENGTH_SHORT).show();
                            incrementItemCount();
                            limparCampos();
                            recriarLayoutImagens();
                            itemAdicionado = true;
                            campoLocalizacao.setEnabled(false);
                            progressDialog.dismiss(); // Feche o diálogo de progresso quando o item for adicionado
                        }
                    });
                }
            } else {
                Toast.makeText(CadastrarItens.this, "Você está muito longe da localização inicial do vistoriado. Por favor, vá até a localização correta para adicionar o item.", Toast.LENGTH_SHORT).show();
            }
        }
       public void FinalizarVistoria(View view) {
        Log.d("CadastrarItens", "FinalizarVistoria chamado");
        if (vistoriaAtual.getItens().isEmpty()) {
            Toast.makeText(CadastrarItens.this, "Por favor, adicione pelo menos um item antes de finalizar a vistoria!", Toast.LENGTH_SHORT).show();
            return;
        }
        exibirDialogSalvando();
        //vistoriaAtual.setItens(listaItens);
        // Salvar a vistoria na localização selecionada
        String localizacaoSelecionada = campoLocalizacao.getSelectedItem().toString();
        vistoriaAtual.setLocalizacao(localizacaoSelecionada);
        String nomePerfilUsuario = usuarioLogado.getNome();
        salvarVistoriaNoFirebase(vistoriaAtual, localizacaoSelecionada, nomePerfilUsuario); // Salva a vistoria no Firebase, incluindo os itens
        listaItens.clear(); // Limpe a lista de itens para a próxima vistoria
        dialog.dismiss();
        finish();
        itemAdicionado = false;
        campoLocalizacao.setEnabled(true);
    }

    private void salvarFotoStorage(Bitmap imagem, int totalFotos, int contador, Vistoria vistoria, Runnable onSuccess) {
        if (vistoria.getIdVistoria() == null) {
            Log.e("INFO", "Vistoria ID não está disponível!");
            return;
        }

        byte[] dadosImagem = convertBitmapToByteArray(imagem);
        StorageReference imagemRef = createImageStorageReference(vistoria, contador);

        uploadImageToStorage(dadosImagem, imagemRef, onSuccess);
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
                    listaURLFotos.add(urlConverted);
                    vistoriaAtual.setFotos(listaURLFotos);
                    listasFotoRe.clear();

                    if (dialog != null) {
                        dialog.dismiss();
                    }

                    onSuccess.run();
                    //limparImagens();
                })
                .addOnFailureListener(e -> {
                    exibirMensagemErro("Falha ao fazer upload");
                    Log.e("INFO", "Falha ao fazer upload:" + e.getMessage());
                }));
    }
    private void exibirDialogSalvando() {
        dialog = new SpotsDialog.Builder(this)
                .setMessage("Salvando...")
                .setCancelable(false)
                .show();
        dialog.show();
    }

    public void validarVistorias(android.view.View view) {
        solicitarPermissaoLocalizacao();

        Item item = criarItem(); // Crie o objeto item
        vistorias = configurarVistoria(item);

        if (imagens.size() != 0) {
            validarCamposEObrigatorios();
        } else {
            exibirMensagemErro("Selecione ao menos uma foto!");
        }
    }

    private void solicitarPermissaoLocalizacao() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        }
    }


    private void validarCamposEObrigatorios() {
        if (!vistorias.getLocalizacao().isEmpty()) {
            if (!item.getNomeItem().isEmpty()) {
                verificarPlacaExistente();
            } else {
                exibirMensagemErro("Preencha o campo Nome");
            }
        } else {
            exibirMensagemErro("Localização");
        }
    }

    private void verificarPlacaExistente() {
        Item.verificarPlacaExistente(item.getPlaca()).addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful()) {
                    if (task.getResult()) {
                        Toast.makeText(CadastrarItens.this, "A placa já está em uso.", Toast.LENGTH_SHORT).show();
                    } else {
                        validarOutrasInformacoes();
                    }
                } else {
                    Toast.makeText(CadastrarItens.this, "Erro ao verificar a placa: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void validarOutrasInformacoes() {
        if (!item.getObservacao().isEmpty()) {
            gerarEExibirQRCode();
        } else {
            exibirMensagemErro("Preencha o campo descrição");
        }
    }

    private void gerarEExibirQRCode() {
        StringBuilder qrCodeDataBuilder = new StringBuilder();
        qrCodeDataBuilder.append("Vistoria\n");

        qrCodeDataBuilder.append("Nome do responsável: ").append(vistorias.getNomePerfilU()).append('\n');
        qrCodeDataBuilder.append("Localização: ").append(vistorias.getLocalizacao()).append('\n');
        qrCodeDataBuilder.append("Data: ").append(vistorias.getData()).append('\n');


        if (vistorias.getItens() != null && !vistorias.getItens().isEmpty()) {
            qrCodeDataBuilder.append("\nItens\n");
            for (Item item : vistorias.getItens()) {
                qrCodeDataBuilder.append("Nome do item: ").append(item.getNomeItem()).append('\n');
                // Adicione mais informações do item, se necessário.
            }
        }

        String qrCodeData = qrCodeDataBuilder.toString();
        Bitmap qrCodeBitmap = generateQRCode(qrCodeData);
        if (qrCodeBitmap != null) {
            salvarQRCodeStorage(qrCodeBitmap);
            Intent intent = new Intent(CadastrarItens.this, ViewQRCodeActivity.class);
            intent.putExtra("qrCodeData", qrCodeData);
            startActivity(intent);
        }

        recreate();
    }


    private void exibirMensagemErro(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }



 // metodo que clica na imagem, e abre e as abrem
    @Override
    public void onClick(android.view.View v) {
        Log.d("onClick", "onClick:" + v.getId());
        switch (v.getId()) {
            case R.id.imageCada1:

                adicionarImagem();
                Log.d("onClick", "onClick:" + v.getId());
                break;
        }

    }
    private Bitmap generateQRCode(String text) {
        int width = 500;
        int height = 500;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
    private void incrementItemCount() {
        itemCount++;
        itemCountTextView.setText("Itens adicionados: " + itemCount);
    }
    // verifica se a placa é duplicada
    private boolean isPlacaDuplicada(String placa) {
        for (Item i : vistoriaAtual.getItens()) {
            if (i.getPlaca().equalsIgnoreCase(placa)) {
                return true;
            }
        }
        return false;
    }
    private void salvarQRCodeStorage(Bitmap qrCodeBitmap) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, bao);
        byte[] dadosQRCode = bao.toByteArray();

        final StorageReference qrCodeAnuncio = storage
                .child("qrcodes")
                .child("Itens")
                .child(vistorias.getIdVistoria())
                .child("qrcode");

        UploadTask uploadTask = qrCodeAnuncio.putBytes(dadosQRCode);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                qrCodeAnuncio.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String qrCodeURL = uri.toString();
                        vistorias.setQrCodeURL(qrCodeURL);
                        vistorias.salvar();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                exibirMensagemErro("Falha ao fazer upload do QR Code");
                Log.e("INFO", "Falha ao fazer upload do QR Code:" + e.getMessage());
            }
        });
    }


    private void adicionarImagem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione uma opção:");
        builder.setPositiveButton("Câmera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (ContextCompat.checkSelfPermission(CadastrarItens.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intentCamera, seleCame);
                } else {
                    ActivityCompat.requestPermissions(CadastrarItens.this, new String[]{Manifest.permission.CAMERA}, seleCame);
                }
            }
        });

        builder.setNegativeButton("Galeria", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (ContextCompat.checkSelfPermission(CadastrarItens.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intentGaleria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intentGaleria, seleGale);
                } else {
                    ActivityCompat.requestPermissions(CadastrarItens.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, seleGale);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bitmap imagemBitmap = null;
            try {
                switch (requestCode) {
                    case seleCame:
                        imagemBitmap = (Bitmap) data.getExtras().get("data");
                        break;
                    case seleGale:
                        Uri localImagemSelecionada = data.getData();
                        imagemBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                        break;
                }
                if (imagemBitmap != null) {
                    imagens.add(imagemBitmap);
                    ImageView novaImagem = new ImageView(CadastrarItens.this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageSize, imageSize);
                    layoutParams.setMargins(8, 0, 8, 0);
                    novaImagem.setLayoutParams(layoutParams);
                    novaImagem.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    novaImagem.setImageBitmap(imagemBitmap);
                    ((ViewGroup) imageContainer.getChildAt(0)).addView(novaImagem);

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void recriarLayoutImagens() {
        LinearLayout imagesLayout = findViewById(R.id.imagesLayout);
        ViewGroup parent = (ViewGroup) imagesLayout.getParent();

        // Remove o LinearLayout atual
        parent.removeView(imagesLayout);

        // Cria um novo LinearLayout com as mesmas configurações
        LinearLayout newImagesLayout = new LinearLayout(this);
        newImagesLayout.setId(R.id.imagesLayout);
        newImagesLayout.setLayoutParams(imagesLayout.getLayoutParams());
        newImagesLayout.setOrientation(imagesLayout.getOrientation());

        // Adiciona o novo LinearLayout ao mesmo ViewGroup do antigo
        parent.addView(newImagesLayout);

        // Adiciona o CircleImageView ao novo LinearLayout
        de.hdodenhof.circleimageview.CircleImageView newImageCada1 = new de.hdodenhof.circleimageview.CircleImageView(this);
        newImageCada1.setId(R.id.imageCada1);
        newImageCada1.setLayoutParams(imageCada1.getLayoutParams());
        newImageCada1.setScaleType(imageCada1.getScaleType());
        newImageCada1.setImageResource(R.drawable.camera);
        newImageCada1.setBorderColor(imageCada1.getBorderColor());
        newImagesLayout.addView(newImageCada1);

        // Atualiza a referência do CircleImageView e do LinearLayout
        newImageCada1.setOnClickListener(v -> adicionarImagem());
        imagesLayout = newImagesLayout;
    }


    // carrega A localização
    public void carregarSpi() {
        String[] Localização = getResources().getStringArray(R.array.Localização);
        ArrayAdapter<String> Adapter2 = new ArrayAdapter<String>(
                this, R.layout.spinner_selected_item, Localização
        );

        Adapter2.setDropDownViewResource(R.layout.spinner_item);
        campoLocalizacao.setAdapter(Adapter2);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION_CODE) {
            boolean locationPermissionDenied = false;
            for (int permissaoResultado : grantResults) {
                if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                    locationPermissionDenied = true;
                    break;
                }
            }

            if (locationPermissionDenied) {
                alertaPermissao();
            } else {
                // Permissão concedida, você pode prosseguir com a obtenção da localização
            }
        }
    }


    private void alertaPermissao() {
        AlertDialog.Builder bul = new AlertDialog.Builder(this);
        bul.setTitle("Permissões Negadas");
        bul.setMessage("Para Usar o App é necessário aceitar as permissões");
        bul.setCancelable(false);
        bul.setPositiveButton("Aceito", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                finish();
            }
        });

        AlertDialog dial = bul.create();
        dial.show();
    }
    private void setupLocationManager() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }
    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        Log.e("INFO", "onLocationChanged - Latitude: " + currentLatitude);
        Log.e("INFO", "onLocationChanged - Longitude: " + currentLongitude);
        // Aqui você pode atualizar a localização do item no objeto Anuncios
    }
    private void iniciarCamponentes() {
        itemCountTextView = findViewById(R.id.itemCountTextView);
        campoNome = findViewById(R.id.editNome);
        imageCada1 = findViewById(R.id.imageCada1);
        imageContainer = findViewById(R.id.imageContainer);
        //campoanimais = findViewById(R.id.tipo);
        campoLocalizacao = findViewById(R.id.localizacao);
        campoData = findViewById(R.id.fone);
        botaoAdicionarItem=findViewById(R.id.adicionarItemVistoria);
        campoNomeRes = findViewById(R.id.editNomeRes);
        campoPlaca = findViewById(R.id.editPlaca);
        salvar=findViewById(R.id.finalizarVistoria);
        Locale locale = new Locale("pt", "BR");
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        //imageContainer.addView(linearLayout);
        campoObs = findViewById(R.id.editObs);

        findViewById(R.id.imageCada1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adicionarImagem();
            }
        });

        botaoAdicionarItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adicionarItemVistoria(v);
            }
        });
        salvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FinalizarVistoria(v);
            }
        });

    }

    @Override
    public void onProviderEnabled(String provider) {
        // Este método é chamado quando o provedor de localização é ativado
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Este método é chamado quando o provedor de localização é desativado
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Este método é chamado quando o status do provedor de localização muda
    }
}
