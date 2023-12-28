package Atividades;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import Adaptadores.CustomInfoWindowAdapter;
import Modelos.Item;
import br.com.patrimoniomv.R;

public class MapasActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Item> itensList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapas);  // Certifique-se de usar o layout correto

        if (getIntent().hasExtra("itensList")) {
            itensList = (List<Item>) getIntent().getSerializableExtra("itensList");

            if (itensList.isEmpty()) {
                handleEmptyItemList();
                return;
            }

            setupMapFragment();
        } else {
            handleMissingItemList();
        }
    }

    private void changeMapType(int mapType) {
        if (mMap != null) {
            mMap.setMapType(mapType);
        } else {
            Log.e("MapasActivity", "Mapa não inicializado ainda");
        }
    }

    private void handleEmptyItemList() {
        Log.w("MapasActivity", "A lista de itens está vazia. Nenhum marcador será exibido.");
        // Lide com a situação de lista vazia conforme necessário
    }

    private void handleMissingItemList() {
        Log.e("MapasActivity", "Lista de itens não encontrada");
        // Lide com a situação de lista nula conforme necessário
    }

    private void setupMapFragment() {
        Log.d("MapasActivity", "Configurando o fragmento do mapa.");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.conte);
        Button btnNormal = findViewById(R.id.btnNormal);
        Button btnSatellite = findViewById(R.id.btnSatellite);
        Button btnHybrid = findViewById(R.id.btnHybrid);

        btnNormal.setOnClickListener(v -> {
            Log.d("MapasActivity", "Botão Normal clicado");
            changeMapType(GoogleMap.MAP_TYPE_NORMAL);
        });
        btnSatellite.setOnClickListener(v -> {
            Log.d("MapasActivity", "Botão Satellite clicado");
            changeMapType(GoogleMap.MAP_TYPE_SATELLITE);
        });
        btnHybrid.setOnClickListener(v -> {
            Log.d("MapasActivity", "Botão Hybrid clicado");
            changeMapType(GoogleMap.MAP_TYPE_HYBRID);
        });

        // Certifique-se de que o mapFragment não é nulo antes de tentar obter o mapa assincronamente
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            Log.d("MapasActivity", "getMapAsync chamado.");
        } else {
            Log.e("MapasActivity", "Fragmento do mapa não encontrado");
        }
    }


    // ...

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MapasActivity", "Mapa pronto! Método onMapReady chamado.");
        mMap = googleMap;
        CustomInfoWindowAdapter infoWindowAdapter = new CustomInfoWindowAdapter(this);
        mMap.setInfoWindowAdapter(infoWindowAdapter);

        // Adiciona marcadores e ajusta a câmera após a inicialização do mapa
        if (!itensList.isEmpty()) {
            addMarkersToMap();
            adjustCameraBounds();
            setupMapUI();  // Adicionado método para configurar a interface do mapa
        } else {
            handleEmptyItemList();
        }
    }

    private void setupMapUI() {
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        mMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            return true;
        });
    }


    private void addMarkersToMap() {
        for (Item item : itensList) {
            LatLng itemLocation = new LatLng(item.getLatitude(), item.getLongitude());
            MarkerOptions markerOptions = createMarkerOptions(item);
            mMap.addMarker(markerOptions);
        }
    }

    private void adjustCameraBounds() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Item item : itensList) {
            builder.include(new LatLng(item.getLatitude(), item.getLongitude()));
        }
        LatLngBounds bounds = builder.build();
        int padding = 50; // Ajuste conforme necessário
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }

    private MarkerOptions createMarkerOptions(Item item) {
        Log.d("MarkerDebug", "Criando marcador para: " + item.getNome());

        String snippetText = String.format("Nome: %s\nPlaca: %s\nObservação: %s", item.getNome(), item.getPlaca(), item.getObservacao());

        LatLng itemLocation = new LatLng(item.getLatitude(), item.getLongitude());

        // Adiciona logs para verificar os dados
        Log.d("MarkerDebug", "Nome: " + item.getNome());
        Log.d("MarkerDebug", "Placa: " + item.getPlaca());
        Log.d("MarkerDebug", "Position: " + itemLocation.toString());

        return new MarkerOptions()
                .position(itemLocation)
                .title(item.getNome())
                .snippet(snippetText)
                .icon(BitmapDescriptorFactory.defaultMarker());
    }
}
