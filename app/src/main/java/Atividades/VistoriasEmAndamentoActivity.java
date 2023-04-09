package Atividades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vistorias_em_andamento);
        uniqueLicensePlates = new HashSet<>();

        vistoriasAndamentoListView = findViewById(R.id.vistorias_andamentoU);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //mDatabase.child("vistoriasConcluidas").setValue(true);

        // Carregar os dados das vistorias em andamento
        fetchVistoriasEmAndamento();

    }

    private void fetchVistoriasEmAndamento() {
        vistoriasEmAndamento = new ArrayList<>();
        Set<String> uniqueKeys = new HashSet<>(); // Crie um conjunto para armazenar as chaves únicas

        Query query = mDatabase.child("anuncios");
        ChildEventListener vistoriasEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("fetchVistorias", "onChildAdded called");
                for (DataSnapshot vistoriaSnapshot : dataSnapshot.getChildren()) {
                    ItensVistorias vistoria = vistoriaSnapshot.getValue(ItensVistorias.class);

                    if (!vistoria.isConcluida() && !vistoria.isExcluidaVistoria()) {
                        String licensePlate = vistoria.getPlaca(); // Obtenha o número da placa aqui
                        String date = vistoria.getData(); // Obtenha a data da vistoria aqui
                        String location = vistoria.getLocalizacao(); // Obtenha a localização da vistoria aqui
                        String uniqueKey = licensePlate + "_" + date + "_" + location; // Crie uma chave única combinando a placa, data e localização

                        if (!uniqueKeys.contains(uniqueKey)) { // Verifique se a chave única não está no conjunto
                            uniqueKeys.add(uniqueKey); // Adicione a chave única ao conjunto
                            vistoriasEmAndamento.add(vistoria); // Adicione a vistoria à lista
                        }
                    }
                }

                // Configurar o adaptador e associá-lo à ListView
                adapter = new VistoriaAndamentoAdapter(VistoriasEmAndamentoActivity.this, R.layout.itensvistoria, vistoriasEmAndamento);
                vistoriasAndamentoListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                fetchVistoriasEmAndamento(); // Quando uma vistoria for concluída, atualize a lista
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Trate os erros aqui
            }
        };
        query.addChildEventListener(vistoriasEventListener);
    }


    public void concluirVistoria(ItensVistorias vistoriaAtual) {
        String idInspector = vistoriaAtual.getIdInspector();
        String localizacao = vistoriaAtual.getLocalizacao();
        String dataVistoria = vistoriaAtual.getData();
        Set<String> uniqueLicensePlates = new HashSet<>();

        // Solicitar confirmação do vistoriador
        AlertDialog.Builder builder = new AlertDialog.Builder(VistoriasEmAndamentoActivity.this);
        builder.setTitle("Concluir Vistoria");
        builder.setMessage("Deseja concluir todas as vistorias da localização: " + localizacao + " na data: " + dataVistoria + "?");
        builder.setPositiveButton("Sim", (dialog, which) -> {
            // Obter todas as vistorias da mesma localização, na mesma data e com placas únicas
            Query query = mDatabase.child("anuncios").child(idInspector).orderByChild("localizacao").equalTo(localizacao);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot vistoriaSnapshot : dataSnapshot.getChildren()) {
                        ItensVistorias vistoria = vistoriaSnapshot.getValue(ItensVistorias.class);
                        String licensePlate = vistoria.getPlaca();

                        if (vistoria.getData().equals(dataVistoria) && !vistoria.isConcluida() && !uniqueLicensePlates.contains(licensePlate)) {
                            uniqueLicensePlates.add(licensePlate);
                            String vistoriaId = vistoria.getIdAnuncio();

                            // Mover vistoria para o nó "vistoriasConcluidas"
                            DatabaseReference vistoriaConcluidaRef = mDatabase.child("vistoriasConcluidas").child(idInspector).child(vistoriaId);
                            vistoria.setConcluida(true);
                            vistoriaConcluidaRef.setValue(vistoria);

                            // Atualizar o status da vistoria nos nós "anuncios" e "anunciosPu"
                            mDatabase.child("anuncios").child(idInspector).child(vistoriaId).child("concluida").setValue(true);
                            mDatabase.child("anunciosPu").child(localizacao).child(vistoriaId).child("concluida").setValue(true);
                        }
                    }

                    // Atualizar a lista de vistorias em andamento
                    fetchVistoriasEmAndamento();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Tratar o erro aqui
                }
            });
        });

        builder.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }





    public void novoMetodo(ItensVistorias vistoriaAtual) {
        // Implemente o comportamento desejado aqui
    }



}