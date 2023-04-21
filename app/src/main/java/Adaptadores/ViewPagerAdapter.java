package Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import Modelos.Vistoria;
import br.com.patrimoniomv.R;

public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewHolder> {

    private List<String> imageUrls;
    private Context context;
    private Vistoria anuncioSele;

    public ViewPagerAdapter(Context context, List<String> imageUrls, Vistoria anuncioSele) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.anuncioSele = anuncioSele;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_pager_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String imageUrl = imageUrls.get(position);
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Picasso.get().load(imageUrl).into(holder.imageView);
            } else {
                // Carregue uma imagem padrão ou faça alguma outra ação em caso de URL inválido
            }
        }

    }
    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.viewPager);
        }
    }
}
