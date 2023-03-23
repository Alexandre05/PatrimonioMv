package Atividades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

import Adapter.VistoriaAndamentoAdapter;
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
        mDatabase.child("anuncios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                vistoriasEmAndamento = new ArrayList<>();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot vistoriaSnapshot : userSnapshot.getChildren()) {
                        ItensVistorias vistoria = vistoriaSnapshot.getValue(ItensVistorias.class);

                        if (!vistoria.isConcluida() && !vistoria.isExcluidaVistoria()) {
                            vistoriasEmAndamento.add(vistoria);
                        }
                    }
                }

                if (vistoriasEmAndamento.isEmpty()) {
                    // Adicione uma mensagem ou um componente visual para informar que não há vistorias em andamento
                    return;
                }

                // Configurar o adaptador e associá-lo à ListView
                adapter = new VistoriaAndamentoAdapter(VistoriasEmAndamentoActivity.this, R.layout.itensvistoria, vistoriasEmAndamento);
                vistoriasAndamentoListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Trate os erros aqui
            }
        });
    }




    public void concluirVistoria(ItensVistorias vistoriaAtual) {
        Log.d("Activity", "concluirVistoria called");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String vistoriaId = vistoriaAtual.getIdAnuncio();
        String localizacao = vistoriaAtual.getLocalizacao();

        // Atualizar o status da vistoria na instância atual
        vistoriaAtual.setConcluida(true);

        // Adicionar a vistoria concluída ao nó "vistoriasConcluidas"
        DatabaseReference vistoriaConcluidaRef = mDatabase.child("vistoriasConcluidas").child(vistoriaId);
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


    private void carregarVistorias() {
        fetchVistoriasEmAndamento();
    }


}