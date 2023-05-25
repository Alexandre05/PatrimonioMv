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

import Modelos.Vistoria;
import br.com.patrimoniomv.R;


public class AdapterVistorias extends RecyclerView.Adapter<AdapterVistorias.MyViewHolder> {
    private final List<Vistoria> vistorias;
    private Context context;

    public AdapterVistorias(List<Vistoria> vistorias, Context context) {
        this.vistorias = vistorias;
        this.context = context;
        if (vistorias != null) {
            Log.i("AdapterVistorias", "Tamanho da lista: " + vistorias.size());
        } else {
            Log.i("AdapterVistorias", "A lista de vistorias está nula.");
        }
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
        if (vistorias != null) {
            Log.i("AdapterVistorias", "onBindViewHolder - Position: " + position);
            Vistoria vistoria = vistorias.get(position);

            // Exibir informações da vistoria
            holder.nomeU.setText(vistoria.getNomePerfilU());
            holder.Data.setText(vistoria.getData());
            holder.locali.setText(vistoria.getLocalizacao());
            Log.i("AdapterVistorias", "Nome do Usuário: " + vistoria.getNomePerfilU());
            Log.i("AdapterVistorias", "Data: " + vistoria.getData());
            Log.i("AdapterVistorias", "Localização: " + vistoria.getLocalizacao());
        } else {
            Log.i("AdapterVistorias", "A lista de vistorias está nula.");
        }
    }

    @Override
    public int getItemCount() {
        if (vistorias != null) {
            Log.i("AdapterVistorias", "getItemCount: " + vistorias.size());
            return vistorias.size();
        } else {
            Log.i("AdapterVistorias", "A lista de vistorias está nula.");
            return 0;
        }
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
