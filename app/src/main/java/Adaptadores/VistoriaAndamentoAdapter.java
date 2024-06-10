// Pacote que contém o adaptador para o RecyclerView
package Adaptadores;

// Importações necessárias para funcionalidades específicas do Android
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

// Importações do AndroidX para suporte a RecyclerView
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Importações de classes específicas do aplicativo
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Atividades.VistoriasEmAndamentoActivity; // Atividade relacionada
import Modelos.Item; // Modelo de dados para itens de vistoria
import Modelos.Vistoria; // Modelo de dados para vistorias

// Importação do recurso de layout R
import br.com.patrimoniomv.R;

// Classe responsável por adaptar dados de vistorias a um RecyclerView
public class VistoriaAndamentoAdapter extends RecyclerView.Adapter<VistoriaAndamentoAdapter.ViewHolder> {

    // Contexto da aplicação
    private Context context;

    // Lista de vistorias a serem exibidas
    private List<Vistoria> vistorias;

    // Flag para controlar a exibição do botão "Concluir Vistoria"
    private boolean showConcluirVistoriaButton;

    // Construtor que inicializa o adaptador com dados iniciais
    public VistoriaAndamentoAdapter(Context context, List<Vistoria> vistorias, boolean showConcluirVistoriaButton) {
        this.context = context;
        // Garante que a lista de vistorias nunca seja nula
        this.vistorias = vistorias != null ? vistorias : new ArrayList<>();
        this.showConcluirVistoriaButton = showConcluirVistoriaButton;
    }

    // Método para criar novas instâncias de ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout do item da vistoria
        View view = LayoutInflater.from(context).inflate(R.layout.itensvistoria, parent, false);
        return new ViewHolder(view);
    }

    // Método chamado para exibir dados na posição especificada

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vistoria vistoriaAtual = vistorias.get(position);

        holder.nomePerfil.setText(vistoriaAtual.getNomePerfilU());
        holder.dataTextView.setText(vistoriaAtual.getData());
        holder.localizacaoTextView.setText(vistoriaAtual.getLocalizacao());

        // Configurar RecyclerView de itens
        List<Item> itemList = new ArrayList<>(vistoriaAtual.getItensMap().values());
        // erro Cannot resolve symbol 'ItemAdapter'
        ItemAdapter itemAdapter = new ItemAdapter(itemList, context);
        holder.recyclerViewItens.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerViewItens.setAdapter(itemAdapter);

        if (showConcluirVistoriaButton) {
            holder.concluirVistoriaButton.setVisibility(View.VISIBLE);
            holder.concluirVistoriaButton.setOnClickListener(view -> {
                ((VistoriasEmAndamentoActivity) context).concluirVistoria(position);
            });
        } else {
            holder.concluirVistoriaButton.setVisibility(View.GONE);
        }
    }


    // Retorna o número total de itens na lista
    @Override
    public int getItemCount() {
        return vistorias.size();
    }

    // Limpa a lista de vistorias
    public void clear() {
        int size = vistorias.size();
        vistorias.clear();
        notifyItemRangeRemoved(0, size);
    }

    // Adiciona uma lista de vistorias à lista existente
    public void addAll(List<Vistoria> newVistorias) {
        int startIndex = vistorias.size();
        vistorias.addAll(newVistorias);
        notifyItemRangeInserted(startIndex, newVistorias.size());
    }

    // Classe interna que representa a estrutura de um item no RecyclerView
    static class ViewHolder extends RecyclerView.ViewHolder {
        Button concluirVistoriaButton;
        TextView nomePerfil, dataTextView, localizacaoTextView;
        RecyclerView recyclerViewItens;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            concluirVistoriaButton = itemView.findViewById(R.id.concluirVistoriaButton);
            nomePerfil = itemView.findViewById(R.id.nomePerfilUTextView);
            dataTextView = itemView.findViewById(R.id.dataTextView);
            localizacaoTextView = itemView.findViewById(R.id.localizacaoTextView);
            recyclerViewItens = itemView.findViewById(R.id.recyclerViewItens);
        }
    }

}
