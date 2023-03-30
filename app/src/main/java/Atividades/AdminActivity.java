package Atividades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media2.exoplayer.external.util.Log;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.ArrayList;
import java.util.List;

import Adapter.PendingUserAdapter;
import Adapter.VistoriaAdapter;
import Helper.ConFirebase;
import Helper.QRCodeGenerator;
import Mode.ItensVistorias;
import Mode.Usuario;
import br.com.patrimoniomv.R;

public class AdminActivity extends AppCompatActivity {
    private ListView listViewVistorias;
    private List<ItensVistorias> vistorias;
    private VistoriaAdapter vistoriaAdapter;
    private EditText novoCodigoEspecial;
    private Button botaoAlterarCodigoEspecial;
    private DatabaseReference firebaseRef;
    private Button editCompanyInfoButton;
    private EditText etNumeroPortarias;
    private EditText etVistoriadoresPorPortaria;
    private Button btnSalvarConfiguracoes;
    private List<String> matriculasMembrosComissao = new ArrayList<>();
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private static final int LIMITE_VISTORIADORES = 5;
    private RecyclerView recyclerViewPendingUsers;
    private PendingUserAdapter pendingUserAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        listViewVistorias = findViewById(R.id.listView_vistorias);
        novoCodigoEspecial = findViewById(R.id.codigoEspecialEditText);
        etNumeroPortarias = findViewById(R.id.et_numero_portarias);
        etVistoriadoresPorPortaria = findViewById(R.id.et_vistoriadores_por_portaria);
        btnSalvarConfiguracoes = findViewById(R.id.btn_salvar_configuracoes);

        botaoAlterarCodigoEspecial = findViewById(R.id.updateCodigoEspecialButton);
        vistorias = new ArrayList<>();
        vistoriaAdapter = new VistoriaAdapter(this, R.layout.itensvistoria, vistorias);
        listViewVistorias.setAdapter(vistoriaAdapter);
        editCompanyInfoButton = findViewById(R.id.edit_company_info_button);

        //buscarVistorias();
        fetchPendingRequests();

        botaoAlterarCodigoEspecial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String novoCodigo = novoCodigoEspecial.getText().toString();
                if (!novoCodigo.isEmpty()) {
                    ConFirebase.CODIGO_ESPECIAL = novoCodigo; // altera o valor da variável estática
                    novoCodigoEspecial.setText("");
                    Toast.makeText(AdminActivity.this, "Código especial atualizado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminActivity.this, "Por favor, insira um novo código especial", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(AdminActivity.this, "Configurações salvas", Toast.LENGTH_SHORT);
                } else {
                    Toast.makeText(AdminActivity.this, "Por favor, insira o número de portarias e vistoriadores por portaria", Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(AdminActivity.this, CompanyInfoActivity.class);
        startActivity(intent);
    }
    private void salvarConfiguracoes(int numeroPortarias, int vistoriadoresPorPortaria) {
        DatabaseReference configuracoesRef = firebaseDatabase.getReference("configuracoes");
        configuracoesRef.child("numeroPortarias").setValue(numeroPortarias);
        configuracoesRef.child("vistoriadoresPorPortaria").setValue(vistoriadoresPorPortaria);
    }

    private void verificarVistoriaConcluida(String idVistoria, String localizacao) {
        DatabaseReference vistoriaRef = firebaseDatabase.getReference("vistorias").child(idVistoria);
        vistoriaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ItensVistorias vistoria = dataSnapshot.getValue(ItensVistorias.class);
                if (vistoria != null && vistoria.isConcluida()) {
                    String textoQRCode = "Localização: " + vistoria.getLocalizacao() + ", Vistoriador: " + vistoria.getNomePerfilU() + ", Data: " + vistoria.getData();
                    Bitmap bitmap = null;
                    try {
                        bitmap = QRCodeGenerator.generateQRCode(textoQRCode, 500, 500);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    // faça algo com o código QR gerado, como exibi-lo na tela ou salvá-lo em um arquivo
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdminActivity.this, "Erro ao buscar vistoria: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void exibirNotificacao(String localizacao) {
        String mensagem = "A vistoria da localização " + localizacao + " foi concluída.";
        Toast.makeText(AdminActivity.this, mensagem, Toast.LENGTH_SHORT).show();
    }
    public boolean isMatriculaValida(String matricula) {
        return matriculasMembrosComissao.contains(matricula);
    }

    public void adicionarMembroComissao(String matricula, String comissaoId) {
        if (!isMatriculaValida(matricula) && matriculasMembrosComissao.size() < LIMITE_VISTORIADORES) {
            matriculasMembrosComissao.add(matricula);
            updateMembroComissao(matricula, true, comissaoId);
        } else {
            // Avisar o administrador que o limite foi atingido ou que a matrícula já faz parte da comissão
        }
    }

    public void removerMembroComissao(String matricula) {
        if (isMatriculaValida(matricula)) {
            matriculasMembrosComissao.remove(matricula);
            updateMembroComissao(matricula, false, null);
        }
    }
    private void fetchPendingRequests() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("usuarios");
        usersRef.orderByChild("status").equalTo("pendente").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Usuario usuarioPendente = snapshot.getValue(Usuario.class);
                    // Aqui você pode processar cada usuário pendente
                    // Por exemplo, adicione-os a uma lista e atualize a interface do usuário
                }
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
    private void updateMembroComissao(String matricula, boolean isMembro, String comissaoId) {
        DatabaseReference usuariosRef = firebaseDatabase.getReference("usuarios");
        usuariosRef.orderByChild("matricula").equalTo(matricula).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        Usuario usuario = userSnapshot.getValue(Usuario.class);
                        if (usuario != null) {
                            usuario.setMembroComissao(isMembro);
                            usuario.setComissaoId(comissaoId);
                            usuario.atualizar();
                        }
                    }
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