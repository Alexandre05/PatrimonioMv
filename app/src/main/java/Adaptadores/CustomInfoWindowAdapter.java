package Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import br.com.patrimoniomv.R;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View mWindow;
    private final Context mContext;

    public CustomInfoWindowAdapter(Context context) {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        render(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    private void render(Marker marker, View view) {
        TextView title = view.findViewById(R.id.title);
        TextView placa = view.findViewById(R.id.placa);
        TextView observacao = view.findViewById(R.id.observacao);

        title.setText("Nome: " + getSnippetNome(marker.getSnippet()));
        placa.setText("Placa: " + getSnippetPlaca(marker.getSnippet()));
        observacao.setText("Observação: " + getSnippetObservacao(marker.getSnippet()));
    }




    private String getSnippetPlaca(String snippet) {
        // Implemente a lógica para obter a placa do snippet
        // Exemplo: supondo que a placa esteja entre "Placa: " e "\n"
        String[] parts = snippet.split("Placa: ");
        if (parts.length > 1) {
            return parts[1].split("\n")[0];
        }
        return "";
    }

    private String getSnippetObservacao(String snippet) {
        // Implemente a lógica para obter a observação do snippet
        // Exemplo: supondo que a observação esteja entre "Observação: " e o final do snippet
        String[] parts = snippet.split("Observação: ");
        if (parts.length > 1) {
            return parts[1];
        }
        return "";
    }
    private String getSnippetNome(String snippet) {
        String[] parts = snippet.split("Nome: ");
        if (parts.length > 1) {
            return parts[1].split("\n")[0];
        }
        return "";
    }
}
