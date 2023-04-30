package Atividades;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.LinkedHashMap;
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

    private Set<String> uniqueLicensePlates;

    private DatabaseReference mDatabase;
    private Button concluir;
    private VistoriaAndamentoAdapter adapter;
    private List<Vistoria> vistoriasEmAndamento;
    private ChildEventListener vistoriasEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vistorias_em_andamento);
        uniqueLicensePlates = new HashSet<>();

        vistoriasAndamentoListView = findViewById(R.id.vistorias_andamentoU);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //mDatabase.child("vistoriasConcluidas").setValue(true);
        checkUserAuthentication();
        // Carregar os dados das vistorias em andamento
        vistoriasEmAndamento = new ArrayList<>();
        Log.d("fetchVistorias", "onChildChanged called");
        vistoriasEventListener = null;
        fetchVistoriasEmAndamento();

    }

    private void checkUserAuthentication() {
        FirebaseUser currentUser = ConFirebase.getUsuarioAtaul();
        if (currentUser != null) {
            Log.d("Authentication", "Usuário autenticado: " + currentUser.getUid());
            // O usuário está autenticado, você pode prosseguir com o acesso aos dados
        } else {
            Log.d("Authentication", "Usuário não autenticado.");
            // Redirecione o usuário para a tela de login ou registre-se
            // Substitua LoginActivity.class pelo nome da sua atividade de login
            Intent intent = new Intent(VistoriasEmAndamentoActivity.this, Login.class);
            startActivity(intent);
            finish();
        }
    }

    private void fetchVistoriasEmAndamento() {
        vistoriasEmAndamento = new ArrayList<>();
        Map<Pair<String, String>, Vistoria> groupedVistorias = new LinkedHashMap<>();
        adapter = new VistoriaAndamentoAdapter(VistoriasEmAndamentoActivity.this, R.layout.itensvistoria, vistoriasEmAndamento);
        vistoriasAndamentoListView.setAdapter(adapter);
        Query query = mDatabase.child("vistorias");

        if (vistoriasEventListener != null) {
            query.removeEventListener(vistoriasEventListener);
        }

        vistoriasEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                processVistoriaSnapshot(dataSnapshot, groupedVistorias);
                updateVistoriasList(groupedVistorias);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                fetchVistoriasEmAndamento();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Trate os erros aqui
            }
        };
        query.addChildEventListener(vistoriasEventListener);
    }

    private void processVistoriaSnapshot(DataSnapshot dataSnapshot, Map<Pair<String, String>, Vistoria> groupedVistorias) {
        Log.d("fetchVistorias", "onChildAdded called");
        for (DataSnapshot vistoriaSnapshot : dataSnapshot.getChildren()) {
            Vistoria vistoria = vistoriaSnapshot.getValue(Vistoria.class);

            if (!vistoria.getConcluida() && !vistoria.getExcluidaVistoria()) {
                String date = vistoria.getData();
                String time = String.valueOf(vistoria.getHour());
                Pair<String, String> dateTimePair = new Pair<>(date, time);

                if (groupedVistorias.containsKey(dateTimePair)) {
                    Vistoria existingVistoria = groupedVistorias.get(dateTimePair);
                    addVistoriaIfNotInList(existingVistoria, vistoria);
                } else {
                    addNewVistoria(groupedVistorias, vistoria, date, time, dateTimePair);
                }
            }
        }
    }

    private void addVistoriaIfNotInList(Vistoria existingVistoria, Vistoria vistoria) {
        for (Map.Entry<String, Item> itemVistoriaEntry : vistoria.getItensMap().entrySet()) {
            boolean isInList = false;
            for (Map.Entry<String, Item> existingItemEntry : existingVistoria.getItensMap().entrySet()) {
                if (existingItemEntry.getKey().equals(itemVistoriaEntry.getKey())) {
                    isInList = true;
                    break;
                }
            }

            if (!isInList) {
                existingVistoria.getItensMap().put(itemVistoriaEntry.getKey(), itemVistoriaEntry.getValue());
            }
        }
    }


    private void addNewVistoria(Map<Pair<String, String>, Vistoria> groupedVistorias, Vistoria vistoria, String date, String time, Pair<String, String> dateTimePair) {
        Vistoria newVistoria = new Vistoria();
        newVistoria.setData(date);
        newVistoria.setNomePerfilU(vistoria.getNomePerfilU());
        newVistoria.setLocalizacao(vistoria.getLocalizacao());
        newVistoria.setItensMap(new HashMap<>());
        newVistoria.getItensMap().putAll(vistoria.getItensMap());
        newVistoria.setHour(Integer.parseInt(time));
        groupedVistorias.put(dateTimePair, newVistoria);
    }

    private void updateVistoriasList(Map<Pair<String, String>, Vistoria> groupedVistorias) {
        // Atualize a lista de vistorias em andamento e notifique o adaptador
        vistoriasEmAndamento.clear();
        vistoriasEmAndamento.addAll(groupedVistorias.values());
        adapter.notifyDataSetChanged();
    }



        public void concluirVistoria(Vistoria vistoriaAtual, int position) {
        if (isProcessing) {
            return;
        }
        isProcessing = true;
        vistoriaAtual.setConcluida(true); // Marque a vistoria atual como concluída
        // Atualize a lista de vistorias e notifique o adaptador
        adapter.notifyDataSetChanged();
        String localizacao = vistoriaAtual.getLocalizacao();
        String dataVistoria = vistoriaAtual.getData();
        String uniqueGroupKey = localizacao + "_" + dataVistoria;
        Log.d("DEBUG", "uniqueGroupKey: " + uniqueGroupKey);

        AlertDialog.Builder builder = new AlertDialog.Builder(VistoriasEmAndamentoActivity.this);
        builder.setTitle("Concluir Vistoria");
        builder.setMessage("Deseja concluir todas as vistorias da localização: " + localizacao + " na data: " + dataVistoria + "?");
        builder.setPositiveButton("Sim", (dialog, which) -> {
            DatabaseReference anunciosRef = ConFirebase.getFirebaseDatabase().child("vistorias");
            DatabaseReference vistoriasConcluidasRef = ConFirebase.getFirebaseDatabase().child("vistoriasConcluidas");

            anunciosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<Vistoria> vistoriasList = new ArrayList<>();
                    for (DataSnapshot inspectorSnapshot : dataSnapshot.getChildren()) {
                        String inspectorId = inspectorSnapshot.getKey();
                        DatabaseReference inspectorRef = ConFirebase.getFirebaseDatabase().child("vistorias").child(inspectorId);
                        Query query = inspectorRef.orderByChild("localizacao_data").equalTo(uniqueGroupKey);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot vistoriaSnapshot : dataSnapshot.getChildren()) {
                                        Vistoria vistoria = vistoriaSnapshot.getValue(Vistoria.class);
                                        if (vistoria != null && !vistoria.getConcluida()) {
                                            vistoriasList.add(vistoria);
                                            Log.d("DEBUG", "Vistoria item: " + vistoria);
                                        }
                                    }
                                } else {
                                    // Tratar a situação em que não há dados no dataSnapshot (por exemplo, exibir uma mensagem)
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Tratar o erro aqui
                                Log.d("DEBUG", "onCancelled - databaseError: " + databaseError);
                            }
                        });
                    }

                    Query vistoriasConcluidasQuery = vistoriasConcluidasRef.orderByChild("localizacao_data").equalTo(uniqueGroupKey);
                    vistoriasConcluidasQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Vistoria vistoriaConcluidaExistente = null;
                            String vistoriaConcluidaId = null;
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot vistoriaConcluidaSnapshot : dataSnapshot.getChildren()) {
                                    vistoriaConcluidaExistente = vistoriaConcluidaSnapshot.getValue(Vistoria.class);
                                    vistoriaConcluidaId = vistoriaConcluidaSnapshot.getKey();
                                    break;
                                }
                            }

                            Vistoria vistoriaConcluida;
                            if (vistoriaConcluidaExistente != null) {
                                vistoriaConcluida = combineVistorias(vistoriasList, vistoriaConcluidaExistente);
                            } else {
                                vistoriaConcluida = combineVistorias(vistoriasList, null);
                            }

                            vistoriaConcluida.setLocalizacao(localizacao);
                            vistoriaConcluida.setData(dataVistoria);
                            vistoriaConcluida.setConcluida(true);

                            if (vistoriaConcluidaId != null) {
                                vistoriasConcluidasRef.child(vistoriaConcluidaId).setValue(vistoriaConcluida.toMap());
                            } else {
                                vistoriaConcluidaId = vistoriasConcluidasRef.push().getKey();
                                if (vistoriaConcluidaId != null) {
                                    vistoriasConcluidasRef.child(vistoriaConcluidaId).setValue(vistoriaConcluida.toMap());
                                }
                            }

                            for (Vistoria vistoria : vistoriasList) {
                                String inspectorId = vistoria.getIdInspector();
                                String vistoriaId = vistoria.getIdVistoria();
                                if (inspectorId != null && vistoriaId != null) {
                                    anunciosRef.child(inspectorId).child(vistoriaId).child("concluida").setValue(true);
                                }
                            }

                            fetchVistoriasEmAndamento();
                            isProcessing = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Tratar o erro aqui
                            Log.d("DEBUG", "onCancelled - databaseError: " + databaseError);
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Tratar o erro aqui
                    Log.d("DEBUG", "onCancelled - databaseError: " + databaseError);
                }
            });
        });

        builder.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }


                private Vistoria combineVistorias(List<Vistoria> vistoriasList, Vistoria vistoriaConcluidaExistente) {
        if (vistoriasList.isEmpty()) {
            if (vistoriaConcluidaExistente != null) {
                return vistoriaConcluidaExistente;
            } else {
                throw new IllegalArgumentException("A lista de vistorias não pode estar vazia");
            }
        }

        Vistoria vistoriaConcluida;
        if (vistoriaConcluidaExistente != null) {
            vistoriaConcluida = vistoriaConcluidaExistente;
        } else {
            vistoriaConcluida = new Vistoria();
            Vistoria firstVistoria = vistoriasList.get(0);

            // Copiar campos que não serão modificados
            vistoriaConcluida.setIdVistoria(firstVistoria.getIdVistoria());
            vistoriaConcluida.setLocalizacao(firstVistoria.getLocalizacao());

            vistoriaConcluida.setData(firstVistoria.getData());



            // Inicializar campos que serão combinados
            vistoriaConcluida.setNomePerfilU(firstVistoria.getNomePerfilU());
            vistoriaConcluida.setIdInspector(firstVistoria.getIdInspector());
            vistoriaConcluida.setFotos(new ArrayList<>(firstVistoria.getFotos()));

        }

        for (int i = (vistoriaConcluidaExistente != null) ? 0 : 1; i < vistoriasList.size(); i++) {
            Vistoria vistoria = vistoriasList.get(i);

            // Verifique se a localização e data são as mesmas
            if (!vistoriaConcluida.getLocalizacao().equalsIgnoreCase(vistoria.getLocalizacao()) ||
                    !vistoriaConcluida.getData().equalsIgnoreCase(vistoria.getData())) {
                throw new IllegalArgumentException("As vistorias devem ter a mesma localização e data");
            }

            // Combine os campos relevantes
            if (!vistoriaConcluida.getIdInspector().equals(vistoria.getIdInspector())) {
                vistoriaConcluida.setIdInspector(vistoriaConcluida.getIdInspector() + "," + vistoria.getIdInspector());
            }
            if (!vistoriaConcluida.getNomePerfilU().equals(vistoria.getNomePerfilU())) {
                vistoriaConcluida.setNomePerfilU(vistoriaConcluida.getNomePerfilU() + "," + vistoria.getNomePerfilU());
            }
            vistoriaConcluida.getFotos().addAll(vistoria.getFotos());

        }

        return vistoriaConcluida;
    }
}