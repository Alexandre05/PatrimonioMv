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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Atividades.FotoDetalhesActivi;
import Atividades.MapasActivity; // Importe a classe MapasActivity
import Modelos.Item;
import br.com.patrimoniomv.R;

public class AdapterItensVistoriaParaEditar extends RecyclerView.Adapter<AdapterItensVistoriaParaEditar.ItemViewHolder> {
    private List<Item> itens;
    private Context context;

    public AdapterItensVistoriaParaEditar(List<Item> itens, Context context) {
        if (itens != null) {
            this.itens = itens;
        } else {
            this.itens = new ArrayList<>();
        }
        this.context = context;
    }
    // Adicione o seguinte construtor


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
        if (isUsuarioVistoriador()) {
            // Permitir a edição para vistoriadores
            holder.mapa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    abrirMapa(item.getLatitude(), item.getLongitude());
                }
            });
        } else {
            // Ocultar ou desativar a ação para o público
            holder.mapa.setVisibility(View.GONE); // ou setOnClickListener(null) para desativar
        }

        holder.recyclerViewFotos.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerViewFotos.setAdapter(fotosAdapter);
        holder.nomeItem.setText(item.getNome());
        holder.placa.setText(item.getPlaca());
        holder.localizacao.setText(item.getLocalizacao());
        holder.informacoesGerais.setText(item.getObservacao());
        holder.latitude.setText("Latitude: " + item.getLatitude());
        holder.longitude.setText("Longitude: " + item.getLongitude());

        // Verificar se é um vistoriador para permitir a edição
        if (isUsuarioVistoriador()) {
            // Permitir a edição para vistoriadores
            holder.mapa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    abrirMapa(item.getLatitude(), item.getLongitude());
                }
            });
        } else {
            // Ocultar ou desativar a ação para o público
            holder.mapa.setVisibility(View.GONE); // ou setOnClickListener(null) para desativar
        }
    }

    private void abrirMapa(double latitude, double longitude) {
        // Crie um Intent para abrir a tela do mapa
        Intent mapaIntent = new Intent(context, MapasActivity.class);

        // Passe os dados de localização e a lista de itens para a tela do mapa
        mapaIntent.putExtra("latitude", latitude);
        mapaIntent.putExtra("longitude", longitude);
        mapaIntent.putExtra("itensList", (Serializable) itens);

        context.startActivity(mapaIntent);
    }


    private boolean isUsuarioVistoriador() {
        return true; // Adicione sua lógica para verificar se o usuário é vistoriador
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView nomeItem, mapa;
        TextView localizacao;
        TextView locali;
        ImageView fotoItem;
        TextView placa;
        TextView informacoesGerais;
        TextView vistoriador;
        TextView latitude;
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
            latitude = itemView.findViewById(R.id.latitude);
            longitude = itemView.findViewById(R.id.longitude);
            recyclerViewFotos = itemView.findViewById(R.id.recyclerViewFotos);
            mapa = itemView.findViewById(R.id.BtnViewOnMa);
            locali = itemView.findViewById(R.id.Novolocalizacao);
        }
    }
}
