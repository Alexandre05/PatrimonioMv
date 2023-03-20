package Atividades;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Picasso;

import Mode.ItensVistorias;
import br.com.patrimoniomv.R;


public class CustomInfo  implements GoogleMap.InfoWindowAdapter  {


    private final View mView;
    private final ItensVistorias anuncio;
    private final Context context;


    public CustomInfo(Context context, ItensVistorias anuncio) {
        this.context = context;
        this.anuncio = anuncio;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.activity_custom_info, null);
        // Construtor padrão vazio
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        TextView tvTitle = mView.findViewById(R.id.title);
        ImageView imageView = mView.findViewById(R.id.imageView);

        tvTitle.setText(anuncio.getNomeItem());

        // Use o Picasso ou outra biblioteca de carregamento de imagens para carregar a imagem.
        if (anuncio.getFotos() != null && !anuncio.getFotos().isEmpty()) {
            Picasso.get().load(anuncio.getFotos().get(0)).into(imageView);
        }

        return mView;
    }
}
