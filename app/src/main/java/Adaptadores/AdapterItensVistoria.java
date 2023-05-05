
package Adaptadores;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import Atividades.FotoDetalhesActivi;
import Modelos.Item;
import br.com.patrimoniomv.R;

public class AdapterItensVistoria extends RecyclerView.Adapter<AdapterItensVistoria.ItemViewHolder> {
    private List<Item> itens;
    private Context context;

    public AdapterItensVistoria(List<Item> itens, Context context) {
        if (itens != null) {
            this.itens = itens;
        } else {
            this.itens = new ArrayList<>();
        }
        this.context = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vistoria, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itens.get(position);
        FotosAdapter fotosAdapter = new FotosAdapter(item.getFotos(), context);

        // Adicionar o listener para tratar o clique na foto
        fotosAdapter.setOnItemClickListener(new FotosAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String urlFoto) {
                Intent intent = new Intent(context, FotoDetalhesActivi.class);
                intent.putExtra("url_foto", urlFoto);
                intent.putStringArrayListExtra("fotos", new ArrayList<>(item.getFotos())); // Passar a lista de URLs das fotos
                context.startActivity(intent);
            }
        });


        holder.recyclerViewFotos.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerViewFotos.setAdapter(fotosAdapter);
        holder.nomeItem.setText(item.getNome());
        holder.placa.setText(item.getPlaca());
        holder.localizacao.setText(item.getLocalizacao());
        holder.informacoesGerais.setText(item.getObservacao());
        holder.latitude.setText("Latitude: " + item.getLatitude());
        holder.longitude.setText("Longitude: " + item.getLongitude());
    }


    @Override
    public int getItemCount() {
        return itens.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView nomeItem;
        TextView localizacao;
        ImageView fotoItem;
        TextView placa;
        TextView informacoesGerais;
        TextView vistoriador;
        TextView latitude; // Adicione esta linha
        TextView longitude;
        RecyclerView recyclerViewFotos;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeItem = itemView.findViewById(R.id.nomeItemA);
            localizacao = itemView.findViewById(R.id.localizacaoA);
            placa = itemView.findViewById(R.id.placaA);
            fotoItem = itemView.findViewById(R.id.fotoItemA);
            informacoesGerais = itemView.findViewById(R.id.informacoesGeraisA);
            vistoriador = itemView.findViewById(R.id.vistoriadorA);
            latitude = itemView.findViewById(R.id.latitude); // Adicione esta linha
            longitude = itemView.findViewById(R.id.longitude);
            recyclerViewFotos = itemView.findViewById(R.id.recyclerViewFotos);


        }
    }
}

