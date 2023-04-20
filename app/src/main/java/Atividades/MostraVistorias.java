package Atividades;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
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
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import Adaptadores.AdapterVistorias;
import Ajuda.ConFirebase;
import Modelos.Item;
import Modelos.RecyclerItemClickListener;

import Modelos.Usuario;
import Modelos.Vistorias;
import br.com.patrimoniomv.R;
import dmax.dialog.SpotsDialog;

public class MostraVistorias extends AppCompatActivity {
    private FirebaseAuth autenticacao;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewPu;

    private DatabaseReference anunciosRef;


    private AlertDialog alertDialog;
    private String filtroItens = "";
    private boolean filtrandoPorLocalizacao = false;
    private String filtroBairro = "";
    private AdapterVistorias adapterAnuncios;
    private List<Modelos.Vistorias> vistoriasList = new ArrayList<>();

    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mostrar_vistoriaspublica);
        setTitle("Comissão de Patrimonio");

        navigation();
        inicializarCompo();

        setupFirebase();
        setupRecyclerView();
        setupSwipeRefresh();

        anunciosRef = ConFirebase.getFirebaseDatabase().child("vistoriaPu");
        recuperarVistoriasPublicasAsync();
        vistoriasList = new ArrayList<>();
        adapterAnuncios = new AdapterVistorias(vistoriasList, this);
        // recyclerViewPu.setAdapter(adapterAnuncios);
        getCurrentUser();
    }

    private void setupFirebase() {
        FirebaseApp.initializeApp(this);
        anunciosRef = ConFirebase.getFirebaseDatabase().child("vistoriaPu");
    }

    private void setupRecyclerView() {
        recyclerViewPu.setAdapter(adapterAnuncios);
        recyclerViewPu.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerViewPu, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Modelos.Vistorias anuncioSelecionado = vistoriasList.get(position);

                Intent i = new Intent(MostraVistorias.this, DetalhesAc.class);
                i.putExtra("idAnuncio", anuncioSelecionado.getIdVistoria());
                i.putExtra("localizacao", anuncioSelecionado.getLocalizacao());
                startActivity(i);
            }

            @Override
            public void onLongItemClick(View view, int position) {
            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        }));
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::recuperarVistoriasPublicasAsync);
    }
    private void recuperarVistoriasPublicasAsync() {
        showProgressDialog(true);

        anunciosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                vistoriasList.clear();

                for (DataSnapshot localizacaoSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot vistoriaSnapshot : localizacaoSnapshot.getChildren()) {
                        DataSnapshot dadosGeraisSnapshot = vistoriaSnapshot.child("DadosGerais");
                        Modelos.Vistorias vistoria;

                        try {
                            vistoria = dadosGeraisSnapshot.getValue(Modelos.Vistorias.class);
                        } catch (DatabaseException e) {
                            // Se ocorrer uma exceção ao converter os campos, pule esta vistoria
                            continue;
                        }

                        // Verifica se a vistoria é válida antes de adicioná-la à lista
                        if (vistoria != null) {
                            vistoria.setLocalizacao(localizacaoSnapshot.getKey());

                            // Recuperar itens dentro da vistoria
                            List<Modelos.Item> itemList = new ArrayList<>();
                            for (DataSnapshot itemSnapshot : dadosGeraisSnapshot.child("itens").getChildren()) {
                                Modelos.Item item;
                                try {
                                    item = itemSnapshot.getValue(Modelos.Item.class);
                                } catch (DatabaseException e) {
                                    // Se ocorrer uma exceção ao converter os campos, pule este item
                                    continue;
                                }

                                // Verifica se o item é válido antes de adicioná-lo à lista
                                if (item != null) {
                                    itemList.add(item);
                                }
                            }
                            vistoria.setItens(itemList); // Adicione a lista de itens à vistoria

                            vistoriasList.add(vistoria);
                        }
                    }
                }

                Log.i("recuperarVistorias", "Tamanho da lista de vistorias: " + vistoriasList.size());
                adapterAnuncios.notifyDataSetChanged();
                showProgressDialog(false);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("recuperarVistorias", "Error: " + databaseError.getMessage());
                showProgressDialog(false);
            }
        });
    }





    private void showProgressDialog(boolean show) {
        if (show) {
            if (alertDialog == null || !alertDialog.isShowing()) {
                alertDialog = new SpotsDialog.Builder(this).setMessage("Recuperando Vistorias").setCancelable(false).create();
                alertDialog.show();
            }
        } else {
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        }
    }


    private void getCurrentUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            getUserFromFirebase(currentUser.getUid(), usuario -> {
                if (usuario != null) {
                    isAdmin = usuario.isUserAdmin();
                }
            });
        }

        //recuperarVistoriasPublicas();
    }

    private void getUserFromFirebase(String uid, OnUserLoadedListener listener) {
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
            case R.id.menuChat:
                startActivity(new Intent(getApplicationContext(), Chat.class));
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
                        Intent intent = new Intent(MostraVistorias.this, VistoriasEmAndamentoActivity.class);
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
