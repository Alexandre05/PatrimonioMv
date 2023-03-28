package Atividades;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media2.exoplayer.external.util.Log;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Adapter.AdapterAnuncios;
import Helper.ConFirebase;
import Mode.ItensVistorias;
import Mode.RecyclerItemClickListener;
import Mode.Usuario;
import br.com.patrimoniomv.R;
import br.com.patrimoniomv.databinding.ActivityMeusAnimaisBinding;


public class MinhasVistorias extends AppCompatActivity {
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMeusAnimaisBinding binding;
    private ActivityMeusAnimaisBinding binding2;
    private DatabaseReference anunciosUsuarioRef;
    private ItensVistorias anuncioSele;
    private RecyclerView recycleAnuncios;
    private AlertDialog alert;
    private Button irOutraSala;


    private Usuario usuario;
    private List<ItensVistorias> anuncios = new ArrayList<>();
    private AdapterAnuncios adapterAnuncios;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMeusAnimaisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        inicializarCompo();
        //setSupportActionBar(binding.toolbar);
        this.setTitle("Minhas Vistorias");


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getApplicationContext(), CadastrarItens.class));
            }
        });

        binding.impriB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MinhasVistorias.this, ImprimirActivity.class);
                startActivity(intent);
            }
        });

        recycleAnuncios.setLayoutManager(new LinearLayoutManager(this));
        recycleAnuncios.setHasFixedSize(true);
        adapterAnuncios = new AdapterAnuncios(anuncios, this);
        recycleAnuncios.setAdapter(adapterAnuncios);

        recuperarAnucnis();
        recycleAnuncios.addOnItemTouchListener(

                new RecyclerItemClickListener(
                        this,
                        recycleAnuncios,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                ItensVistorias anuncioSelecionado = anuncios.get(position);
                                Intent intent = new Intent(getApplicationContext(), AtualizarActivity.class);
                                intent.putExtra("anuncio", anuncioSelecionado);
                                startActivity(intent);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MinhasVistorias.this);
                                builder.setMessage("Tem certeza que deseja excluir o item?")
                                        .setCancelable(false)
                                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                ItensVistorias anunciosSelecionado = anuncios.get(position);
                                                anunciosSelecionado.remover();
                                                adapterAnuncios.notifyDataSetChanged();
                                            }
                                        })
                                        .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }


                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                            }
                        }
                )
        );


    }


    private void recuperarAnucnis() {
        alert = new AlertDialog.Builder(this)
                .setMessage("Recuperando Meus Anuncios...")
                .setCancelable(false)
                .show();
        alert.show();
        anunciosUsuarioRef = ConFirebase.getFirebaseDatabase()
                // aqui pega e mostra os meus anuncios
                .child("anuncios")
                .child(ConFirebase.getIdUsuario());

        anunciosUsuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                anuncios.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    anuncios.add(ds.getValue(ItensVistorias.class));

                }
                Collections.reverse(anuncios);
                adapterAnuncios.notifyDataSetChanged();
                alert.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void inicializarCompo() {

        recycleAnuncios = findViewById(R.id.reclyAnim);


    }


}