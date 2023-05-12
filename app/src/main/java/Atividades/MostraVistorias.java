package Atividades;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Adaptadores.AdapterVistorias;
import Ajuda.ConFirebase;
import Modelos.Item;
import Modelos.RecyclerItemClickListener;
import Modelos.Usuario;
import Modelos.Vistoria;
import br.com.patrimoniomv.R;
import dmax.dialog.SpotsDialog;

public class MostraVistorias extends AppCompatActivity {
    private FirebaseAuth autenticacao;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView nomeItem,placa,informacoes,latitude,longetude;
    private RecyclerView recyclerViewPu;
    private DatabaseReference anunciosRef;
    private AlertDialog alertDialog;
    private String filtroItens = "";
    private boolean filtrandoPorLocalizacao = false;
    private String filtroBairro = "";
    private AdapterVistorias adapterVistorias;
    List<Vistoria> Listadevistorias = new ArrayList<>();

    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mostrar_vistoriaspublica);
        setTitle("Comissão de Patrimonio");


        navigation();
        inicializarCompo();

        setupFirebase();
        setupSwipeRefresh();
        Listadevistorias = new ArrayList<>();
        adapterVistorias = new AdapterVistorias(Listadevistorias, this);


        recuperarVistoriasTelaMostrar();

        setupRecyclerView();
        getCurrentUser();
        autenticacao = FirebaseAuth.getInstance();

    }

    private void setupFirebase() {
        anunciosRef = ConFirebase.getFirebaseDatabase().child("vistoriaPu");
    }

    private void setupRecyclerView() {
        recyclerViewPu.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPu.setAdapter(adapterVistorias);
        recyclerViewPu.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerViewPu, new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                Vistoria anuncioSelecionado = Listadevistorias.get(position);
                Log.d("MostraVistorias", "Item clicado: " + position + ", ID da vistoria: " + anuncioSelecionado.getIdVistoria());
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    ArrayList<Item> listaItens = new ArrayList<>(anuncioSelecionado.getItensMap().values());
                    Log.d("MostraVistorias", "Quantidade de itens na lista: " + listaItens.size());  // Adicionar este log
                    for (Item item : listaItens) {
                        Log.d("MostraVistorias", "Item: " + item.toString());  // Adicionar este log
                    }

                    Intent i = new Intent(MostraVistorias.this, DetalhesAc.class);
                    i.putExtra("vistorias", anuncioSelecionado);
                    i.putExtra("idVistoria", anuncioSelecionado.getIdVistoria());
                    i.putExtra("localizacao", anuncioSelecionado.getLocalizacao());
                    i.putExtra("itensList", listaItens);

                    Log.d("MostraVistorias", "Enviando dados para DetalhesAc: idVistoria: " + anuncioSelecionado.getIdVistoria() +
                            ", localizacao: " + anuncioSelecionado.getLocalizacao() +
                            ", quantidade de itens: " + listaItens.size());
                    startActivity(i);
                } else {
                    Toast.makeText(MostraVistorias.this, "Por favor, cadastre-se para ver mais detalhes sobre a vistoria.", Toast.LENGTH_SHORT).show();
                }
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
        swipeRefreshLayout.setOnRefreshListener(this::recuperarVistoriasTelaMostrar);
    }


    // metodo para recupera vistorias
    private void recuperarVistoriasTelaMostrar() {
        Log.d("recuperarVistoriasTelaMostrar", "Iniciando o método...");
        showProgressDialog(true);

        DatabaseReference anunciosPuRef = FirebaseDatabase.getInstance().getReference("vistoriaPu");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Listadevistorias.clear();
                Log.d("MostraVistorias", "Quantidade de vistorias recuperadas: " + dataSnapshot.getChildrenCount());
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Vistoria vistoria = new Vistoria();

                    if (snapshot.hasChild("concluida")) {
                        vistoria.setConcluida(snapshot.child("concluida").getValue(Boolean.class));
                    }

                    if (snapshot.hasChild("data")) {
                        vistoria.setData(snapshot.child("data").getValue(String.class));
                    }

                    if (snapshot.hasChild("excluidaVistoria")) {
                        vistoria.setExcluidaVistoria(snapshot.child("excluidaVistoria").getValue(Boolean.class));
                    }

                    if (snapshot.hasChild("idVistoria")) {
                        vistoria.setIdVistoria(snapshot.child("idVistoria").getValue(String.class));
                    }

                    if (snapshot.hasChild("localizacao")) {
                        vistoria.setLocalizacao(snapshot.child("localizacao").getValue(String.class));

                    }


                    if (snapshot.hasChild("nomePerfilU")) {
                        vistoria.setNomePerfilU(snapshot.child("nomePerfilU").getValue(String.class));
                    }

                    DataSnapshot itensSnapshot = snapshot.child("itensMap");
                    if (itensSnapshot.exists()) {
                        Map<String, Item> itemMap = new HashMap<>();
                        for (DataSnapshot itemSnapshot : itensSnapshot.getChildren()) {
                            Item itemObj = itemSnapshot.getValue(Item.class);
                            if (itemObj != null) {
                                Log.d("MostraVistorias", "Item recuperado: " + itemObj.toString());
                                itemMap.put(itemSnapshot.getKey(), itemObj);
                            } else {
                                Log.d("MostraVistorias", "Item não foi recuperado corretamente.");
                            }
                        }
                        vistoria.setItensMap(itemMap);
                    }


                    Listadevistorias.add(vistoria);
                    Log.d("MostraVistorias", "Vistorias recuperadas: " + Listadevistorias.size());
                }
                adapterVistorias = new AdapterVistorias(Listadevistorias, MostraVistorias.this);
                recyclerViewPu.setAdapter(adapterVistorias);
                adapterVistorias.notifyDataSetChanged();
                showProgressDialog(false);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showProgressDialog(false);
                Log.d("recuperarVistoriasTelaMostrar", "Erro ao recuperar vistorias: " + databaseError.getMessage());
            }
        };

        anunciosPuRef.addListenerForSingleValueEvent(valueEventListener);
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

    private void inicializarCompo() {
        recyclerViewPu = findViewById(R.id.ryclePublico);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        nomeItem=findViewById(R.id.nomeItemA);

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

                break;
        }
        return super.onOptionsItemSelected(item);
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
