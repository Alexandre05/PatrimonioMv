package Atividades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Adapter.VistoriaAndamentoAdapter;
import Helper.ConFirebase;
import Mode.ItensVistorias;
import br.com.patrimoniomv.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class VistoriasEmAndamentoActivity extends AppCompatActivity {

    private ListView vistoriasAndamentoListView;
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
        Set<String> uniqueKeys = new HashSet<>();

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
                        String licensePlate = vistoria.getPlaca();
                        String date = vistoria.getData();
                        String location = vistoria.getLocalizacao();
                        String uniqueKey = licensePlate + "_" + date + "_" + location;

                        if (!uniqueKeys.contains(uniqueKey)) {
                            uniqueKeys.add(uniqueKey);
                            vistoriasEmAndamento.add(vistoria);
                        }
                    }
                }

                adapter = new VistoriaAndamentoAdapter(VistoriasEmAndamentoActivity.this, R.layout.itensvistoria, vistoriasEmAndamento);
                vistoriasAndamentoListView.setAdapter(adapter);
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
        };
        query.addChildEventListener(vistoriasEventListener);
    }


    public void concluirVistoria(ItensVistorias vistoriaAtual) {
        String localizacao = vistoriaAtual.getLocalizacao();
        String dataVistoria = vistoriaAtual.getData();
        String uniqueGroupKey = localizacao + "_" + dataVistoria;
        Log.d("DEBUG", "uniqueGroupKey: " + uniqueGroupKey);

        AlertDialog.Builder builder = new AlertDialog.Builder(VistoriasEmAndamentoActivity.this);
        builder.setTitle("Concluir Vistoria");
        builder.setMessage("Deseja concluir todas as vistorias da localização: " + localizacao + " na data: " + dataVistoria + "?");
        builder.setPositiveButton("Sim", (dialog, which) -> {
            DatabaseReference anunciosRef = ConFirebase.getFirebaseDatabase().child("vistorias");

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
                                            Log.d("DEBUG", "Vistoria item: " + vistoria); // Adicionado para rastrear itens da vistoria
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

                    // Combinar os itens das várias vistorias em uma única vistoria concluída
                    ItensVistorias vistoriaConcluida = combineVistorias(vistoriasList);
                    vistoriaConcluida.setLocalizacao(localizacao);
                    vistoriaConcluida.setData(dataVistoria);
                    vistoriaConcluida.setConcluida(true);

                    // Salvar a vistoria concluída no nó "vistoriasConcluidas"
                    DatabaseReference vistoriasConcluidasRef = ConFirebase.getFirebaseDatabase().child("vistoriasConcluidas");
                    String vistoriaConcluidaId = vistoriasConcluidasRef.push().getKey();
                    if (vistoriaConcluidaId != null) {
                        vistoriasConcluidasRef.child(vistoriaConcluidaId).setValue(vistoriaConcluida.toMap());
                    }                // Atualizar o status das vistorias originais para "concluida = true"
                    for (ItensVistorias vistoria : vistoriasList) {
                        String inspectorId = vistoria.getIdInspector();
                        String vistoriaId = vistoria.getIdAnuncio();
                        if (inspectorId != null && vistoriaId != null) {
                            anunciosRef.child(inspectorId).child(vistoriaId).child("concluida").setValue(true);
                        }
                    }

                    // Atualizar a lista de vistorias em andamento
                    fetchVistoriasEmAndamento();
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

    private ItensVistorias combineVistorias(List<ItensVistorias> vistoriasList) {
        ItensVistorias vistoriaConcluida = new ItensVistorias();
        // Inicializar campos adicionais conforme necessário

        for (ItensVistorias vistoria : vistoriasList) {
            // Combine os itens das várias vistorias aqui
            // Exemplo: vistoriaConcluida.setCampoX(vistoriaConcluida.getCampoX() + vistoria.getCampoX());
        }

        return vistoriaConcluida;
    }
}
