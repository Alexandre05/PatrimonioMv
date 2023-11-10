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
import com.google.firebase.database.Query;
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

import Modelos.VistoriaAnexada;
import br.com.patrimoniomv.R;

public class VistoriasEmAndamentoActivity extends AppCompatActivity implements OnVistoriaCreatedListener {

    private boolean isProcessing = false;
    private RecyclerView vistoriasAndamentoRecyclerView;
    private Set<String> uniqueLicensePlates;
    private boolean vistoriaConcluida = false;
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
        adapter = new VistoriaAndamentoAdapter(this, vistoriasEmAndamento, true);

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

        // Verificar se já existe uma vistoria concluída com a mesma data e localização
        vistoriasConcluidasRef.orderByChild("data").equalTo(vistoria.getData()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot vistoriaSnapshot : dataSnapshot.getChildren()) {
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

    // Verifica se o usuário está autenticado
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

    // Recupera as vistorias em andamento do banco de dados
    private void fetchVistorias() {
        FirebaseUser currentUser = ConFirebase.getUsuarioAtaul();
        DatabaseReference vistoriasRef = mDatabase.child("vistorias");

        vistoriasEventListener = createVistoriasEventListener();

        // Consulta o banco para recuperar as vistorias do usuário atual
        vistoriasRef.orderByChild("idUsuario").equalTo(currentUser.getUid()).addChildEventListener(vistoriasEventListener);
    }

    // Cria um ouvinte de eventos para as vistorias
    private ChildEventListener createVistoriasEventListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Vistoria vistoria = dataSnapshot.getValue(Vistoria.class);
                if (vistoria != null) {
                    // Recupera os itens da vistoria
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
                // Lida com a mudança nas vistorias, se necessário
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Lida com a remoção de vistorias, se necessário
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Lida com a movimentação de vistorias, se necessário
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Lida com erros, se necessário
            }
        };
    }

    // Método chamado quando o usuário conclui uma vistoria
    public void concluirVistoria(int position) {
        Vistoria vistoria = vistoriasEmAndamento.get(position);
        vistoria.setConcluida(true);

        DatabaseReference vistoriasRef = mDatabase.child("vistorias");
        DatabaseReference vistoriasConcluidasRef = mDatabase.child("vistoriasConcluidas");
        DatabaseReference vistoriaPuRef = mDatabase.child("vistoriaPu");

        String localizacao = vistoria.getLocalizacao();
        String data = vistoria.getData();
        String vistoriadorId = vistoria.getIdUsuario();

        // Consulta o banco para vistorias anteriores com a mesma data, localização e vistoriador
        Query query = vistoriasRef.orderByChild("localizacao_data_idUsuario")
                .equalTo(localizacao + data + vistoriadorId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Itera sobre as vistorias anteriores
                for (DataSnapshot vistoriaSnapshot : dataSnapshot.getChildren()) {
                    Vistoria vistoriaAntiga = vistoriaSnapshot.getValue(Vistoria.class);
                    if (vistoriaAntiga != null) {
                        // Adiciona os itens da vistoria antiga à vistoria atual
                        Map<String, Item> itensMapAntiga = vistoriaAntiga.getItensMap();
                        vistoria.getItensMap().putAll(itensMapAntiga);

                        // Remove a vistoria antiga
                        vistoriaSnapshot.getRef().removeValue();
                    }
                }

                // Adiciona a vistoria atual às vistorias concluídas
                String idVistoria = vistoriasConcluidasRef.push().getKey();
                vistoriasConcluidasRef.child(idVistoria).setValue(vistoria);

                // Adiciona a vistoria atual à vistoriaPu
                vistoriaPuRef.child(idVistoria).setValue(vistoria);

                // Remove a vistoria em andamento
                vistoriasEmAndamento.remove(position);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Erro ao verificar vistorias anteriores", databaseError.toException());
            }
        });
    }
}