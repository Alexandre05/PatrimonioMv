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
        // Obtém a vistoria atual
        Vistoria vistoriaAtual = vistorias.get(position);

        // Define dados nos elementos visuais do ViewHolder
        holder.nomePerfil.setText(vistoriaAtual.getNomePerfilU());
        holder.dataTextView.setText(vistoriaAtual.getData());
        holder.localizacaoTextView.setText(vistoriaAtual.getLocalizacao());

        // Remove todos os itens anteriores do itemListContainer
        holder.itemListContainer.removeAllViews();

        // Verifica se há itens na vistoria atual
        if (vistoriaAtual.getItensMap() != null) {
            // Itera sobre os itens da vistoria e os exibe
            for (Map.Entry<String, Item> entry : vistoriaAtual.getItensMap().entrySet()) {
                Item item = entry.getValue();

                // Infla o layout para o item
                View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_item, holder.itemListContainer, false);

                // Define dados nos elementos visuais do item
                TextView itemNameTextView = itemView.findViewById(R.id.item_name);
                itemNameTextView.setText(item.getNome());

                TextView itemPlacaTextView = itemView.findViewById(R.id.item_placa);
                itemPlacaTextView.setText(item.getPlaca());

                TextView itemOutrasInformacoesTextView = itemView.findViewById(R.id.item_outras_informacoes);
                itemOutrasInformacoesTextView.setText(item.getObservacao());

                // Adiciona o item ao itemListContainer
                holder.itemListContainer.addView(itemView);
            }
        }

        // Verifica se o botão "Concluir Vistoria" deve ser exibido
        if (showConcluirVistoriaButton) {
            // Torna o botão visível e define um OnClickListener
            holder.concluirVistoriaButton.setVisibility(View.VISIBLE);
            holder.concluirVistoriaButton.setOnClickListener(view -> {
                // Chama o método na atividade relacionada quando o botão é clicado
                ((VistoriasEmAndamentoActivity) context).concluirVistoria(position);
            });
        } else {
            // Torna o botão invisível se não for necessário
            holder.concluirVistoriaButton.setVisibility(View.GONE);
        }

        // Adiciona o OnClickListener ao botão Concluir Vistoria
        holder.concluirVistoriaButton.setOnClickListener(view -> {
            ((VistoriasEmAndamentoActivity) context).concluirVistoria(position);
        });
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
        LinearLayout itemListContainer;

        // Construtor que inicializa os elementos visuais
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            concluirVistoriaButton = itemView.findViewById(R.id.concluirVistoriaButton);
            nomePerfil = itemView.findViewById(R.id.nomePerfilUTextView);
            dataTextView = itemView.findViewById(R.id.dataTextView);
            localizacaoTextView = itemView.findViewById(R.id.localizacaoTextView);
            itemListContainer = itemView.findViewById(R.id.item_list_container);
        }
    }
}
