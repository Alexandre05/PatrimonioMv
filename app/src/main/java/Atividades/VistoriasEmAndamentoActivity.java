package Atividades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Adaptadores.VistoriaAndamentoAdapter;
import Ajuda.ConFirebase;
import Modelos.ItensVistorias;
import br.com.patrimoniomv.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class VistoriasEmAndamentoActivity extends AppCompatActivity {

    private ListView vistoriasAndamentoListView;
    private boolean isProcessing = false;

    private Set<String> uniqueLicensePlates;

    private DatabaseReference mDatabase;
    private Button concluir;
    private VistoriaAndamentoAdapter adapter;
    private List<ItensVistorias> vistoriasEmAndamento;
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
        Map<Pair<String, String>, ItensVistorias> groupedVistorias = new LinkedHashMap<>();
        adapter = new VistoriaAndamentoAdapter(VistoriasEmAndamentoActivity.this, R.layout.itensvistoria, vistoriasEmAndamento);
        vistoriasAndamentoListView.setAdapter(adapter);
        Query query = mDatabase.child("vistorias");

        if (vistoriasEventListener != null) {
            query.removeEventListener(vistoriasEventListener);
        }
        vistoriasEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("fetchVistorias", "onChildAdded called");
                for (DataSnapshot vistoriaSnapshot : dataSnapshot.getChildren()) {
                    ItensVistorias vistoria = vistoriaSnapshot.getValue(ItensVistorias.class);

                    if (!vistoria.isConcluida() && !vistoria.isExcluidaVistoria()) {
                        String date = vistoria.getData();
                        String time = String.valueOf(vistoria.getHour());
                        Pair<String, String> dateTimePair = new Pair<>(date, time);

                        if (groupedVistorias.containsKey(dateTimePair)) {
                            ItensVistorias existingVistoria = groupedVistorias.get(dateTimePair);

                            // Verificar se a vistoria já está na lista de vistorias em andamento
                            boolean isInList = false;
                            for (ItensVistorias itemVistoria : existingVistoria.getItens()) {
                                if (itemVistoria.getIdAnuncio().equals(vistoria.getIdAnuncio())) {
                                    isInList = true;
                                    break;
                                }
                            }

                            if (!isInList) {
                                existingVistoria.getItens().add(vistoria);
                            }
                        } else {
                            ItensVistorias newVistoria = new ItensVistorias();
                            newVistoria.setData(date);
                            newVistoria.setNomePerfilU(vistoria.getNomePerfilU());
                            newVistoria.setNomeItem(vistoria.getNomeItem());
                            newVistoria.setLocalizacao(vistoria.getLocalizacao());
                            newVistoria.setPlaca(vistoria.getPlaca());
                            newVistoria.setOutrasInformacoes(vistoria.getOutrasInformacoes());
                            newVistoria.getItens().add(vistoria);
                            newVistoria.setHour(Integer.parseInt(time));
                            groupedVistorias.put(dateTimePair, newVistoria);
                        }
                    }
                }

                // Atualize a lista de vistorias em andamento e notifique o adaptador
                vistoriasEmAndamento.clear();
                vistoriasEmAndamento.addAll(groupedVistorias.values());
                adapter.notifyDataSetChanged();
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
        };query.addChildEventListener(vistoriasEventListener);
    }



    public void concluirVistoria(ItensVistorias vistoriaAtual, int position) {
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
                    List<ItensVistorias> vistoriasList = new ArrayList<>();
                    for (DataSnapshot inspectorSnapshot : dataSnapshot.getChildren()) {
                        String inspectorId = inspectorSnapshot.getKey();
                        DatabaseReference inspectorRef = ConFirebase.getFirebaseDatabase().child("vistorias").child(inspectorId);
                        Query query = inspectorRef.orderByChild("localizacao_data").equalTo(uniqueGroupKey);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot vistoriaSnapshot : dataSnapshot.getChildren()) {
                                        ItensVistorias vistoria = vistoriaSnapshot.getValue(ItensVistorias.class);
                                        if (vistoria != null && !vistoria.isConcluida()) {
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
                            ItensVistorias vistoriaConcluidaExistente = null;
                            String vistoriaConcluidaId = null;
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot vistoriaConcluidaSnapshot : dataSnapshot.getChildren()) {
                                    vistoriaConcluidaExistente = vistoriaConcluidaSnapshot.getValue(ItensVistorias.class);
                                    vistoriaConcluidaId = vistoriaConcluidaSnapshot.getKey();
                                    break;
                                }
                            }

                            ItensVistorias vistoriaConcluida;
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

                            for (ItensVistorias vistoria : vistoriasList) {
                                String inspectorId = vistoria.getIdInspector();
                                String vistoriaId = vistoria.getIdAnuncio();
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


                private ItensVistorias combineVistorias(List<ItensVistorias> vistoriasList, ItensVistorias vistoriaConcluidaExistente) {
        if (vistoriasList.isEmpty()) {
            if (vistoriaConcluidaExistente != null) {
                return vistoriaConcluidaExistente;
            } else {
                throw new IllegalArgumentException("A lista de vistorias não pode estar vazia");
            }
        }

        ItensVistorias vistoriaConcluida;
        if (vistoriaConcluidaExistente != null) {
            vistoriaConcluida = vistoriaConcluidaExistente;
        } else {
            vistoriaConcluida = new ItensVistorias();
            ItensVistorias firstVistoria = vistoriasList.get(0);

            // Copiar campos que não serão modificados
            vistoriaConcluida.setIdAnuncio(firstVistoria.getIdAnuncio());
            vistoriaConcluida.setLocalizacao(firstVistoria.getLocalizacao());
            vistoriaConcluida.setLocalizacao_data(firstVistoria.getLocalizacao_data());
            vistoriaConcluida.setPlaca(firstVistoria.getPlaca());
            vistoriaConcluida.setData(firstVistoria.getData());
            vistoriaConcluida.setLatitude(firstVistoria.getLatitude());
            vistoriaConcluida.setLongetude(firstVistoria.getLongetude());
            vistoriaConcluida.setTipoItem(firstVistoria.getTipoItem());

            // Inicializar campos que serão combinados
            vistoriaConcluida.setNomePerfilU(firstVistoria.getNomePerfilU());
            vistoriaConcluida.setIdInspector(firstVistoria.getIdInspector());
            vistoriaConcluida.setFotos(new ArrayList<>(firstVistoria.getFotos()));
            vistoriaConcluida.setOutrasInformacoes(firstVistoria.getOutrasInformacoes());
        }

        for (int i = (vistoriaConcluidaExistente != null) ? 0 : 1; i < vistoriasList.size(); i++) {
            ItensVistorias vistoria = vistoriasList.get(i);

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
            vistoriaConcluida.setOutrasInformacoes(vistoriaConcluida.getOutrasInformacoes() + "\n\n" + vistoria.getOutrasInformacoes());
        }

        return vistoriaConcluida;
    }
}