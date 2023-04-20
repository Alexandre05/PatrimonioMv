package Atividades;

import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import Modelos.Vistorias;
import br.com.patrimoniomv.R;

public class MapasActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Vistorias anuncio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapas);

        anuncio = (Vistorias) getIntent().getSerializableExtra("anuncio");

        // Obtenha o SupportMapFragment e seja notificado quando o mapa estiver pronto para ser usado.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);



        if (mapFragment == null) {
            Log.e("MapasActivity", "SupportMapFragment não encontrado");
            return;
        }

        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Adicione um marcador para a localização do item e mova a câmera.
        LatLng itemLocation = new LatLng(anuncio.getLatitude(), anuncio.getLongetude());


        Marker marker = mMap.addMarker(new MarkerOptions().position(itemLocation).title(anuncio.getNomeItem()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(itemLocation, 15));

        // Configure o adaptador personalizado da janela de informações.
        GoogleMap.InfoWindowAdapter adapter = new CustomInfo(this, anuncio);
        mMap.setInfoWindowAdapter(adapter);

        // Abra a janela de informações do marcador.
        marker.showInfoWindow();
    }
}
