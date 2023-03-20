package Atividades;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;



import Helper.ConFirebase;
import Helper.Permissoes;
import Mode.Usuario;
import br.com.patrimoniomv.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class Perfil extends AppCompatActivity {
    private ImageButton imagemCamera, imageGaleria;
    private EditText editeNome;
    private CircleImageView perfil;
    private EditText nomeUs;
    private Usuario usuarioLogado;
    private Button atualizarNome;
    private String identificadorUsuario;
    private StorageReference storageReference;
    private static final int seleCame = 100;
    private static final int seleGale = 200;
    private String[] permissoes = new String[]{

            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle("Perfil");
        // recuper dados do usuario
        usuarioLogado= ConFirebase.getDadosUsarioLogado();

        atualizarNome = findViewById(R.id.butaoAtualizarNomePerfil);
        perfil=findViewById(R.id.fotoCadastroPerfil);
        nomeUs= findViewById(R.id.editNomeUsario);

        FirebaseUser usuario = ConFirebase.getUsuarioAtaul();
        Uri url =usuario.getPhotoUrl();
        if (url !=null){

            Glide.with(Perfil.this)
                    .load(url)
                    .into(perfil);

        }else {
            perfil.setImageResource(R.drawable.camera);


        }
        nomeUs.setText(usuario.getDisplayName());


        storageReference= ConFirebase.getFirebaseStorage();
        identificadorUsuario = ConFirebase.getIdentificarUsaurio();
        inicarCampos();

        imagemCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ação abrir camera

                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                startActivityForResult(i, seleCame);


            }
        });


        imageGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ação abri galeria

                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);


                startActivityForResult(i, seleGale);

            }
        });

        atualizarNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = nomeUs.getText().toString();

                boolean retonor = ConFirebase.AtualizarNomeUsuario(nome);
                if (retonor){

                    usuarioLogado.setNome(nome);
                    usuarioLogado.atualizar();


                    Toast.makeText(Perfil.this,
                            "Nome Atualizado com Sucesso",
                            Toast.LENGTH_SHORT).show();


                }
            }
        });
    }



    private void inicarCampos() {
        imagemCamera = findViewById(R.id.imageCamera);
        imageGaleria = findViewById(R.id.imageGaleria);
        editeNome = findViewById(R.id.editNomeUsario);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {


            Bitmap imagem = null;

            try {
                switch (requestCode) {
                    case seleCame:

                        imagem = (Bitmap) data.getExtras().get("data");
                        break;

                    case seleGale:
                        if (android.os.Build.VERSION.SDK_INT >= 29) {

                            Uri localImagemSe = data.getData();
                            imagem = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), localImagemSe));
                            break;
                        }

                }
                if (imagem != null) {

                    perfil.setImageBitmap(imagem);
                    // recupera dados da imagem no para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    byte[] dadosImagem = baos.toByteArray();


                    final StorageReference imageRef = storageReference.
                            child("imagens")
                            .child("perfil")
                            .child(identificadorUsuario)
                            .child("perfil.jpeg");

                    UploadTask uploadTask = imageRef.putBytes(dadosImagem);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(Perfil.this,
                                    "Sucesso ao fazer upload da imagem",
                                    Toast.LENGTH_SHORT).show();

                            imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri url = task.getResult();
                                    AtualizafotoUsuario(url);
                                }
                            });
                        }
                    });

                }
            } catch (Exception e) {
                e.printStackTrace();

            }

        }

    }

    private void AtualizafotoUsuario(Uri url) {
        boolean retorno= ConFirebase.AtualizarFotoUsuario(url);
        if (retorno){
            usuarioLogado.setFoto(url.toString());
            usuarioLogado.atualizar();
            Toast.makeText(Perfil.this,
                    "Foto Alterado",
                    Toast.LENGTH_SHORT).show();

        }


    }
}