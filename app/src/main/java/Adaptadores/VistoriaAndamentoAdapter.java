package Adaptadores;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Atividades.VistoriasEmAndamentoActivity;
import Modelos.Item;
import Modelos.Vistoria;
import br.com.patrimoniomv.R;

public class VistoriaAndamentoAdapter extends RecyclerView.Adapter<VistoriaAndamentoAdapter.ViewHolder> {
    private Context context;
    private List<Vistoria> vistorias;
    private boolean showConcluirVistoriaButton;
    public VistoriaAndamentoAdapter(Context context, List<Vistoria> vistorias) {
        this.context = context;
        this.vistorias = vistorias != null ? vistorias : new ArrayList<>();
        this.showConcluirVistoriaButton = showConcluirVistoriaButton;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.itensvistoria, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vistoria vistoriaAtual = vistorias.get(position);
        Log.d("VistoriaAndamentoAdapter", "getView: Position=" + position + ", Vistoria=" + vistoriaAtual.toString());
        holder.nomePerfil.setText(vistoriaAtual.getNomePerfilU());
        holder.dataTextView.setText(vistoriaAtual.getData());
        holder.localizacaoTextView.setText(vistoriaAtual.getLocalizacao());

        holder.itemListContainer.removeAllViews();
        if (vistoriaAtual.getItensMap() != null) {
            for (Map.Entry<String, Item> entry : vistoriaAtual.getItensMap().entrySet()) {
                Item item = entry.getValue();
                Log.d("VistoriaAndamentoAdapter", "Item atual: " + item.toString());
                // Inflate the layout for the item
                View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_item, holder.itemListContainer, false);

                TextView itemNameTextView = itemView.findViewById(R.id.item_name);
                itemNameTextView.setText(item.getNome());

                TextView itemPlacaTextView = itemView.findViewById(R.id.item_placa);
                itemPlacaTextView.setText(item.getPlaca());

                TextView itemOutrasInformacoesTextView = itemView.findViewById(R.id.item_outras_informacoes);
                itemOutrasInformacoesTextView.setText(item.getObservacao());

                // Add the itemView to the itemListContainer
                holder.itemListContainer.addView(itemView);
            }
        }
        if (showConcluirVistoriaButton) {
            holder.concluirVistoriaButton.setVisibility(View.VISIBLE);
            holder.concluirVistoriaButton.setOnClickListener(view -> {
                ((VistoriasEmAndamentoActivity) context).concluirVistoria(position);
            });
        } else {
            holder.concluirVistoriaButton.setVisibility(View.GONE);
        }

        // Adicione o OnClickListener ao botão Concluir Vistoria
        holder.concluirVistoriaButton.setOnClickListener(view -> {
            ((VistoriasEmAndamentoActivity) context).concluirVistoria(position);
        });
    }

    @Override
    public int getItemCount() {
        return vistorias.size();
    }
    public void clear() {
        int size = vistorias.size();
        vistorias.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addAll(List<Vistoria> newVistorias) {
        int startIndex = vistorias.size();
        vistorias.addAll(newVistorias);
        notifyItemRangeInserted(startIndex, newVistorias.size());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        Button concluirVistoriaButton;
        TextView nomePerfil, dataTextView, localizacaoTextView;
        LinearLayout itemListContainer;

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

