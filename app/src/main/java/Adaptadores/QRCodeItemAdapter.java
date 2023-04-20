package Adaptadores;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import Modelos.Vistorias;
import br.com.patrimoniomv.R;

public class QRCodeItemAdapter extends RecyclerView.Adapter<QRCodeItemAdapter.QRCodeItemViewHolder> {
    private List<Vistorias> itemList;
    private Context context;

    public QRCodeItemAdapter(List<Vistorias> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
        Log.d("QRCodeItemAdapter", "Item list in adapter constructor: " + itemList);
    }
    @NonNull
    @Override
    public QRCodeItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_qrcode_details, parent, false);
        return new QRCodeItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QRCodeItemViewHolder holder, int position) {
        Vistorias item = itemList.get(position);
        Log.d("QRCodeItemAdapter", "Binding view holder at position: " + position + " with item: " + item);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        Log.d("QRCodeItemAdapter", "Item list size: " + itemList.size());
        return itemList.size();
    }

    public static class QRCodeItemViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewItemName;
        private TextView textViewItemLocation;
        private TextView textViewItemInspector;
        private TextView textViewItemOb;
        private TextView textViewItemVistor;
        private TextView textViewItemData;
        private ImageView imageViewFoto;

        public QRCodeItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewItemName = itemView.findViewById(R.id.textView_item_name);
            textViewItemLocation = itemView.findViewById(R.id.textView_item_location);
            textViewItemInspector = itemView.findViewById(R.id.textView_item_NPatr);
            textViewItemData = itemView.findViewById(R.id.textView_item_Data);
            textViewItemVistor = itemView.findViewById(R.id.textView_item_Vistoriador);
            textViewItemOb = itemView.findViewById(R.id.textView_item_OB);
            imageViewFoto = itemView.findViewById(R.id.Imagem);
        }

        public void bind(Vistorias item) {
            textViewItemName.setText("Nome do item: " + item.getNomeItem());
            textViewItemLocation.setText("Localização: " + item.getLocalizacao());
            textViewItemInspector.setText("ID Inspetor: " + item.getIdInspector());
            textViewItemData.setText("Data: " + item.getData());
            textViewItemVistor.setText("Nome Perfil U: " + item.getNomePerfilU());
            textViewItemOb.setText("Outras Informações: " + item.getOutrasInformacoes());

            // Carregar a primeira imagem na ImageView
            if (!item.getFotos().isEmpty()) {
                String imageUrl = item.getFotos().get(0);
                Glide.with(itemView.getContext()).load(imageUrl).into(imageViewFoto);
            }
        }
    }
}
