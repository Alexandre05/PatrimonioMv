package Adaptadores;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Modelos.Vistorias;
import br.com.patrimoniomv.R;

public class AdapterVistorias extends RecyclerView.Adapter<AdapterVistorias.MyViewHolder> {
    private final List<Vistorias> vistoriasList;
    private Context context;

    public AdapterVistorias(List<Vistorias> vistoriasList, Context context) {
        this.vistoriasList = vistoriasList;
        this.context = context;
        Log.i("AdapterVistorias", "Tamanho da lista: " + vistoriasList.size());
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i("AdapterVistorias", "onCreateViewHolder");
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.vistoria_item, parent, false);
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Log.i("AdapterVistorias", "onBindViewHolder - Position: " + position);
        Vistorias vistoria = vistoriasList.get(position);
        Log.i("AdapterVistorias", "Vistoria: " + vistoria.toString());
        // Exibir informações da vistoria
        holder.nomeU.setText(vistoria.getNomePerfilU());
        holder.Data.setText(vistoria.getData());
        holder.locali.setText(vistoria.getLocalizacao());
    }

    @Override
    public int getItemCount() {
        return vistoriasList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView Data;
        TextView nomeU;
        TextView locali;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            locali = itemView.findViewById(R.id.texLocali);
            Data = itemView.findViewById(R.id.textData);
            nomeU = itemView.findViewById(R.id.textNomeUsuario);
        }
    }
}
