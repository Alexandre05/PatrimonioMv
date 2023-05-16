package Atividades;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Adaptadores.AdapterVistorias;
import Ajuda.ConFirebase;
import Modelos.RecyclerItemClickListener;
import Modelos.Vistoria;
import br.com.patrimoniomv.R;
import br.com.patrimoniomv.databinding.ActivityMeusAnimaisBinding;

public class MinhasVistorias extends AppCompatActivity {
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;
    private ActivityMeusAnimaisBinding binding;
    private RecyclerView recyclerViewVistorias;
    private AlertDialog alertDialog;

    private List<Vistoria> Listadevistorias = new ArrayList<>();
    private AdapterVistorias adapterVistorias;
    private ValueEventListener valueEventListenerVistorias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_animais);
        setTitle("Minhas Vistorias");

        binding = ActivityMeusAnimaisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.d("MinhasVistorias", "Componentes inicializados com sucesso");
        initializeComponents();
        recuperarAnuncios();
        binding.fab.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), CadastrarItens.class));
        });

        binding.impriB.setOnClickListener(view -> {
            Intent intent = new Intent(MinhasVistorias.this, ImprimirActivity.class);
            startActivity(intent);
        });

        recyclerViewVistorias.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerViewVistorias,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Log.d("MinhasVistorias", "Item clicado na posição: " + position);
                                Vistoria vistoriaSelecionada = Listadevistorias.get(position);
                                Log.d("MinhasVistorias", "Vistoria selecionada: " + vistoriaSelecionada.getIdVistoria());
                                Intent intent = new Intent(getApplicationContext(), DetalhesMinhasVistoriasAc.class);
                                intent.putExtra("vistorias", vistoriaSelecionada);
                                startActivity(intent);
                                Log.d("MinhasVistorias", "Iniciando atividade: DetalhesMinhasVistoriasAc");
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                Log.d("MinhasVistorias", "Item de longo clique na posição: " + position);
                                new AlertDialog.Builder(MinhasVistorias.this)
                                        .setMessage("Tem certeza que deseja excluir o item?")
                                        .setCancelable(false)
                                        .setPositiveButton("Sim", (dialog, id) -> {
                                            Vistoria vistoriaSelecionada = Listadevistorias.get(position);
                                            String vistoriaId = vistoriaSelecionada.getIdVistoria();
                                            Log.d("MinhasVistorias", "Excluindo vistoria: " + vistoriaId);
                                            DatabaseReference anunciosUsuarioRef = ConFirebase.getFirebaseDatabase().child("vistorias");
                                            anunciosUsuarioRef.child(vistoriaId).removeValue();
                                            vistoriaSelecionada.remover();
                                            Listadevistorias.remove(position);
                                            adapterVistorias.notifyItemRemoved(position);
                                            Log.d("MinhasVistorias", "Vistoria excluída, atualizando anúncios...");
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

    private void atualizarAdapter() {
        Log.d("MinhasVistorias", "Atualizando Adapter...");
        if (adapterVistorias != null) {
            adapterVistorias.notifyDataSetChanged();
            Log.d("MinhasVistorias", "Adapter atualizado com sucesso");
        } else {
            Log.d("MinhasVistorias", "Adapter é null. Não pode ser atualizado");
        }
    }


    private void showProgressDialog() {
        Log.d("MinhasVistorias", "Mostrando ProgressDialog...");
        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(this)
                    .setMessage("Recuperando Minhas Vistorias...")
                    .setCancelable(false)
                    .create();
            Log.d("MinhasVistorias", "ProgressDialog criado");
        }
        alertDialog.show();
        Log.d("MinhasVistorias", "ProgressDialog mostrado");
    }

    private void recuperarAnuncios() {

        Log.d("MinhasVistorias", "Recuperando anúncios...");
        showProgressDialog();

        DatabaseReference vistoriasRef = ConFirebase.getFirebaseDatabase()
                .child("vistorias");

        String currentUserId = ConFirebase.getIdUsuario();
        Log.d("MinhasVistorias", "ID do usuário logado: " + currentUserId);

        Query vistoriasQuery = vistoriasRef.orderByChild("idUsuario").equalTo(currentUserId);

        valueEventListenerVistorias = vistoriasQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Listadevistorias.clear();
                if (dataSnapshot.exists()) {
                    Log.d("MinhasVistorias", "DataSnapshot: " + dataSnapshot.toString());

                    for (DataSnapshot vistoriaSnapshot : dataSnapshot.getChildren()) {
                        Vistoria vistoria;

                        try {
                            vistoria = vistoriaSnapshot.getValue(Vistoria.class);
                        } catch (DatabaseException e) {
                            // Se ocorrer uma exceção ao converter os campos, pule esta vistoria
                            Log.d("MinhasVistorias", "Erro ao converter a vistoria: " + e.getMessage());
                            continue;
                        }

                        if (vistoria != null) {
                            Log.d("MinhasVistorias", "Vistoria encontrada: " + vistoria.getIdVistoria() + ", ID do usuário: " + vistoria.getIdUsuario());
                            Listadevistorias.add(vistoria);
                            Log.d("MinhasVistorias", "Vistoria adicionada: " + vistoria.getIdVistoria());
                        }
                    }
                    Collections.reverse(Listadevistorias);

                    // Atualize o adaptador aqui
                    atualizarAdapter();
                } else {
                    Log.d("MinhasVistorias", "Nenhuma vistoria encontrada");
                }
                alertDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("MinhasVistorias", "Erro: " + error.getMessage());
                alertDialog.dismiss();
            }
        });
    }
        private void initializeComponents() {
        recyclerViewVistorias = findViewById(R.id.recyclerViewVistorias);
        Listadevistorias = new ArrayList<>();
        adapterVistorias = new AdapterVistorias(Listadevistorias, this);
        recyclerViewVistorias.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewVistorias.setHasFixedSize(true);
        recyclerViewVistorias.setAdapter(adapterVistorias);

    }
}
