package Atividades;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Adaptadores.VistoriaAndamentoAdapter;
import Ajuda.ConFirebase;
import Modelos.Item;
import Modelos.Vistoria;
import br.com.patrimoniomv.R;

public class VistoriasEmAndamentoActivity extends AppCompatActivity {

    private ListView vistoriasAndamentoListView;
    private boolean isProcessing = false;
    private RecyclerView vistoriasAndamentoRecyclerView;
    private Set<String> uniqueLicensePlates;

    private DatabaseReference mDatabase;
    private VistoriaAndamentoAdapter adapter;
    private List<Vistoria> vistoriasEmAndamento;
    private ChildEventListener vistoriasEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vistorias_em_andamento);
        uniqueLicensePlates = new HashSet<>();

        vistoriasAndamentoRecyclerView = findViewById(R.id.vistorias_andamento_recyclerview);

        checkUserAuthentication();
        vistoriasEmAndamento = new ArrayList<>();
        adapter = new VistoriaAndamentoAdapter(this, vistoriasEmAndamento);

        vistoriasAndamentoRecyclerView.setAdapter(adapter);



        mDatabase = FirebaseDatabase.getInstance().getReference();

        fetchVistorias();
    }

    private void checkUserAuthentication() {
        FirebaseUser currentUser = ConFirebase.getUsuarioAtaul();
        if (currentUser != null) {
            Log.d("Authentication", "Usuário autenticado: " + currentUser.getUid());
        } else {
            Log.d("Authentication", "Usuário não autenticado.");
            Intent intent = new Intent(VistoriasEmAndamentoActivity.this, Login.class);
            startActivity(intent);
            finish();
        }
    }

    private void fetchVistorias() {
        FirebaseUser currentUser = ConFirebase.getUsuarioAtaul();
        DatabaseReference vistoriasRef = mDatabase.child("vistorias");
        Query query = vistoriasRef.orderByChild("idUsuario").equalTo(currentUser.getUid());

        vistoriasEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Vistoria vistoria = dataSnapshot.getValue(Vistoria.class);
                if (vistoria != null) {
                    Map<String, Item> itensMap = new HashMap<>();
                    for (DataSnapshot itemSnapshot : dataSnapshot.child("itens").getChildren()) {
                        String key = itemSnapshot.getKey();
                        Item item = itemSnapshot.getValue(Item.class);
                        itensMap.put(key, item);
                    }
                    vistoria.setItensMap(itensMap);
                    vistoriasEmAndamento.add(vistoria);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        query.addChildEventListener(vistoriasEventListener);
    }


    public void concluirVistoria(int position) {
        Vistoria vistoria = vistoriasEmAndamento.get(position);
        vistoria.setConcluida(true);

        DatabaseReference vistoriaEmAndamentoRef = mDatabase.child("vistorias").child(vistoria.getLocalizacao());
        DatabaseReference vistoriasConcluidasRef = mDatabase.child("vistoriasConcluidas").child(vistoria.getIdVistoria());

        // Remover a vistoria em andamento
        vistoriaEmAndamentoRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Adicionar a vistoria ao nó "vistoriasConcluidas"
                vistoriasConcluidasRef.setValue(vistoria).addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        vistoriasEmAndamento.remove(position);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("VistoriaUpdateError", "Não foi possível adicionar a vistoria concluída", task2.getException());
                    }
                });
            } else {
                Log.e("VistoriaRemovalError", "Não foi possível remover a vistoria em andamento", task.getException());
            }
        });
    }

        @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vistoriasEventListener != null) {
            mDatabase.removeEventListener(vistoriasEventListener);
        }
    }
}

