package Atividades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.List;

import Adapter.VistoriaAndamentoAdapter;
import Helper.ConFirebase;
import Mode.ItensVistorias;
import br.com.patrimoniomv.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class VistoriasEmAndamentoActivity extends AppCompatActivity {

    private ListView vistoriasAndamentoListView;
    private DatabaseReference mDatabase;
    private Button concluir;
    private VistoriaAndamentoAdapter adapter;
    private List<ItensVistorias> vistoriasEmAndamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vistorias_em_andamento);

        vistoriasAndamentoListView = findViewById(R.id.vistorias_andamentoU);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //mDatabase.child("vistoriasConcluidas").setValue(true);

        // Carregar os dados das vistorias em andamento
        fetchVistoriasEmAndamento();

    }

    private void fetchVistoriasEmAndamento() {
        vistoriasEmAndamento = new ArrayList<>();
        Query query = mDatabase.child("anuncios");
        ChildEventListener vistoriasEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("fetchVistorias", "onChildAdded called");
                for (DataSnapshot vistoriaSnapshot : dataSnapshot.getChildren()) {
                    ItensVistorias vistoria = vistoriaSnapshot.getValue(ItensVistorias.class);

                    if (!vistoria.isConcluida() && !vistoria.isExcluidaVistoria()) {
                        vistoriasEmAndamento.add(vistoria);
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
        Log.d("concluirVistoria", "concluirVistoria called");
        String userId = ConFirebase.getIdUsuario();
        String vistoriaId = vistoriaAtual.getIdAnuncio();
        String localizacao = vistoriaAtual.getLocalizacao();
        String dataVistoria = vistoriaAtual.getData();

        // Verificar se outra vistoria na mesma sala e com a mesma data já foi concluída
        mDatabase.child("vistoriasConcluidas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                vistoriaAtual.setConcluida(true);
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot vistoriaSnapshot : userSnapshot.getChildren()) {
                        ItensVistorias vistoriaConcluida = vistoriaSnapshot.getValue(ItensVistorias.class);

                        if (localizacao.equals(vistoriaConcluida.getLocalizacao()) && dataVistoria.equals(vistoriaConcluida.getData())) {
                            // Outra vistoria na mesma sala e com a mesma data já foi concluída
                            Toast.makeText(VistoriasEmAndamentoActivity.this, "A vistoria já foi concluída por outro vistoriador.", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                }

                // Se chegar aqui, a vistoria pode ser concluída
                // Atualizar o status da vistoria na instância atual
                vistoriaAtual.setConcluida(true);

                // Adicionar a vistoria concluída ao nó "vistoriasConcluidas"
                DatabaseReference vistoriaConcluidaRef = mDatabase.child("vistoriasConcluidas").child(userId).child(vistoriaId);
                vistoriaConcluidaRef.setValue(vistoriaAtual, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            // Atualizar o status da vistoria nos nós "anuncios" e "anunciosPu"
                            mDatabase.child("anuncios").child(userId).child(vistoriaId).child("concluida").setValue(true);
                            mDatabase.child("anunciosPu").child(localizacao).child(vistoriaId).child("concluida").setValue(true);

                            // Atualizar a lista de vistorias
                            fetchVistoriasEmAndamento();

                        } else {
                            // Tratar o erro aqui
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Tratar o erro aqui
            }
        });
    }






    public void novoMetodo(ItensVistorias vistoriaAtual) {
        // Implemente o comportamento desejado aqui
    }



}