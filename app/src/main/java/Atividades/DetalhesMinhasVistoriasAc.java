package Atividades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Adaptadores.AdapterItensVistoria;
import Ajuda.ConFirebase;
import Modelos.Item;
import Modelos.Vistoria;
import br.com.patrimoniomv.R;
import Modelos.RecyclerItemClickListener;

public class DetalhesMinhasVistoriasAc extends AppCompatActivity {

    private Vistoria vistoria;
    private RecyclerView recyclerViewItens;
    private AdapterItensVistoria adapterItensVistoria;
    private List<Item> listaItens;
    private DatabaseReference vistoriasRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_minhas_vistorias);
        vistoria = (Vistoria) getIntent().getSerializableExtra("vistorias");

        // Configurar Firebase
        vistoriasRef = FirebaseDatabase.getInstance().getReference("vistorias").child(vistoria.getLocalizacao());

        // Configurar RecyclerView
        recyclerViewItens = findViewById(R.id.recyclerViewItens);
        listaItens = new ArrayList<>(); // Inicialize listaItens como um novo ArrayList vazio
        adapterItensVistoria = new AdapterItensVistoria(listaItens, this);
        recyclerViewItens.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItens.setHasFixedSize(true);
        recyclerViewItens.setAdapter(adapterItensVistoria);
        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adicionarItem(v);
            }
        });

        // Adicionar ValueEventListener para ler os itens do Firebase
        vistoriasRef.child("itens").addValueEventListener(new ValueEventListener() {
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
                // Tratar erros aqui
            }
        });

        // Adicionar OnClickListener ao RecyclerView
        recyclerViewItens.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerViewItens,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                // Clique simples - Editar item
                                editarItem(position);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                // Clique longo - Excluir item
                                excluirItem(position);
                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            }
                        }
                )
        );
    }

    private void editarItem(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_item, null);

        EditText editTextNomeItem = dialogView.findViewById(R.id.editTextNomeItem);
        EditText editTextObservacao = dialogView.findViewById(R.id.editTextObservacao);

        Item currentItem = listaItens.get(position);
        editTextNomeItem.setText(currentItem.getNome());
        editTextObservacao.setText(currentItem.getObservacao());

        editTextNomeItem.setEnabled(true);

        builder.setView(dialogView)
                .setPositiveButton("Salvar", (dialog, id) -> {
                    String nomeItem = editTextNomeItem.getText().toString();
                    String observacao = editTextObservacao.getText().toString();

                    Item updatedItem = new Item();
                    updatedItem.setId(currentItem.getId());
                    updatedItem.setNome(nomeItem);
                    updatedItem.setObservacao(observacao);
                    updatedItem.setFotos(currentItem.getFotos());
                    updatedItem.setPlaca(currentItem.getPlaca());
                    updatedItem.setLocalizacao(currentItem.getLocalizacao());
                    updatedItem.setLatitude(currentItem.getLatitude());
                    updatedItem.setLongitude(currentItem.getLongitude());

                    atualizarItemNoBanco(position, updatedItem);
                })
                .setNegativeButton("Cancelar", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    private void atualizarItemNoBanco(int position, Item newItem) {
        Item oldItem = listaItens.get(position);

        DatabaseReference itemRefVistorias = ConFirebase.getFirebaseDatabase()
                .child("vistorias")
                .child(oldItem.getLocalizacao())
                .child("itens")
                .child(oldItem.getId());

        DatabaseReference itemRefVistoriaPu = ConFirebase.getFirebaseDatabase()
                .child("vistoriaPu")
                .child(oldItem.getLocalizacao())
                .child("itens")
                .child(oldItem.getId());

        Map<String, Object> itemUpdates = new HashMap<>();
        itemUpdates.put("nome", newItem.getNome());
        itemUpdates.put("observacao", newItem.getObservacao());
        itemUpdates.put("fotos", newItem.getFotos());
        itemUpdates.put("placa", newItem.getPlaca());
        itemUpdates.put("localizacao", newItem.getLocalizacao());
        itemUpdates.put("latitude", newItem.getLatitude());
        itemUpdates.put("longitude", newItem.getLongitude());

        Task<Void> taskVistorias = itemRefVistorias.updateChildren(itemUpdates);
        Task<Void> taskVistoriaPu = itemRefVistoriaPu.updateChildren(itemUpdates);

        Task<Void> combinedTask = Tasks.whenAll(taskVistorias, taskVistoriaPu);

        combinedTask.addOnSuccessListener(aVoid -> {
            Toast.makeText(getApplicationContext(), "Item atualizado com sucesso!", Toast.LENGTH_SHORT).show();
            listaItens.remove(position);
            listaItens.add(position, newItem);
            adapterItensVistoria.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(getApplicationContext(), "Erro ao atualizar o item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void excluirItem(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Excluir Item")
                .setMessage("Tem certeza que deseja excluir este item?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Item item = listaItens.get(position);
                        listaItens.remove(position);

                        // Atualize o Firebase aqui
                        vistoriasRef.child("itens").child(item.getId()).removeValue();

                        adapterItensVistoria.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    public void adicionarItem(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_item, null);

        EditText editTextNomeItem = dialogView.findViewById(R.id.editTextNomeItem);
        EditText editTextObservacao = dialogView.findViewById(R.id.editTextObservacao);

        builder.setView(dialogView)
                .setPositiveButton("Adicionar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String nomeItem = editTextNomeItem.getText().toString();
                        String observacao = editTextObservacao.getText().toString();

                        Item newItem = new Item();
                        newItem.setNome(nomeItem);
                        newItem.setObservacao(observacao);

                        // Gere um novo ID para o item
                        String newItemId = vistoriasRef.child("itens").push().getKey();
                        newItem.setId(newItemId);

                        listaItens.add(newItem);

                        // Atualize o Firebase aqui
                        vistoriasRef.child("itens").child(newItemId).setValue(newItem);

                        adapterItensVistoria.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

}

