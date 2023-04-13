package Atividades;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

import Adapter.ViewPagerAdapter;
import Mode.ItensVistorias;
import br.com.patrimoniomv.R;

public class DetalhesAc extends AppCompatActivity {

    private TextView nome, ob, porte, localiza, nomeVistoriador, latitudeTextView, longitudeTextView;
    private ItensVistorias anuncioSele;
    private Button btnChat;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes);
        iniciarComponentesUI();

        Intent intent = getIntent();
        String idAnuncio = getIntent().getStringExtra("idAnuncio");
        Log.e("DETAILED_ACTIVITY", "IdAnu." + idAnuncio);

        String localizacao = intent.getStringExtra("localizacao");



        if (idAnuncio == null) {
            Log.e("DETAILED_ACTIVITY", "idAnuncio não foi passado para a Activity");
        } else {
            Log.d("DETAILED_ACTIVITY", "idAnuncio: " + idAnuncio);
        }

        if (localizacao == null) {
            Log.e("DETAILED_ACTIVITY", "localizacao não foi passado para a Activity");
        } else {
            Log.d("DETAILED_ACTIVITY", "localizacao: " + localizacao);
        }

        carregarDados(idAnuncio, localizacao);

        // Definir o listener do botão para abrir a tela de mapas
        Button btnViewOnMap = findViewById(R.id.btnViewOnMap);
        btnViewOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetalhesAc.this, MapasActivity.class);
                intent.putExtra("vistorias", anuncioSele);
                startActivity(intent);
            }
        });
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirTelaChat();
            }
        });

    }

    private void carregarDados(String idAnuncio, String localizacao) {

        if (idAnuncio == null) {
            Log.e("DETAILED_ACTIVITY", "idAnuncio é nulo");
            return;
        }

        DatabaseReference anunciosPuRef = FirebaseDatabase.getInstance()
                .getReference("vistoriaPu")
                .child(localizacao) // cai aqui o erro
                .child(idAnuncio);

        Log.d("DETAILED_ACTIVITY", "caminho da consulta: " + anunciosPuRef.toString());
        anunciosPuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    anuncioSele = dataSnapshot.getValue(ItensVistorias.class);
                    Log.d("DETAILED_ACTIVITY", "Dados do anúncio carregados: " + anuncioSele.toString());
                    exibirDados(anuncioSele);
                } else {
                    Log.e("DETAILED_ACTIVITY", "Anúncio não encontrado no Firebase.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("DETAILED_ACTIVITY", "Erro ao carregar dados do anúncio.", databaseError.toException());
            }
        });
    }

    private void exibirDados(ItensVistorias anuncioSele) {
        if (anuncioSele != null) {
            Log.d("DETAILED_ACTIVITY", "vistorias: " + anuncioSele.toString());
            // Definir o nome, outras informações, placa, nome do perfil do usuário e localização nos TextViews
            nome.setText(anuncioSele.getNomeItem());
            ob.setText(anuncioSele.getOutrasInformacoes());
            porte.setText(anuncioSele.getPlaca());
            nomeVistoriador.setText(anuncioSele.getNomePerfilU());
            localiza.setText(anuncioSele.getLocalizacao());

            // Definir a latitude e longitude nos TextViews correspondentes
            String latitudeString = String.format(Locale.getDefault(), "%.6f", anuncioSele.getLatitude());
            latitudeTextView.setText(getString(R.string.latitude_value, latitudeString));
            String longitudeString = String.format(Locale.getDefault(), "%.6f", anuncioSele.getLongetude());
            longitudeTextView.setText(getString(R.string.longitude_value, longitudeString));

            // Obter as URLs das imagens do Firebase Storage
            List<String> imageUrls = anuncioSele.getFotos();

            // Definir as imagens no ViewPager
            ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, imageUrls, anuncioSele);
            viewPager.setAdapter(viewPagerAdapter);
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();
        }
    }
    private void abrirTelaChat() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Se o usuário estiver logado, inicie a atividade de chat
            Intent chatIntent = new Intent(DetalhesAc.this, Chat.class);
            startActivity(chatIntent);
        } else {
            // Se o usuário não estiver logado, mostre um aviso
            Toast.makeText(this, "Você precisa estar logado para acessar o chat.", Toast.LENGTH_SHORT).show();
        }
    }

    private void iniciarComponentesUI() {
        viewPager = findViewById(R.id.view_pager3);
        btnChat=findViewById(R.id.buttonMsf);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager3);
        nome = findViewById(R.id.textView4);
        localiza = findViewById(R.id.textView5);
        ob = findViewById(R.id.textView6);
        nomeVistoriador = findViewById(R.id.Vistoriador);
        porte = findViewById(R.id.Placa);
        latitudeTextView = findViewById(R.id.textLatitude);
        longitudeTextView = findViewById(R.id.textLongitude);
    }
}
