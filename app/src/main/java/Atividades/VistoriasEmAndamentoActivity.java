package Atividades;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import Adaptadores.VistoriaAndamentoAdapter;
import Ajuda.ConFirebase;
import Modelos.Item;
import Modelos.Vistoria;

import br.com.patrimoniomv.R;

public class VistoriasEmAndamentoActivity extends AppCompatActivity implements OnVistoriaCreatedListener  {


    private boolean isProcessing = false;
    private RecyclerView vistoriasAndamentoRecyclerView;
    private Set<String> uniqueLicensePlates;

    private DatabaseReference mDatabase;
    private VistoriaAndamentoAdapter adapter;
    private List<Vistoria> vistoriasEmAndamento;
    private ChildEventListener vistoriasEventListener;
    private ChildEventListener vistoriasPuEventListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vistorias_em_andamento);
        uniqueLicensePlates = new HashSet<>();

        vistoriasAndamentoRecyclerView = findViewById(R.id.vistorias_andamento_recyclerview);

        checkUserAuthentication();
        vistoriasEmAndamento = new ArrayList<>();
        adapter = new VistoriaAndamentoAdapter(this, vistoriasEmAndamento,true);

        vistoriasAndamentoRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        vistoriasAndamentoRecyclerView.setLayoutManager(layoutManager);
        vistoriasAndamentoRecyclerView.setHasFixedSize(true);
        vistoriasAndamentoRecyclerView.setAdapter(adapter);




        mDatabase = FirebaseDatabase.getInstance().getReference();

        fetchVistorias();

    }
    @Override
    public void onVistoriaCreated(Vistoria vistoria) {
        DatabaseReference vistoriasConcluidasRef = mDatabase.child("vistoriasConcluidas");

        vistoriasConcluidasRef.orderByChild("data").equalTo(vistoria.getData()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot vistoriaSnapshot: dataSnapshot.getChildren()) {
                    Vistoria vistoriaExistente = vistoriaSnapshot.getValue(Vistoria.class);
                    if (vistoriaExistente != null && vistoriaExistente.getLocalizacao().equals(vistoria.getLocalizacao())) {
                        Toast.makeText(VistoriasEmAndamentoActivity.this, "Já existe uma vistoria concluída com a mesma data e localização.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                // Se não encontrou uma vistoria com a mesma data e localização, adiciona a nova vistoria
                vistoriasEmAndamento.add(vistoria);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Erro ao verificar vistorias concluídas", databaseError.toException());
            }
        });
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
        //DatabaseReference vistoriaPuRef = mDatabase.child("vistoriaPu");

        vistoriasEventListener = createVistoriasEventListener();
        vistoriasPuEventListener = createVistoriasEventListener();

        vistoriasRef.orderByChild("idUsuario").equalTo(currentUser.getUid()).addChildEventListener(vistoriasEventListener);
        //vistoriaPuRef.orderByChild("idUsuario").equalTo(currentUser.getUid()).addChildEventListener(vistoriasPuEventListener);
    }



    private ChildEventListener createVistoriasEventListener() {
        return new ChildEventListener() {
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
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }
    public void concluirVistoria(int position) {
        Vistoria vistoria = vistoriasEmAndamento.get(position);
        vistoria.setConcluida(true);

        DatabaseReference vistoriasRef = mDatabase.child("vistorias");
        DatabaseReference vistoriasConcluidasRef = mDatabase.child("vistoriasConcluidas");
        DatabaseReference vistoriaPuRef = mDatabase.child("vistoriaPu");
        DatabaseReference vistoriasHistoricoRef = mDatabase.child("vistoriasHistorico");  // novo nó para histórico de vistorias

        vistoriasConcluidasRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                List<String> uniqueVistoriaIds = new ArrayList<>();

                for (Map.Entry<String, Item> entry : vistoria.getItensMap().entrySet()) {
                    String itemId = entry.getKey();
                    Item item = entry.getValue();

                    for (DataSnapshot vistoriaSnapshot : dataSnapshot.getChildren()) {
                        Vistoria vistoriaAntiga = vistoriaSnapshot.getValue(Vistoria.class);

                        Optional<Map.Entry<String, Item>> itemAntigoEntry = vistoriaAntiga.getItensMap().entrySet()
                                .stream()
                                .filter(e -> e.getValue().getPlaca().equals(item.getPlaca()))
                                .findFirst();

                        if (itemAntigoEntry.isPresent()) {
                            DatabaseReference itemRef = vistoriasConcluidasRef.child(vistoriaAntiga.getIdVistoria()).child("itensMap").child(itemAntigoEntry.get().getKey());
                            itemRef.removeValue();

                            // Se a vistoria antiga tem apenas um item, adicione-a ao histórico
                            if (vistoriaAntiga.getItensMap().size() == 1) {
                                vistoriasHistoricoRef.child(vistoriaAntiga.getIdVistoria()).setValue(vistoriaAntiga);
                                uniqueVistoriaIds.add(vistoriaAntiga.getIdVistoria());
                            } else { // Se a vistoria antiga tem mais de um item, adicione apenas o item ao histórico
                                vistoriasHistoricoRef.child(vistoriaAntiga.getIdVistoria()).child("itensMap").child(itemAntigoEntry.get().getKey()).setValue(itemAntigoEntry.get().getValue());
                            }
                        }
                    }

                    vistoriasConcluidasRef.child(vistoria.getIdVistoria()).child("itensMap").child(itemId).setValue(item);
                }

                vistoriasRef.child(vistoria.getIdVistoria()).removeValue();
                vistoriasConcluidasRef.child(vistoria.getIdVistoria()).setValue(vistoria);
                vistoriaPuRef.child(vistoria.getIdVistoria()).setValue(vistoria);

                for (String vistoriaId : uniqueVistoriaIds) {
                    vistoriasConcluidasRef.child(vistoriaId).removeValue(); // Remover a vistoria única do nó "vistoriasConcluidas"
                    vistoriaPuRef.child(vistoriaId).removeValue(); // Adicionado: Remover a vistoria do nó "vistoriaPu"
                }

                vistoriasEmAndamento.remove(position);
                adapter.notifyDataSetChanged();

            } else {
                Log.e("Firebase", "Erro ao obter vistorias concluídas", task.getException());
            }
        });
    }

           @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vistoriasEventListener != null) {
            mDatabase.removeEventListener(vistoriasEventListener);
        }
        if (vistoriasPuEventListener != null) { // Adicione estas duas linhas
            mDatabase.removeEventListener(vistoriasPuEventListener);
        }
    }

}

