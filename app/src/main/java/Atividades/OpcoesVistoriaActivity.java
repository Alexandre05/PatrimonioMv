package Atividades;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import Modelos.Item;
import Modelos.Vistoria;
import br.com.patrimoniomv.R;

public class OpcoesVistoriaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opcoes_vistoria);
        Button btnAtualizarItens = findViewById(R.id.btnAtualizarItens);
        Button btnAdicionarNovoItem = findViewById(R.id.btnAdicionarNovoItem);
        Button btnExcluirItem = findViewById(R.id.btnExcluirItem);

        Intent intent = getIntent();
// Receber a lista de itens da Intent
        ArrayList<Item> listaItens = getIntent().getParcelableArrayListExtra("listaItens");
        if (intent != null) {
            String vistoriaId = intent.getStringExtra("idVistoria");

            if (listaItens != null) {
                for (Item item : listaItens) {
                    Log.d("OpcoesVistoriaActivity", "Item: " + item.toString());
                    // Faça o que precisar com cada item
                }
            }
        btnAtualizarItens.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Adicione a lógica para abrir a tela de atualização de itens
                // por exemplo, você pode abrir a tela de edição existente

            }
        });
            mostrarListaItens(vistoriaId);
        btnAdicionarNovoItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Adicione a lógica para adicionar um novo item à vistoria
                // por exemplo, você pode abrir uma tela para adicionar um novo item
                // ou exibir um diálogo para inserir os detalhes do novo item
                // e adicioná-lo à lista de itens da vistoria

                //String idVistoria = "teste";
                Intent intent = new Intent(OpcoesVistoriaActivity.this, EditVistoriaActivity.class);
                // Adicione dados extras ao Intent, como o ID da vistoria
                intent.putExtra("idVistoria", vistoriaId );
                startActivity(intent);
            }
        });
        btnExcluirItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Adicione a lógica para excluir um item da vistoria
                // por exemplo, você pode exibir uma lista de itens e permitir que o usuário selecione um para excluir
                // ou abrir uma tela de edição onde o usuário pode selecionar e excluir itens
            }
        });

    }

}
    private void mostrarListaItens(String idVistoria) {
        DatabaseReference vistoriaRef = Vistoria.getVistoriaReference(idVistoria);

        vistoriaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Vistoria vistoria = dataSnapshot.getValue(Vistoria.class);
                    if (vistoria != null) {
                        Map<String, Item> itensMap = vistoria.getItensMap();

                        if (itensMap != null && !itensMap.isEmpty()) {
                            for (Map.Entry<String, Item> entry : itensMap.entrySet()) {
                                Item item = entry.getValue();
                                if (item != null) {
                                    Log.d("INFO", "ID do Item: " + item.getId());
                                    Log.d("INFO", "Nome do Item: " + item.getNome());
                                    Log.d("INFO", "Observação do Item: " + item.getObservacao());
                                    // Adicione outros campos conforme necessário

                                    // Adicione aqui a lógica para exibir na tela, como por exemplo, usando Toast
                                    // Toast.makeText(OpcoesActivity.this, "Nome do Item: " + item.getNome(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.d("INFO", "Item é nulo");
                                }
                            }
                        } else {
                            Log.d("INFO", "A lista de itens está vazia");
                        }
                    } else {
                        Log.d("INFO", "Vistoria é nula");
                    }
                } else {
                    Log.d("INFO", "Vistoria não encontrada");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ERRO", "Erro ao buscar vistoria: " + databaseError.getMessage());
            }
        });
    }


}