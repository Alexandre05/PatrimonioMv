package Atividades;

import static android.graphics.ImageDecoder.decodeBitmap;

import android.Manifest;
import android.app.AlertDialog;
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
import com.google.firebase.database.DatabaseReference;
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
import java.util.List;
import java.util.Locale;


import Ajuda.ConFirebase;
import Ajuda.DataCuston;
import Modelos.ItensVistorias;
import Ajuda.Permissoes;
import Modelos.Usuario;
import br.com.patrimoniomv.R;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class CadastrarItens extends AppCompatActivity
        implements android.view.View.OnClickListener, LocationListener {
    private EditText campoNome, campoPorte, campoObs;
    private TextView campoNomeRes ;
    private CircleImageView imageCada1;
    private HorizontalScrollView imageContainer;
    private ItensVistorias anuncios;
    private Usuario usuario;
    private int imageSize;

    private AlertDialog dialog;
    private Usuario usuarioLogado;
    Bitmap imagem=null;
    Bitmap imagem2=null;
    private Button salvar;
    private TextView campoData;
    private List<Bitmap> imagens = new ArrayList<>();

    private static final int seleCame = 100;
    private static final int seleGale = 200;
    private Spinner campoanimais, campocidade;
    private StorageReference storage;
    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

    private List<String> listasFotoRe = new ArrayList<>();
    private List<String> listaURLFotos = new ArrayList<>();
    private LocationManager locationManager;
    private double latitude;
    private double longitude;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageSize = getResources().getDimensionPixelSize(R.dimen.image_size);

        setContentView(R.layout.activity_cadastrar_itens);
        Permissoes.validarPermissoes(permissoes, this, 1);
        FirebaseUser usuario = ConFirebase.getUsuarioAtaul();
        iniciarCamponentes();
        carregarSpi();

        storage = ConFirebase.getFirebaseStorage();
        usuarioLogado= ConFirebase.getDadosUsarioLogado();
        campoNomeRes.setText(usuario.getDisplayName());
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }


    }

