package Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import br.com.patrimoniomv.R;

public class FotosAdapter extends RecyclerView.Adapter<FotosAdapter.FotoViewHolder> {
    private List<String> fotos;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public FotosAdapter(List<String> fotos, Context context) {
        this.fotos = fotos;
        this.context = context;
    }

    @NonNull
    @Override
    public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_foto, parent, false);
        return new FotoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FotoViewHolder holder, int position) {
        String urlFoto = fotos.get(position);

        Glide.with(context)
                .load(urlFoto)
                .into(holder.imageViewFoto);
    }

    @Override
    public int getItemCount() {
        return fotos.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(String urlFoto);
    }

    public class FotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewFoto;

        public FotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewFoto = itemView.findViewById(R.id.imageViewFoto);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(fotos.get(getAdapterPosition()));
                    }
                }
            });
        }
    }
}
