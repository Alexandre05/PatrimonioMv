package Atividades;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;

import Adaptadores.AdapterItensVistoriaParaEditar;
import Modelos.Item;
import Modelos.Vistoria;
import br.com.patrimoniomv.R;


public class DetalhesAc extends AppCompatActivity {

    private TextView nome, ob, porte, localiza, nomeVistoriador, latitudeTextView, longitudeTextView;
    private Item item;
    private ImageView fotoA;
    private String idVistoria;

    private RecyclerView recyclerView;
    private AdapterItensVistoriaParaEditar detalhesVistoriaAdapter;
    //private Button btnViewOnMapa;
    private ArrayList<Item> itens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes);
        iniciarComponentesUI();

        // Verificar se o usuário está logado
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Intent loginIntent = new Intent(DetalhesAc.this, Login.class);
            startActivity(loginIntent);
            finish();
            return;


    }



        recyclerView = findViewById(R.id.recyclerViewItens);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itens = new ArrayList<>();
        detalhesVistoriaAdapter = new AdapterItensVistoriaParaEditar(itens, this);
        recyclerView.setAdapter(detalhesVistoriaAdapter);
        ;

        String idVistoria = getIntent().getStringExtra("idVistoria");
        String localizacao = getIntent().getStringExtra("localizacao");
        Log.d("DETAILED_ACTIVITY", "idVistoria: " + idVistoria);
        Log.d("DETAILED_ACTIVITY", "localizacao 1: " + localizacao);



        Serializable serializable = getIntent().getSerializableExtra("itensList");
        if (serializable instanceof ArrayList) {
            ArrayList<Item> itensRecebidos = (ArrayList<Item>) serializable;
            Log.d("DETAILED_ACTIVITY", "itensRecebidos: " + itensRecebidos);
            if (itensRecebidos != null) {
                itens.addAll(itensRecebidos);
                detalhesVistoriaAdapter.notifyDataSetChanged();
            } else {
                // Você pode optar por manter a consulta ao Firebase aqui como um fallback,
                // caso a lista de itens não seja recebida corretamente.
                carregarDados(idVistoria, localizacao);
            }
        } else {
            Log.e("DETAILED_ACTIVITY", "Objeto serializável não é uma instância de List");
        }
    }


        private void carregarDados(String idVistoria, String localizacao) {
        Log.d("DETAILED_ACTIVITY", "idVistoria: " + idVistoria);
        Log.d("DETAILED_ACTIVITY", "localizacao 3: " + localizacao);

        if (idVistoria == null) {
            Log.e("DETAILED_ACTIVITY", "idVistoria é nulo");
            return;
        }

        DatabaseReference vistoriasRef = FirebaseDatabase.getInstance().getReference("vistorias")
                .child(idVistoria);
        DatabaseReference vistoriasConcluidasRef = FirebaseDatabase.getInstance().getReference("vistoriasConcluidas")
                .child(idVistoria);

        Log.d("DETAILED_ACTIVITY", "Iniciando consulta ao Firebase...");
        attachEventListener(vistoriasRef);
        attachEventListener(vistoriasConcluidasRef);
    }

    private void attachEventListener(DatabaseReference vistoriasRef) {
        vistoriasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("DETAILED_ACTIVITY", "dataSnapshot: " + dataSnapshot.toString());
                if (dataSnapshot.exists()) {
                    Vistoria vistoria = dataSnapshot.getValue(Vistoria.class);
                    exibirDados(vistoria);

                    // Buscar os itens
                    DatabaseReference itensRef;
                    if (dataSnapshot.hasChild("itensMap")) {
                        itensRef = dataSnapshot.child("itensMap").getRef();
                    } else {
                        itensRef = dataSnapshot.child("itens").getRef();
                    }
                    itensRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            itens.clear();
                            Log.d("DETAILED_ACTIVITY", "dataSnapshot (itens): " + dataSnapshot.toString());
                            for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                                String itemId = itemSnapshot.getKey(); // Adicionando busca do ID do Item
                                Item item = itemSnapshot.getValue(Item.class);
                                item.setId(itemId);

                                Log.d("DETAILED_ACTIVITY", "Item encontrado: " + item.toString());
                                itens.add(item);
                            }
                            detalhesVistoriaAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.w("DETAILED_ACTIVITY", "Erro ao carregar dados dos itens.", databaseError.toException());
                        }
                    });

                    detalhesVistoriaAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("DETAILED_ACTIVITY", "Erro ao carregar dados da vistoria.", databaseError.toException());
            }
        });
    }

    private void exibirDados(Vistoria vistoriaSelecionada) {
        if (vistoriaSelecionada != null) {
            Log.d("DETAILED_ACTIVITY", "vistoria: " + vistoriaSelecionada.toString());

            if (vistoriaSelecionada.getNomePerfilU() != null) {
                nomeVistoriador.setText(vistoriaSelecionada.getNomePerfilU());
            } else {
                nomeVistoriador.setText(getString(R.string.unknown_user));
            }

            if (vistoriaSelecionada.getLocalizacao() != null) {
                localiza.setText(vistoriaSelecionada.getLocalizacao());
            } else {
                localiza.setText(getString(R.string.unknown_location));
            }

        } else {
            Log.e("DETAILED_ACTIVITY", "Vistoria selecionada é nula");
        }
    }

    private void iniciarComponentesUI() {
        nomeVistoriador = findViewById(R.id.vistoriadorA);
        localiza = findViewById(R.id.localizacaoA);
        latitudeTextView = findViewById(R.id.latitude);
        longitudeTextView = findViewById(R.id.longitude);
        fotoA = findViewById(R.id.fotoA);
        recyclerView = findViewById(R.id.recyclerViewItens);
        //btnViewOnMapa = findViewById(R.id.BtnViewOnMa);
    }

}

