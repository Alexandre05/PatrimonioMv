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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;



import Ajuda.ConFirebase;
import Modelos.Usuario;
import br.com.patrimoniomv.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class Perfil extends AppCompatActivity {
    private ImageButton imagemCamera, imageGaleria;
    private EditText nomeUs,editIdade, editCPF, editSexo, editEndereco,editSobrenome;

    private CircleImageView perfil;

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


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle("Perfil");
        // recuper dados do usuario
       usuarioLogado= ConFirebase.getDadosUsarioLogado();


        inicarCampos();
        carregarDadosUsuario();
        FirebaseUser usuario = ConFirebase.getUsuarioAtaul();

        nomeUs.setText(usuario.getDisplayName());




        storageReference= ConFirebase.getFirebaseStorage();
        identificadorUsuario = ConFirebase.getIdentificarUsaurio();


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
                String sobrenome = editSobrenome.getText().toString();
                String idade = editIdade.getText().toString();
                String cpf = editCPF.getText().toString();
                String sexo = editSexo.getText().toString();
                String endereco = editEndereco.getText().toString();

                usuarioLogado.setNome(nome);
                usuarioLogado.setSobrenome(sobrenome);
                usuarioLogado.setIdade(idade);
                usuarioLogado.setCpf(cpf);
                usuarioLogado.setSexo(sexo);
                usuarioLogado.setEndereco(endereco);

                if (usuarioLogado != null) {
                    usuarioLogado.atualizar();
                    Toast.makeText(Perfil.this, "Dados atualizados com sucesso", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("AtualizarUsuario", "usuarioLogado é nulo.");
                    Toast.makeText(Perfil.this, "Erro ao atualizar os dados", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }



    private void inicarCampos() {
        imagemCamera = findViewById(R.id.imageCamera);
        imageGaleria = findViewById(R.id.imageGaleria);
        nomeUs = findViewById(R.id.editNomeUsario);
        editIdade = findViewById(R.id.editIdade);
        editCPF = findViewById(R.id.editCPF);
        editSexo = findViewById(R.id.editSexo);
        editEndereco = findViewById(R.id.editEndereco);
        editSobrenome = findViewById(R.id.editSobrenome);
        perfil = (CircleImageView) findViewById(R.id.fotoCadastroPerfil);
        atualizarNome = findViewById(R.id.butaoAtualizarNomePerfil);


    }

    private void carregarDadosUsuario() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            identificadorUsuario = currentUser.getUid();
        } else {
            // Tratar a situação em que o usuário não está logado
            return;
        }

        DatabaseReference usuarioRef = ConFirebase.getFirebaseDatabase().child("usuarios").child(identificadorUsuario);
        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                if (usuario != null) {
                    nomeUs.setText(usuario.getNome());
                    if (usuario.getSobrenome() != null) {
                        editSobrenome.setText(usuario.getSobrenome());
                    }
                    if (usuario.getIdade() != null) {
                        editIdade.setText(usuario.getIdade());
                    }
                    if (usuario.getCpf() != null) {
                        editCPF.setText(usuario.getCpf());
                    }
                    if (usuario.getSexo() != null) {
                        editSexo.setText(usuario.getSexo());
                    }
                    editEndereco.setText(usuario.getEndereco());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Tratar o erro aqui
            }
        });

        // Adicione o trecho de código abaixo para configurar a imagem e o nome do perfil
        Uri url = currentUser.getPhotoUrl();
        if (url != null) {
            Glide.with(Perfil.this)
                    .load(url)
                    .into(perfil);
        } else {
            perfil.setImageResource(R.drawable.camera);
        }
        nomeUs.setText(currentUser.getDisplayName());
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