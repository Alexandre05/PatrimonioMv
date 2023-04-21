package Adaptadores;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import Modelos.Vistoria;
import br.com.patrimoniomv.R;

public class DetalhesVistoriaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Vistoria vistoria;
    private Context context;

    public DetalhesVistoriaAdapter(Vistoria vistoria, Context context) {
        this.vistoria = vistoria;
        this.context = context;
    }

    @NonNull
    @Override
    public AdapterVistorias.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_detalhes, parent, false);
        return new AdapterVistorias.MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            //headerHolder.nomeItem.setText(vistoria.getNomeItem());
            headerHolder.localizacao.setText(vistoria.getLocalizacao());
            //headerHolder.placa.setText(vistoria.getPlaca());
            //headerHolder.informacoesGerais.setText(vistoria.getOutrasInformacoes());
            headerHolder.vistoriador.setText(vistoria.getNomePerfilU());
        } else if (holder instanceof FotosViewHolder) {
            FotosViewHolder fotosHolder = (FotosViewHolder) holder;
            List<String> urlFotos = vistoria.getFotos();

            if (urlFotos != null && position - 1 < urlFotos.size()) {
                String urlFoto = urlFotos.get(position - 1);

                // Limpando a imagem antes de carregar
                Picasso.get().cancelRequest(fotosHolder.foto);

                // Carregando a imagem
                Picasso.get().load(urlFoto).into(fotosHolder.foto);
            } else {
                fotosHolder.foto.setImageDrawable(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return (vistoria.getFotos() != null ? vistoria.getFotos().size() : 0) + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView nomeItem;
        TextView localizacao;
        TextView placa;
        TextView informacoesGerais;
        TextView vistoriador;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeItem = itemView.findViewById(R.id.nomeItemA);
            localizacao = itemView.findViewById(R.id.localizacaoA);
            placa = itemView.findViewById(R.id.placaA);
            informacoesGerais = itemView.findViewById(R.id.informacoesGeraisA);
            vistoriador = itemView.findViewById(R.id.vistoriadorA);
        }
    }

    public static class FotosViewHolder extends RecyclerView.ViewHolder {
        ImageView foto;

        public FotosViewHolder(@NonNull View itemView) {
            super(itemView);
            foto = itemView.findViewById(R.id.foto);
        }
    }
}




