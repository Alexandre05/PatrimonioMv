package Atividades;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Adaptadores.AdapterItensVistoriaParaEditar;
import Modelos.Item;
import Modelos.Vistoria;
import br.com.patrimoniomv.R;

public class DetalhesVistoriaActivity extends AppCompatActivity {
    private RecyclerView recyclerViewItensVistoria;
    private AdapterItensVistoriaParaEditar adapterItensVistoria;
    private List<Item> listaItens;
    private FirebaseUser currentUser;

    private Vistoria vistoria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_vistoria);

        // Obter a vistoria e inicializar o RecyclerView e o Adapter
        vistoria = (Vistoria) getIntent().getSerializableExtra("vistoria");
        recyclerViewItensVistoria = findViewById(R.id.recyclerViewItensVistoria);
        listaItens = new ArrayList<>();
        adapterItensVistoria = new AdapterItensVistoriaParaEditar(listaItens, this);
        recyclerViewItensVistoria.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItensVistoria.setAdapter(adapterItensVistoria);
        // Inicialize o usuário atual
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Exiba os detalhes da vistoria e forneça opções de edição/exclusão
        exibirDetalhes(vistoria);

        // Busque e exiba os itens da vistoria
        buscarItensVistoria(vistoria.getIdVistoria());
    }

    private void exibirDetalhes(Vistoria vistoria) {
        // Implemente a lógica para exibir os detalhes da vistoria nesta activity
        // Pode ser TextViews, EditTexts, Buttons, etc.
        // Você também pode adicionar botões para editar ou excluir itens aqui
        // Exemplo:
        TextView localizacaoTextView = findViewById(R.id.localizacaoTextView);
        TextView dataTextView = findViewById(R.id.dataTextView);
        localizacaoTextView.setText("Localização: " + vistoria.getLocalizacao());
        //dataTextView.setText("Data: " + vistoria.getData());
    }

    private void buscarItensVistoria(String idVistoria) {
        DatabaseReference vistoriasRef = FirebaseDatabase.getInstance().getReference("vistorias").child(idVistoria).child("itens");

        vistoriasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listaItens.clear();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    Item item = itemSnapshot.getValue(Item.class);
                    listaItens.add(item);
                }
                adapterItensVistoria.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}