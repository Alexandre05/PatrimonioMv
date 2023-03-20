package Atividades;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import Adapter.AdapterAnuncios;
import Helper.ConFirebase;
import Mode.ItensVistorias;
import Mode.RecyclerItemClickListener;

import br.com.patrimoniomv.R;
import dmax.dialog.SpotsDialog;

public class Animais extends AppCompatActivity {
    private FirebaseAuth autenticacao;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_animais);
        //Toolbar tolbar= findViewById(R.id.Toolbar);
        this.setTitle("Comissão  de Patrimonio ");
        String idAnuncio = getIntent().getStringExtra("idAnuncio");

        navegation();
        inicializarCompo();
        autenticacao = ConFirebase.getReferenciaAutencicacao();
        anunciosRef = ConFirebase.getFirebaseDatabase().child("anuncios");
        //autenticacao.signOut();
        recyclerViewPu.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPu.setHasFixedSize(true);
        adapterAnuncios = new AdapterAnuncios(listacioItens, this);
        recyclerViewPu.setAdapter(adapterAnuncios);

        recuperaAnuncioPublicos();
// aqui faz o tique na tela para abrir
        recyclerViewPu.addOnItemTouchListener(new RecyclerItemClickListener(
                this, recyclerViewPu,
                new RecyclerItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        Log.e("DETAILED_ACTIVITY", "Onclik.");
                        ItensVistorias anuncioSelecionado = listacioItens.get(position);

                        Intent i = new Intent(Animais.this, DetalhesAc.class);
                        i.putExtra("anuncioSelecionado", anuncioSelecionado);

                        i.putExtra("localizacao", "sua_localizacao_aqui"); // substitua por sua localização real
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

    @Override
    protected void onStart() {
        super.onStart();
        anunciosRef = ConFirebase.getFirebaseDatabase().child("anuncios").child("localizacao");
    }

    private void navegation() {
        BottomNavigationView bottom = findViewById(R.id.bnve);
        caregarNavegação(bottom);
        Menu menu = bottom.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);


    }

    private void caregarNavegação(BottomNavigationView view) {
        view.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {

                    case R.id.ic_minhas:
                        startActivity(new Intent(getApplicationContext(), MinhasVistorias.class));
                        break;

                    case R.id.ic_perfil:
                        startActivity(new Intent(getApplicationContext(), Perfil.class));

                        break;
                    case R.id.inicial:
                        recreate();
                        break;

                }
                return true;
            }
        });

    }
