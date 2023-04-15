package Adaptadores;

import android.content.Context;

import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Modelos.ItensVistorias;
import br.com.patrimoniomv.R;

public class AdapterAnuncios extends RecyclerView.Adapter<AdapterAnuncios.MyViewHolder> {
    private List<ItensVistorias> anuncios;

    private Context context;

    public AdapterAnuncios(List<ItensVistorias> anuncios, Context context) {
        this.anuncios = anuncios;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder (@NonNull ViewGroup parent,
                                            int viewType){
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_adpeter_anuncios, parent, false);

        return new MyViewHolder( item );
    }

    @Override
    public void onBindViewHolder (@NonNull MyViewHolder holder,int position){

        ItensVistorias anuncio= anuncios.get(position);
        holder.nomeItem.setText(anuncio.getNomeItem());
        holder.nomeU.setText(anuncio.getNomePerfilU());
        holder.placa.setText(anuncio.getPlaca());
        holder.ob.setText(anuncio.getOutrasInformacoes());
        holder.Data.setText(anuncio.getData());
        holder.locali.setText(anuncio.getLocalizacao());

        List<String> urlFtos = anuncio.getFotos();

        if (urlFtos != null && urlFtos.size() > 0) {
            String urlCapa = urlFtos.get(0);

            // Limpando a imagem antes de carregar
            Picasso.get().cancelRequest(holder.foto);

            // Carregando a imagem
            Picasso.get().load(urlCapa).into(holder.foto);
            Log.i("foto", "men" + urlCapa);
        } else {
            holder.foto.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount () {
        return anuncios.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nomeItem;
        TextView placa;
        TextView ob;
        TextView  Data;
        ImageView foto;
        TextView nomeU;
        TextView locali;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            locali=itemView.findViewById(R.id.texLocali);
            nomeItem= itemView.findViewById(R.id.texVilNomeItem);
            placa=itemView.findViewById(R.id.textViewPlaca);
            ob=itemView.findViewById(R.id.textViewOb);
            foto=itemView.findViewById(R.id.imageAnuncios);
            Data=itemView.findViewById(R.id.textData);
            nomeU=itemView.findViewById(R.id.textNomeUsuario);
            //Editar=itemView.findViewById(R.id.EditarNome);

        }
    }
}
