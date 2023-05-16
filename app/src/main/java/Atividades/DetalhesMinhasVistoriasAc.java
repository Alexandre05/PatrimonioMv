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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
        if (vistoria != null) {
            Log.d("DetalhesAc", "Vistoria recebida: " + vistoria.getIdVistoria());
            // use os campos de vistoria aqui
            vistoriasRef = FirebaseDatabase.getInstance().getReference("vistorias").child(vistoria.getIdVistoria());
            // etc.
        } else {
            Log.d("DetalhesAc", "Nenhuma vistoria recebida na intenção.");
            // trate o caso em que vistoria é null
        }

        // Configurar Firebase
        //vistoriasRef = FirebaseDatabase.getInstance().getReference("vistorias").child(vistoria.getLocalizacao());

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
                Log.d("FirebaseData", "Número de itens: " + dataSnapshot.getChildrenCount());
                listaItens.clear();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    Item item = itemSnapshot.getValue(Item.class);
                    listaItens.add(item);
                    Log.d("FirebaseData", "Item adicionado: " + item.getNome());
                }
                adapterItensVistoria.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Tratar erros aqui
                Log.d("FirebaseData", "Erro ao ler os dados: " + databaseError.getMessage());
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
        String itemId = oldItem.getId(); // ID do item antigo
        String vistoriaId = vistoria.getIdVistoria(); // ID da vistoria

        DatabaseReference itemRefVistorias = ConFirebase.getFirebaseDatabase()
                .child("vistorias")
                .child(vistoriaId)
                .child("itens")
                .child(itemId);

        DatabaseReference itemRefVistoriaPu = ConFirebase.getFirebaseDatabase()
                .child("vistoriaPu")
                .child(vistoriaId)
                .child("itens")
                .child(itemId);

        Map<String, Object> itemUpdates = new HashMap<>();
        itemUpdates.put("/nome", newItem.getNome());
        itemUpdates.put("/observacao", newItem.getObservacao());
        itemUpdates.put("/fotos", newItem.getFotos());
        itemUpdates.put("/placa", newItem.getPlaca());
        itemUpdates.put("/localizacao", newItem.getLocalizacao());
        itemUpdates.put("/latitude", newItem.getLatitude());
        itemUpdates.put("/longitude", newItem.getLongitude());

        Task<Void> taskVistorias = itemRefVistorias.updateChildren(itemUpdates);
        Task<Void> taskVistoriaPu = itemRefVistoriaPu.updateChildren(itemUpdates);

        Task<Void> combinedTask = Tasks.whenAll(taskVistorias, taskVistoriaPu);

        combinedTask.addOnSuccessListener(aVoid -> {
            Toast.makeText(getApplicationContext(), "Item atualizado com sucesso!", Toast.LENGTH_SHORT).show();
            listaItens.set(position, newItem); // Atualiza o item na lista
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

                        DatabaseReference vistoriaPuRef = FirebaseDatabase.getInstance().getReference("vistoriaPu").child(vistoria.getIdVistoria());

                        // Se não houver mais itens na vistoria, exclua a vistoria inteira
                        if (listaItens.size() == 0) {
                            vistoriasRef.removeValue();
                            vistoriaPuRef.removeValue();
                        } else {
                            // Caso contrário, exclua apenas o item
                            vistoriasRef.child("itens").child(item.getId()).removeValue();
                            vistoriaPuRef.child("itens").child(item.getId()).removeValue();
                        }

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
        String dataVistoriaStr = vistoria.getData();
        String dataAtualStr = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try {
            Date dataVistoria = sdf.parse(dataVistoriaStr);
            Date dataAtual = sdf.parse(dataAtualStr);

            Calendar calVistoria = Calendar.getInstance();
            Calendar calAtual = Calendar.getInstance();

            calVistoria.setTime(dataVistoria);
            calAtual.setTime(dataAtual);

            // Verifique se a data da vistoria é igual à data atual
            if (calVistoria.get(Calendar.YEAR) != calAtual.get(Calendar.YEAR) ||
                    calVistoria.get(Calendar.MONTH) != calAtual.get(Calendar.MONTH) ||
                    calVistoria.get(Calendar.DAY_OF_MONTH) != calAtual.get(Calendar.DAY_OF_MONTH)) {
                Toast.makeText(this, "Não é possível adicionar itens a vistorias de dias anteriores.", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao comparar as datas. Tente novamente.", Toast.LENGTH_LONG).show();
            return;
        }


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

