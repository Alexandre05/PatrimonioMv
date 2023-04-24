package Atividades;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Adaptadores.AdapterVistorias;
import Ajuda.ConFirebase;
import Modelos.Vistoria;
import Modelos.RecyclerItemClickListener;
import Modelos.Usuario;
import br.com.patrimoniomv.R;
import br.com.patrimoniomv.databinding.ActivityMeusAnimaisBinding;


public class MinhasVistorias extends AppCompatActivity {
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMeusAnimaisBinding binding;
    private ActivityMeusAnimaisBinding binding2;
    private DatabaseReference anunciosUsuarioRef;
    private Vistoria anuncioSele;
    private RecyclerView recyclerViewAnuncios;
    private AlertDialog alertDialog;
    private Button btnOutraSala;

    private Usuario usuario;
    private List<Vistoria> vistoriasList = new ArrayList<>();
    private AdapterVistorias adapterAnuncios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMeusAnimaisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeComponents();
        setTitle("Minhas Vistorias");

        binding.fab.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), CadastrarItens.class));
        });

        binding.impriB.setOnClickListener(view -> {
            Intent intent = new Intent(MinhasVistorias.this, ImprimirActivity.class);
            startActivity(intent);
        });

        recyclerViewAnuncios.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAnuncios.setHasFixedSize(true);
        adapterAnuncios = new AdapterVistorias(vistoriasList, this);
        recyclerViewAnuncios.setAdapter(adapterAnuncios);

        recuperarAnuncios();
        recyclerViewAnuncios.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerViewAnuncios,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Vistoria vistoriaSelecionada = vistoriasList.get(position);
                                Intent intent = new Intent(getApplicationContext(), Atualizar.class);
                                intent.putExtra("vistorias", vistoriaSelecionada);
                                startActivity(intent);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                new AlertDialog.Builder(MinhasVistorias.this)
                                        .setMessage("Tem certeza que deseja excluir o item?")
                                        .setCancelable(false)
                                        .setPositiveButton("Sim", (dialog, id) -> {
                                            Vistoria vistoriaSelecionada = vistoriasList.get(position);
                                            String vistoriaId = vistoriaSelecionada.getIdVistoria();
                                            anunciosUsuarioRef.child(vistoriaId).removeValue();
                                            vistoriaSelecionada.remover();
                                            adapterAnuncios.notifyDataSetChanged();
                                            recuperarAnuncios();
                                        })
                                        .setNegativeButton("Não", (dialog, id) -> dialog.cancel())
                                        .create()
                                        .show();
                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            }
                        }
                )
        );
    }
    private void recuperarAnuncios() {
        alertDialog = new AlertDialog.Builder(this)
                .setMessage("Recuperando Minhas Vistorias...")
                .setCancelable(false)
                .show();

        DatabaseReference vistoriasRef = ConFirebase.getFirebaseDatabase()
                .child("vistorias");

        vistoriasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                vistoriasList.clear();
                if (dataSnapshot.exists()) {
                    String currentUserId = ConFirebase.getIdUsuario();

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Vistoria vistoria;

                        try {
                            vistoria = ds.getValue(Vistoria.class);
                        } catch (DatabaseException e) {
                            // Se ocorrer uma exceção ao converter os campos, pule esta vistoria
                            continue;
                        }

                        if (vistoria != null && vistoria.getIdUsuario().equals(currentUserId)) {
                            vistoriasList.add(vistoria);
                        }
                    }
                    Collections.reverse(vistoriasList);
                }
                adapterAnuncios.notifyDataSetChanged();
                alertDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                alertDialog.dismiss();
            }
        });
    }




    private void initializeComponents() {
        recyclerViewAnuncios = findViewById(R.id.reclyAnim);
    }
}