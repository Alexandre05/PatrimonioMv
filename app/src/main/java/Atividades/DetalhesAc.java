package Atividades;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Adaptadores.AdapterVistorias;
import Adaptadores.DetalhesVistoriaAdapter;
import Adaptadores.ViewPagerAdapter;
import Modelos.Vistoria;
import br.com.patrimoniomv.R;

public class DetalhesAc extends AppCompatActivity {

    private TextView nome, ob, porte, localiza, nomeVistoriador, latitudeTextView, longitudeTextView;
    private Vistoria anuncioSele;
    private String idVistoria;
    private RecyclerView recyclerView;
    private DetalhesVistoriaAdapter detalhesVistoriaAdapter;
    private Button btnChat;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes);
        iniciarComponentesUI();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Obter o ID da vistoria da Intent
        Intent intent = getIntent();
        // Obter o ID da vistoria e a localização da Intent
        idVistoria = getIntent().getStringExtra("idAnuncio");
        String localizacao = getIntent().getStringExtra("localizacao");;

        carregarDados(idVistoria, localizacao);


        // Verificar se o ID da vistoria foi passado para a Activity
        if (idVistoria == null) {
            Log.e("DETAILED_ACTIVITY", "idVistoria não foi passado para a Activity");
            return;
        } else {
            Log.d("DETAILED_ACTIVITY", "idVistoria: " + idVistoria);
        }

        // Verificar se a localização foi passada para a Activity
        if (localizacao == null) {
            Log.e("DETAILED_ACTIVITY", "localizacao não foi passada para a Activity");
            return;
        } else {
            Log.d("DETAILED_ACTIVITY", "localizacao: " + localizacao);
        }

        // Carregar os dados da vistoria usando o adaptador
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("vistorias").child(idVistoria);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Vistoria vistoria = snapshot.getValue(Vistoria.class);

                if (vistoria != null) {
                    DetalhesVistoriaAdapter adapter = new DetalhesVistoriaAdapter(vistoria, DetalhesAc.this);
                    recyclerView.setAdapter(adapter);
                } else {
                    Log.e("DETAILED_ACTIVITY", "Vistoria não encontrada com o ID: " + idVistoria);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DETAILED_ACTIVITY", "Erro ao carregar os dados da vistoria: " + error.getMessage());
            }
        });

        // Definir o listener do botão para abrir a tela de mapas
        Button btnViewOnMap = findViewById(R.id.btnViewOnMapA);
        btnViewOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewOnMap();
            }
        });
    }

    private void viewOnMap() {
        String idVistoria = getIntent().getStringExtra("idVistoria");
        String localizacao = getIntent().getStringExtra("localizacao");

        DatabaseReference vistoriasRef = FirebaseDatabase.getInstance()
                .getReference("vistoriaPu")
                .child(localizacao)
                .child(idVistoria);

        vistoriasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Vistoria vistoria = dataSnapshot.getValue(Vistoria.class);

                    if (vistoria.getLatitude() != null && vistoria.getLongetude() != null) {
                        double latitude = (vistoria.getLatitude());
                        double longitude = (vistoria.getLongetude());

                        // Abrir o aplicativo de mapas na localização da vistoria
                        Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(" + vistoria.getLocalizacao() + ")");
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        } else {
                            Toast.makeText(DetalhesAc.this, "Não foi possível abrir o mapa. Por favor, instale o Google Maps.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(DetalhesAc.this, "Coordenadas não disponíveis para esta vistoria.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("DETAILED_ACTIVITY", "Nenhuma vistoria encontrada no Firebase.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("DETAILED_ACTIVITY", "Erro ao carregar dados da vistoria.", databaseError.toException());
            }
        });
    }

    private void carregarDados(String idVistoria, String localizacao) {
        if (idVistoria == null || localizacao == null) {
            Log.e("DETAILED_ACTIVITY", "idVistoria ou localizacao é nulo");
            return;
        }

        DatabaseReference vistoriasRef = FirebaseDatabase.getInstance()
                .getReference("vistoriaPu")
                .child(localizacao)
                .child(idVistoria);

        vistoriasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Vistoria vistoria = dataSnapshot.getValue(Vistoria.class);
                    exibirDados(vistoria);
                    carregarLista(vistoria);
                } else {
                    Log.e("DETAILED_ACTIVITY", "Nenhuma vistoria encontrada no Firebase.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("DETAILED_ACTIVITY", "Erro ao carregar dados da vistoria.", databaseError.toException());
            }
        });
    }

    // Nova função para configurar o RecyclerView e o Adapter
    private void carregarLista(Vistoria vistoria) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Vistoria> vistoriasList = new ArrayList<>();
        vistoriasList.add(vistoria);
        AdapterVistorias adapterVistorias = new AdapterVistorias(vistoriasList, this);
        recyclerView.setAdapter(adapterVistorias);
    }

    private void exibirDados(Vistoria vistoriaSelecionada) {
        if (vistoriaSelecionada != null) {
            Log.d("DETAILED_ACTIVITY", "vistoria: " + vistoriaSelecionada.toString());
            // Definir o nome, outras informações, placa, nome do perfil do usuário e localização nos TextViews

            nomeVistoriador.setText(vistoriaSelecionada.getNomePerfilU());
            localiza.setText(vistoriaSelecionada.getLocalizacao());
            // Definir a latitude e longitude nos TextViews correspondentes
            String latitudeString = String.valueOf(vistoriaSelecionada.getLatitude());
            latitudeTextView.setText(getString(R.string.latitude_value, latitudeString));
            String longitudeString = String.valueOf(vistoriaSelecionada.getLongetude());
            longitudeTextView.setText(getString(R.string.longitude_value, longitudeString));



            // Obter as URLs das imagens do Firebase Storage
            List<String> imageUrls = vistoriaSelecionada.getFotos();

            if (imageUrls != null && imageUrls.size() > 0) {
                // Definir as imagens no ViewPager
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, imageUrls, vistoriaSelecionada);
                viewPager.setAdapter(viewPagerAdapter);
                new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();
            }
        }
    }




    private void iniciarComponentesUI() {
        nome=findViewById(R.id.nomeItemA);
        localiza=findViewById(R.id.localizacaoA);
        porte=findViewById(R.id.placaA);
        ob=findViewById(R.id.informacoesGeraisA);
        nomeVistoriador=findViewById(R.id.vistoriadorA);
        latitudeTextView=findViewById(R.id.latitude);
        longitudeTextView=findViewById(R.id.longitude);
    }

}