// não esta sendo usado
    public void filtrarLocalizacao(View view) {
        if (filtrandoPorLocalizacao == true) {
            AlertDialog.Builder dialoAnimalRece = new AlertDialog.Builder(this);
            dialoAnimalRece.setTitle("Filtre Por Localização!!");
            View viwSpi = getLayoutInflater().inflate(R.layout.dialo_spiner, null);
            final Spinner spinnerBairro = viwSpi.findViewById(R.id.spinnerFiltro);
            String[] animais = getResources().getStringArray(R.array.Localização);
            ArrayAdapter<String> Adapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_spinner_item, animais
            );

            spinnerBairro.setAdapter(Adapter);
            String[] bairro = getResources().getStringArray(R.array.Localização);
            ArrayAdapter<String> Adapter2 = new ArrayAdapter<String>(
                    this, android.R.layout.simple_spinner_item, bairro
            );


            dialoAnimalRece.setView(viwSpi);


            dialoAnimalRece.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    filtroBairro = spinnerBairro.getSelectedItem().toString();
                    recuperaAnuncioPorBairro();


                }
            });

            dialoAnimalRece.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog dialog = dialoAnimalRece.create();
            dialog.show();


        } else {
            Toast.makeText(this, "Escolha um item",
                    Toast.LENGTH_SHORT).show();


        }


    }

    public void filtrarPorItem(View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Pesquise Por Placa ou Localização");

        final EditText input = new EditText(Animais.this);

        LinearLayout inputLayout = new LinearLayout(this);
        inputLayout.setOrientation(LinearLayout.VERTICAL);

        inputLayout.addView(input); // exibe a barra de entrada do usuário
        dialogBuilder.setView(inputLayout);

        dialogBuilder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String filterInput = input.getText().toString().toUpperCase();

                // Validação da entrada do usuário
                if (filterInput.isEmpty()) {
                    Toast.makeText(Animais.this, "Por favor, insira alguma informação válida", Toast.LENGTH_SHORT).show();
                    return;
                }

                filtroItens = filterInput;
                recuperaItens(filtroItens);
            }
        });

        dialogBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Não faz nada
            }
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    public void recuperaItens(String texto) {

        if (texto == null || texto.trim().equals("")) {
            exibirMensagemErro("Digite Sua busca");



    }else {
            alertDialog = new SpotsDialog.Builder(this)

                    .setMessage("Recuperando  Itens").setCancelable(false)
                    .show();



            Query query = anunciosRef.orderByChild("Localizacao")
                    .equalTo(texto+ "\uf8ff");

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    listacioItens.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                            ItensVistorias anuncio = ds.getValue(ItensVistorias.class);


                           listacioItens.add(anuncio);





                    }


                    int total = listacioItens.size();
                    exibirMensagemErro("total" + total);

                    Collections.reverse(listacioItens);
                    adapterAnuncios.notifyDataSetChanged();

                    alertDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }


}




    public void recuperaAnuncioPorBairro() {
        alertDialog = new SpotsDialog.Builder(this)

                .setMessage("Recuperando  Bairro")
                .setCancelable(false)


                .show();


        alertDialog.show();

        anunciosRef = ConFirebase.getFirebaseDatabase()
                .child("anunciosPu")
                .child(filtroItens)
                .child(filtroBairro);
        anunciosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listacioItens.clear();

                for (DataSnapshot localizacao : snapshot.getChildren()) {
                    for (DataSnapshot lo : localizacao.getChildren()) {
                        ItensVistorias anuncio = lo.getValue(ItensVistorias.class);
                        listacioItens.add(anuncio);
                    }
                }

                Collections.reverse(listacioItens);
                adapterAnuncios.notifyDataSetChanged();
                alertDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Lida com erros ao recuperar os dados
            }
        });

    }
    private void exibirMensagemErro(String mensagem) {
        Toast.makeText(this,
                mensagem, Toast.LENGTH_SHORT).show();


    }

    public void recuperaAnuncioPublicos() {
        alertDialog = new SpotsDialog.Builder(this)

                .setMessage("Recuperando  Vistorias")
                .setCancelable(false)
                .show();
        alertDialog.show();
        anunciosRef = ConFirebase.getFirebaseDatabase()
                .child("anunciosPu");
        anunciosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listacioItens.clear();
                for (DataSnapshot localizacao : snapshot.getChildren()) {
                    for (DataSnapshot lo : localizacao.getChildren()) {
                            ItensVistorias anuncio = lo.getValue(ItensVistorias.class);
                            listacioItens.add(anuncio);
                    }
                }
                Collections.reverse(listacioItens);
                adapterAnuncios.notifyDataSetChanged();
                alertDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (autenticacao.getCurrentUser() == null) {
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
                  startActivity(new Intent(getApplicationContext(),Login.class));

                break;
            case R.id.menu_relatorio:
                startActivity(new Intent(getApplicationContext(),Relatorios.class));

                break;
            case R.id.menu_perfil:
                startActivity(new Intent(getApplicationContext(),Perfil.class));

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

    private void Abrir() {
        startActivity(new Intent(this, Login.class));
    }


    private void inicializarCompo() {

        recyclerViewPu = findViewById(R.id.ryclePublico);
        campoEmail = findViewById(R.id.emialLog);
        campoSenha = findViewById(R.id.senhaLog);

    }
}