// savar Anuncios
public void salvarVistorias() {
    dialog = new SpotsDialog.Builder(this)
            .setMessage("Salvando...")
            .setCancelable(false)
            .show();
    dialog.show();
    anuncios.setLatitude(latitude);
    anuncios.setLongetude(longitude);

    for (int i = 0; i < imagens.size(); i++) {
        Bitmap imagem = imagens.get(i);
        int tamanhoLista = imagens.size();
        salvarFotoStorage(imagem, tamanhoLista, i);
    }

}

    // salva
    private void salvarFotoStorage(Bitmap imagem, final int totalFotos, int contador) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        imagem.compress(Bitmap.CompressFormat.JPEG, 80, bao);
        byte[] dadosImagem = bao.toByteArray();
    final StorageReference imagemAnuncio = storage
            .child("imagens")
            .child("Itens")
            .child(anuncios.getIdAnuncio())
            .child("imagem" + contador);

    UploadTask uploadTask = imagemAnuncio.putBytes(dadosImagem);
    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            imagemAnuncio.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String urlConverted = uri.toString();
                    listaURLFotos.add(urlConverted);
                    anuncios.setFotos(listaURLFotos);
                    anuncios.salvar();
                    listasFotoRe.clear();
                    dialog.dismiss();
                    finish();

                }
            });
        }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            exibirMensagemErro("Falha ao fazer uplad");
            Log.e("INFO", "Falha ao fazer upload:" + e.getMessage());
        }
    });
}

    private ItensVistorias ConfigurarVistoria() {
        String recebe = ConFirebase.getDadosUsarioLogado().getNome();


        //String tipoIten = campoanimais.getSelectedItem().toString();
        String localizacao = campocidade.getSelectedItem().toString();
        String nomeItem = campoNome.getText().toString();
        String placa = campoPorte.getText().toString();
        String nomeCampo = campoNomeRes.getText().toString();

        usuarioLogado.setNome(nomeCampo);

        String outrasInformacoes = campoObs.getText().toString();
        //String fone = campoData.toString();

        // Inicialize o objeto Anuncios antes de configurar latitude e longitude.
        ItensVistorias anuncios = new ItensVistorias();

        anuncios.setLatitude(latitude);
        anuncios.setLongetude(longitude);
        //anuncios.setTipoItem(tipoIten);
        anuncios.setLocalizacao(localizacao);
        anuncios.setNomeItem(nomeItem);
        anuncios.setNomePerfilU(nomeCampo);

        anuncios.setOutrasInformacoes(outrasInformacoes);
        anuncios.setPlaca(placa);
        anuncios.setIdInspector(usuarioLogado.getIdU());

        anuncios.setData(DataCuston.dataAtual());
        anuncios.setLocalizacao_data(anuncios.getLocalizacao() + "_" + anuncios.getData());
        return anuncios;
    }


    public void validarVistorias(android.view.View view) {
        anuncios = ConfigurarVistoria();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                // Aqui você pode atualizar a localização do item no objeto Anuncios
            }
        }

        if (imagens.size() != 0) {
            if (!anuncios.getLocalizacao().isEmpty()) {
                if (!anuncios.getNomeItem().isEmpty()) {
                    ItensVistorias.verificarPlacaExistente(anuncios.getPlaca()).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                        @Override
                        public void onComplete(@NonNull Task<Boolean> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult()) {
                                    Toast.makeText(CadastrarItens.this, "A placa já está em uso.", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (!anuncios.getOutrasInformacoes().isEmpty()) {
                                        // Adicionar informações da vistoria em uma string e gerar o QR Code
                                        String qrCodeData = "Nome do item: " + anuncios.getNomeItem()
                                                + "\nPlaca: " + anuncios.getPlaca()
                                                + "\nNome do responsável: " + anuncios.getNomePerfilU()
                                                + "\nLocalização: " + anuncios.getLocalizacao()
                                                + "\nData: " + anuncios.getData()
                                                + "\nOutras informações: " + anuncios.getOutrasInformacoes();
                                        Bitmap qrCodeBitmap = generateQRCode(qrCodeData);
                                        if (qrCodeBitmap != null) {
                                            salvarQRCodeStorage(qrCodeBitmap);
                                            // imagens.add(qrCodeBitmap);
                                            // Adicione este código após adicionar o QR Code à lista de imagens
                                            Intent intent = new Intent(CadastrarItens.this, ViewQRCodeActivity.class);
                                            intent.putExtra("qrCodeData", qrCodeData);
                                            startActivity(intent);
                                        }

                                        salvarVistorias();
                                        recreate();

                                    } else {
                                        exibirMensagemErro("Preencha o campo descrição");
                                    }
                                }
                            } else {
                                Toast.makeText(CadastrarItens.this, "Erro ao verificar a placa: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    exibirMensagemErro("Preencha o campo Nome");
                }
            } else {
                exibirMensagemErro("Localização");
            }
        } else {
            exibirMensagemErro("Selecione ao menos uma foto!");
        }
    }

    private void exibirMensagemErro(String mensagem) {
        Toast.makeText(this,
                mensagem, Toast.LENGTH_SHORT).show();


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
    private void salvarQRCodeStorage(Bitmap qrCodeBitmap) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, bao);
        byte[] dadosQRCode = bao.toByteArray();

        final StorageReference qrCodeAnuncio = storage
                .child("qrcodes")
                .child("Itens")
                .child(anuncios.getIdAnuncio())
                .child("qrcode");

        UploadTask uploadTask = qrCodeAnuncio.putBytes(dadosQRCode);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                qrCodeAnuncio.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String qrCodeURL = uri.toString();
                        anuncios.setQrCodeURL(qrCodeURL);
                        anuncios.salvar();
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


    public void carregarSpi() {
        String[] Localização = getResources().getStringArray(R.array.Localização);
        ArrayAdapter<String> Adapter2 = new ArrayAdapter<String>(
                this, R.layout.spinner_selected_item, Localização
        );

        Adapter2.setDropDownViewResource(R.layout.spinner_item);
        campocidade.setAdapter(Adapter2);
    }

    private void iniciarCamponentes() {

        campoNome = findViewById(R.id.editNome);
        imageCada1 = findViewById(R.id.imageCada1);
        imageContainer = findViewById(R.id.imageContainer);
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
        //campoanimais = findViewById(R.id.tipo);
        campocidade = findViewById(R.id.cidade);
        campoData = findViewById(R.id.fone);
        campoNomeRes = findViewById(R.id.editNomeRes);
        campoPorte = findViewById(R.id.editPlaca);
        salvar=findViewById(R.id.SalvarA);
        Locale locale = new Locale("pt", "BR");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {

                alertaPermissao();

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

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        // Aqui você pode atualizar a localização do item no objeto Anuncios
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
