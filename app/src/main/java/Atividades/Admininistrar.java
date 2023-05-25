package Atividades;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Adaptadores.PendingUserAdapter;
import Adaptadores.UsuarioAdapter;
import Adaptadores.VistoriaAdapter;
import Ajuda.ConFirebase;
import Modelos.Comissao;
import Modelos.Usuario;
import Modelos.Vistoria;

import br.com.patrimoniomv.R;

public class Admininistrar extends AppCompatActivity {
    private ListView listViewVistorias;
    private List<Vistoria> vistorias;
    private VistoriaAdapter vistoriaAdapter;
    private EditText novoCodigoEspecial;
    private Button botaoAlterarCodigoEspecial;
    private DatabaseReference firebaseRef;
    private Button editCompanyInfoButton;
    private EditText novaLocalizacaoEditText;
    private Button adicionarLocalizacaoButton;
    private EditText etNumeroPortarias;
    private Comissao comissaoAtual;

    private EditText etVistoriadoresPorPortaria;
    private Button btnSalvarConfiguracoes, botaoAdicionar;
    private List<String> matriculasMembrosComissao = new ArrayList<>();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private static final int LIMITE_VISTORIADORES = 5;

    private PendingUserAdapter pendingUserAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        listViewVistorias = findViewById(R.id.list_view_usuarios);
        novoCodigoEspecial = findViewById(R.id.codigoEspecialEditText);
        etNumeroPortarias = findViewById(R.id.et_numero_portarias);
        etVistoriadoresPorPortaria = findViewById(R.id.et_vistoriadores_por_portaria);
        btnSalvarConfiguracoes = findViewById(R.id.btn_salvar_configuracoes);
        adicionarLocalizacaoButton = findViewById(R.id.adicionarLocalizacaoButton);
        novaLocalizacaoEditText = findViewById(R.id.novaLocalizacaoEditText);
        botaoAlterarCodigoEspecial = findViewById(R.id.updateCodigoEspecialButton);
        vistorias = new ArrayList<>();
        vistoriaAdapter = new VistoriaAdapter(this, R.layout.itensvistoria, vistorias);
        listViewVistorias.setAdapter(vistoriaAdapter);
        editCompanyInfoButton = findViewById(R.id.edit_company_info_button);
        //Toast.makeText(this, "Atividade criada!", Toast.LENGTH_SHORT).show();

