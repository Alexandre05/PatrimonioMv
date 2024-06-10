package Atividades;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import java.util.Set;

import Adaptadores.VistoriaAndamentoAdapter;
import Ajuda.ConFirebase;
import Modelos.Item;
import Modelos.Vistoria;

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
    private ArrayList<? extends Parcelable> listaItens;
    private ChildEventListener vistoriasPuEventListener;

    public VistoriasEmAndamentoActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vistorias_em_andamento);
        uniqueLicensePlates = new HashSet<>();
       // @layout/activity_vistorias_em_andamento does not contain a declaration with id recyclerViewVistorias
        RecyclerView recyclerView = findViewById(R.id.recyclerViewItens);;
        // Obtenha o ID da vistoria passado da activity anterior
        String idVistoria = getIntent().getStringExtra("idVistoria");
        checkUserAuthentication();
        vistoriasEmAndamento = new ArrayList<>();
        adapter = new VistoriaAndamentoAdapter(this, vistoriasEmAndamento, true);
// @layout/activity_vistorias_em_andamento does not contain a declaration with id recyclerViewVistorias

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Vistoria> listaVistorias = new ArrayList<>(); // Preencha com seus dados
        VistoriaAndamentoAdapter adapter = new VistoriaAndamentoAdapter(this, listaVistorias, true);
        recyclerView.setAdapter(adapter);

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

        AlertDialog.Builder builder = new AlertDialog.Builder(VistoriasEmAndamentoActivity.this);
        builder.setTitle("Confirmação");
        builder.setMessage("Deseja realmente concluir esta vistoria?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Define a vistoria como concluída
                vistoria.setConcluida(true);

                // Restante do código para concluir a vistoria

                // Notifica o usuário que a vistoria foi concluída
                Toast.makeText(VistoriasEmAndamentoActivity.this, "Vistoria concluída com sucesso!", Toast.LENGTH_SHORT).show();

                // Remove espaços e caracteres especiais dos valores
                String localizacao = vistoria.getLocalizacao().replaceAll("\\s+", "");
                String idVistoriador = vistoria.getIdVistoria().replaceAll("\\s+", "");
                String vistoriadorId = vistoria.getIdUsuario().replaceAll("\\s+", "");

                // Cria uma consulta para verificar se existem vistorias anteriores com a mesma data, localização e vistoriador
                Query query = mDatabase.child("vistorias")
                        .orderByKey()
                        .equalTo(localizacao + vistoriadorId + idVistoriador);

                // Adiciona um ouvinte de eventos único para a consulta
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Itera sobre as vistorias anteriores encontradas na consulta
                        for (DataSnapshot vistoriaSnapshot : dataSnapshot.getChildren()) {
                            Vistoria vistoriaAntiga = vistoriaSnapshot.getValue(Vistoria.class);
                            if (vistoriaAntiga != null) {
                                // Adiciona os itens da vistoria antiga à vistoria atual
                                vistoria.getItensMap().putAll(vistoriaAntiga.getItensMap());
                                Log.d("Debug", "Itens da Vistoria Antiga: " + vistoriaAntiga.getItensMap().toString());
                                Log.d("Debug", "Itens da Vistoria Atual: " + vistoria.getItensMap().toString());

                                // Remove a vistoria antiga do banco de dados Firebase
                                vistoriaSnapshot.getRef().removeValue();
                            }
                        }

                        // Adiciona a vistoria atual às vistorias concluídas
                        String idVistoria = mDatabase.child("vistoriasConcluidas").push().getKey();
                        mDatabase.child("vistoriasConcluidas").child(idVistoria).setValue(vistoria);

                        // Adiciona a vistoria atual aos nós de vistorias concluídas e patrimônio público
                        mDatabase.child("vistoriaPu").child(idVistoria).setValue(vistoria);

                        // Remove a vistoria em andamento da lista local e notifica o adapter para atualizar a UI.
                        vistoriasEmAndamento.remove(position);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Firebase", "Erro ao verificar vistorias anteriores", databaseError.toException());
                    }
                });
            }
        });

        builder.setNeutralButton("Editar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Aqui você pode adicionar a lógica para abrir a tela de edição
                abrirOpcoesVistoria(vistoria.getIdVistoria(), new ArrayList<>(vistoria.getItensMap().values()));
            }
        });

        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // O usuário optou por não concluir a vistoria, não faz nada
            }
        });

        // Exibe o AlertDialog
        builder.create().show();
    }

    // Exemplo de um método para abrir a tela de edição
    private void abrirTelaDeEdicao(Vistoria vistoria) {
        // Adicione a lógica para abrir a tela de edição aqui
        // Pode ser uma nova Activity, Fragment, etc.
        // Por exemplo:error: incompatible types: ArrayList<Item> cannot be converted to ArrayList<? extends Parcelable>
        //            intent.putParcelableArrayListExtra("listaItens", (ArrayList<? extends Parcelable>) itemList);
        //                                                                                               ^
        Intent intent = new Intent(VistoriasEmAndamentoActivity.this, DetalhesVistoriaActivity.class);
        intent.putExtra("vistorias", vistoria); // Se necessário, envie a vistoria para a tela de edição
        startActivity(intent);
    }

    private void abrirOpcoesVistoria(String idVistoria, List<Item> listaItens) {
        Toast.makeText(this, "ID da Vistoria: " + idVistoria, Toast.LENGTH_SHORT).show();

        // Adiciona logs para verificar se a lista está sendo passada corretamente
        if (listaItens != null) {
            for (Item item : listaItens) {
                Log.d("Item", "Item: " + item.toString());
            }
        } else {
            Log.d("Item", "Lista de itens é nula.");
        }

        Intent intent = new Intent(VistoriasEmAndamentoActivity.this, OpcoesVistoriaActivity.class);
        // Passe os dados da vistoria, se necessário
        intent.putExtra("idVistoria", idVistoria);
        // Se a lista de itens não for nula, passe-a como um extra
        if (listaItens != null) {
            ArrayList<Item> itemList = new ArrayList<>(listaItens);
            intent.putParcelableArrayListExtra("listaItens", itemList);
        }
        startActivity(intent);
    }



}