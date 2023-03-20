package Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import Atividades.DetalhesAc;
import Mode.ItensVistorias;
import br.com.patrimoniomv.R;

public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewHolder> {

    private List<String> imageUrls;
    private Context context;
    private ItensVistorias anuncioSele; // adicione essa variável

    public ViewPagerAdapter(Context context, List<String> imageUrls, ItensVistorias anuncioSele) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.anuncioSele = anuncioSele;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.view_pager_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.get().load(imageUrls.get(position)).into(holder.imageView);
        holder.itemView.setOnClickListener(new View.OnClickListener() { // adicione um listener de click na imagem
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetalhesAc.class);
                intent.putExtra("animalSelecionado", anuncioSele);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }
}