        //buscarVistorias();
        fetchPendingRequests();
        fetchAllUsers();
        botaoAlterarCodigoEspecial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String novoCodigo = novoCodigoEspecial.getText().toString();
                if (!novoCodigo.isEmpty()) {
                    ConFirebase.CODIGO_ESPECIAL = novoCodigo; // altera o valor da variável estática
                    novoCodigoEspecial.setText("");
                    Toast.makeText(Admininistrar.this, "Código especial atualizado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Admininistrar.this, "Por favor, insira um novo código especial", Toast.LENGTH_SHORT).show();
                }
            }
        });

        novaLocalizacaoEditText = findViewById(R.id.novaLocalizacaoEditText);
        Button adicionarLocalizacaoButton = findViewById(R.id.adicionarLocalizacaoButton);

        adicionarLocalizacaoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String novaLocalizacao = novaLocalizacaoEditText.getText().toString();
                if (!novaLocalizacao.isEmpty()) {
                    adicionarLocalizacao(novaLocalizacao);
                } else {
                    Toast.makeText(Admininistrar.this, "Por favor, insira uma localização", Toast.LENGTH_SHORT).show();
                }
            }
        });


        btnSalvarConfiguracoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numeroPortarias = etNumeroPortarias.getText().toString();
                String vistoriadoresPorPortaria = etVistoriadoresPorPortaria.getText().toString();

                if (!numeroPortarias.isEmpty() && !vistoriadoresPorPortaria.isEmpty()) {
                    salvarConfiguracoes(Integer.parseInt(numeroPortarias), Integer.parseInt(vistoriadoresPorPortaria));
                    Toast.makeText(Admininistrar.this, "Configurações salvas", Toast.LENGTH_SHORT);
                } else {
                    Toast.makeText(Admininistrar.this, "Por favor, insira o número de portarias e vistoriadores por portaria", Toast.LENGTH_SHORT).show();
                }
            }
        });
        editCompanyInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditCompanyInfoActivity();
            }
        });

    }
    private void openEditCompanyInfoActivity() {
        Intent intent = new Intent(Admininistrar.this, InformacoesEmpresa.class);
        startActivity(intent);
    }
    private void salvarConfiguracoes(int numeroPortarias, int vistoriadoresPorPortaria) {
        DatabaseReference configuracoesRef = firebaseDatabase.getReference("configuracoes");
        configuracoesRef.child("numeroPortarias").setValue(numeroPortarias);
        configuracoesRef.child("vistoriadoresPorPortaria").setValue(vistoriadoresPorPortaria);
    }

    private void fetchAllUsers() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("usuarios");
        usersRef.orderByChild("status").equalTo("pendente").addValueEventListener(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Usuario> usuarios = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    usuario.setIdU(snapshot.getKey());  // Adicione esta linha
                    if (!"AD".equals(usuario.getTipo())) {
                        usuarios.add(usuario);
                    }
                }
                UsuarioAdapter usuarioAdapter = new UsuarioAdapter(Admininistrar.this, R.layout.item_usuario, usuarios);
                listViewVistorias.setAdapter(usuarioAdapter);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log.w("AdminActivity", "Erro ao carregar usuários", databaseError.toException());
            }
        });
    }

    public void adicionarLocalizacao(String localizacao) {
        DatabaseReference locRef = FirebaseDatabase.getInstance().getReference("localizacoes");
        String id = locRef.push().getKey();
        locRef.child(id).setValue(localizacao).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Admininistrar.this, "Localização adicionada com sucesso", Toast.LENGTH_SHORT).show();
                    novaLocalizacaoEditText.setText("");  // Limpar o EditText após adicionar a localização
                } else {
                    Toast.makeText(Admininistrar.this, "Erro ao adicionar localização", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }





private void exibirNotificacao(String localizacao) {
        String mensagem = "A vistoria da localização " + localizacao + " foi concluída.";
        Toast.makeText(Admininistrar.this, mensagem, Toast.LENGTH_SHORT).show();
    }
    public boolean isMatriculaValida(String matricula) {
        return matriculasMembrosComissao.contains(matricula);
    }

    public void adicionarMembroComissao(String matricula) {
        if (!comissaoAtual.getMembros().contains(matricula) && comissaoAtual.getMembros().size() < LIMITE_VISTORIADORES) {
            comissaoAtual.getMembros().add(matricula);
            updateMembroComissao(matricula, true);
        } else {
            // Avisar o administrador que o limite foi atingido ou que a matrícula já faz parte da comissão
        }
    }

    public void removerMembroComissao(String matricula) {
        if (comissaoAtual.getMembros().contains(matricula)) {
            comissaoAtual.getMembros().remove(matricula);
            updateMembroComissao(matricula, false);
        }
    }
    private void fetchPendingRequests() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("usuarios");
        usersRef.orderByChild("status").equalTo("pendente").addValueEventListener(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Usuario> usuariosPendentes = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Usuario usuarioPendente = snapshot.getValue(Usuario.class);
                    usuarioPendente.setIdU(snapshot.getKey());  // Adicione esta linha
                    usuariosPendentes.add(usuarioPendente);
                }
                // Aqui você pode atualizar sua interface do usuário com a lista de usuários pendentes
                // Por exemplo, você pode usar um Adapter para preencher uma ListView ou RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log.w("AdminActivity", "Erro ao carregar solicitações pendentes", databaseError.toException());
            }
        });
    }

    private void approveRequest(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId);
        userRef.child("status").setValue("aprovado");
    }

    private void rejectRequest(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId);
        userRef.child("status").setValue("rejeitado");
    }



    // Método para verificar se o usuário já faz parte de uma comissão
    public boolean isUsuarioMembroComissao(String matricula) {
        return isMatriculaValida(matricula);
    }
    private void updateMembroComissao(String matricula, boolean isMembro) {
        DatabaseReference usuariosRef = firebaseDatabase.getReference("usuarios");
        usuariosRef.orderByChild("matricula").equalTo(matricula).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        Usuario usuario = userSnapshot.getValue(Usuario.class);
                        if (usuario != null) {
                            usuario.setMembroComissao(isMembro);
                            usuario.setComissaoId(isMembro ? comissaoAtual.getId() : null);
                            usuario.atualizar();
                        }
                    }
                    // Atualize a comissão no banco de dados
                    DatabaseReference comissaoRef = firebaseDatabase.getReference("comissoes").child(comissaoAtual.getId());
                    comissaoRef.setValue(comissaoAtual);
                } else {
                    // Avisar que a matrícula não foi encontrada
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Tratar o erro
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
       // buscarVistorias();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}