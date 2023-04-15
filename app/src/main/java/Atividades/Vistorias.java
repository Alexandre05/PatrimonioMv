package Atividades;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Adaptadores.AdapterAnuncios;
import Ajuda.ConFirebase;
import Modelos.ItensVistorias;
import Modelos.RecyclerItemClickListener;

import Modelos.Usuario;
import br.com.patrimoniomv.R;
import dmax.dialog.SpotsDialog;

public class Vistorias extends AppCompatActivity {
    private FirebaseAuth autenticacao;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewPu;
    private EditText nomeUsuario;
    private Button animais, bairoo;
    private EditText campoEmail, campoSenha;
    private DatabaseReference anunciosRef;
    private List<ItensVistorias> listacioItens = new ArrayList<>();
    private AdapterAnuncios adapterAnuncios;
    private AlertDialog alertDialog;
    private String filtroItens = "";
    private boolean filtrandoPorLocalizacao = false;
    private String filtroBairro = "";

    private boolean isAdmin = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_animais);
        //Toolbar tolbar= findViewById(R.id.Toolbar);
        this.setTitle("Comissão  de Patrimonio ");
        String idAnuncio = getIntent().getStringExtra("idAnuncio");

        navigation();
        inicializarCompo();

        autenticacao = ConFirebase.getReferenciaAutencicacao();
        anunciosRef = ConFirebase.getFirebaseDatabase().child("vistorias");
        //autenticacao.signOut();
        recyclerViewPu.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPu.setHasFixedSize(true);
        adapterAnuncios = new AdapterAnuncios(listacioItens, this);
        recyclerViewPu.setAdapter(adapterAnuncios);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            getUserFromFirebase(currentUser.getUid(), new OnUserLoadedListener() {
                @Override
                public void onUserLoaded(Usuario usuario) {
                    if (usuario != null) {
                        isAdmin = usuario.isUserAdmin();

                    }
                }
            });
        }
        recuperarVistoriasPublicas(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                atualizarDados();
            }
        });
// aqui faz o toque na tela para abrir
        recyclerViewPu.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerViewPu, new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                Log.e("DETAILED_ACTIVITY", "Onclik.");
                ItensVistorias anuncioSelecionado = listacioItens.get(position);

                Intent i = new Intent(Vistorias.this, DetalhesAc.class);
                i.putExtra("idAnuncio", anuncioSelecionado.getIdAnuncio()); // Altere esta linha
                i.putExtra("localizacao", anuncioSelecionado.getLocalizacao());
                startActivity(i);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }

        ));


    }


    private void getUserFromFirebase(String uid, final OnUserLoadedListener listener) {
        DatabaseReference userRef = ConFirebase.getFirebaseDatabase().child("usuarios").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);
                    listener.onUserLoaded(usuario);
                } else {
                    listener.onUserLoaded(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onUserLoaded(null);
            }
        });
    }


    private interface OnUserLoadedListener {
        void onUserLoaded(Usuario usuario);
    }
    @Override
    protected void onStart() {
        super.onStart();
        anunciosRef = ConFirebase.getFirebaseDatabase().child("vistoriaPu");
    }

    @Override
    protected void onStop() {
        super.onStop();
        anunciosRef.removeEventListener(valueEventListenerAnuncios);
    }

    private ValueEventListener valueEventListenerAnuncios = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            listacioItens.clear();
            for (DataSnapshot localizacao : snapshot.getChildren()) {
                for (DataSnapshot lo : localizacao.getChildren()) {
                    // Verifique se o anúncio ainda existe antes de adicioná-lo à lista
                    if (lo.exists()) {
                        ItensVistorias anuncio = lo.getValue(ItensVistorias.class);

                        // Adicione uma verificação adicional para garantir que o anúncio seja válido
                        if (anuncio != null && anuncio.getIdAnuncio() != null && !anuncio.getIdAnuncio().isEmpty()) {
                            listacioItens.add(anuncio);
                        }
                    }
                }
            }
            Collections.reverse(listacioItens);
            adapterAnuncios.notifyDataSetChanged();
            alertDialog.dismiss();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
    private void recuperarVistoriasPublicas(boolean firTime) {
        alertDialog = new SpotsDialog.Builder(this)
                .setMessage("Recuperando  Vistorias").setCancelable(false).show();
        alertDialog.show();
        anunciosRef = ConFirebase.getFirebaseDatabase().child("vistoriaPu");

        // Adicione esta linha para remover o listener anterior antes de adicionar um novo
        anunciosRef.removeEventListener(valueEventListenerAnuncios);

        anunciosRef.addValueEventListener(valueEventListenerAnuncios);
    }


    private void atualizarDados() {
        // Remova o listener anterior, caso exista
        if (valueEventListenerAnuncios != null) {
            anunciosRef.removeEventListener(valueEventListenerAnuncios);
        }

        // Recupere os dados novamente do Firebase
        recuperarVistoriasPublicas(false);

        // Atualize o RecyclerView
        adapterAnuncios.notifyDataSetChanged();

        // Pare o ícone de carregamento do SwipeRefreshLayout
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            menu.setGroupVisible(R.id.group_deslogado, true);
        } else {
            menu.setGroupVisible(R.id.group_logado, true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_cadastrar:
                startActivity(new Intent(getApplicationContext(), Login.class));
                break;
            case R.id.menu_relatorio:
                startActivity(new Intent(getApplicationContext(), Relatorios.class));
                break;
            case R.id.menu_perfil:
                startActivity(new Intent(getApplicationContext(), Perfil.class));
                break;
            case R.id.menu_compartilhar:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/play");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Baixe o App Seu Pet");
                intent.putExtra(Intent.EXTRA_TEXT, "kkkkk");
                startActivity(Intent.createChooser(intent, "Compartilhar"));
                break;
            case R.id.menu_sair:
                autenticacao.signOut();
                invalidateOptionsMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void inicializarCompo() {
        recyclerViewPu = findViewById(R.id.ryclePublico);
        campoEmail = findViewById(R.id.emialLog);
        campoSenha = findViewById(R.id.senhaLog);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }
    private void navigation() {
        BottomNavigationView bottom = findViewById(R.id.bnve);
        loadNavigation(bottom);
        Menu menu = bottom.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);
    }

    private void loadNavigation(BottomNavigationView view) {
        view.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.Ad:
                        if (isAdmin) {
                            startActivity(new Intent(getApplicationContext(), Admininistrar.class));
                        }
                        break;
                    case R.id.ic_minhas:
                        startActivity(new Intent(getApplicationContext(), MinhasVistorias.class));
                        break;
                    case R.id.ic_perfil:
                        Intent intent = new Intent(Vistorias.this, VistoriasEmAndamentoActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.inicial:
                        recreate();
                        break;
                }
                return true;
            }
        });
    }

}
