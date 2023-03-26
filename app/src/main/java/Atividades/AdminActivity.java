package Atividades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import Adapter.VistoriaAdapter;
import Helper.QRCodeGenerator;
import Mode.ItensVistorias;
import br.com.patrimoniomv.R;

public class AdminActivity extends AppCompatActivity {
    private ListView listViewVistorias;
    private List<ItensVistorias> vistorias;
    private VistoriaAdapter vistoriaAdapter;
    private EditText novoCodigoEspecial;
    private Button botaoAlterarCodigoEspecial;
    private DatabaseReference firebaseRef;
    private Button editCompanyInfoButton;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        listViewVistorias = findViewById(R.id.listView_vistorias);
        novoCodigoEspecial = findViewById(R.id.codigoEspecialEditText);
        botaoAlterarCodigoEspecial = findViewById(R.id.updateCodigoEspecialButton);
        vistorias = new ArrayList<>();
        vistoriaAdapter = new VistoriaAdapter(this, R.layout.itensvistoria, vistorias);
        listViewVistorias.setAdapter(vistoriaAdapter);
        editCompanyInfoButton = findViewById(R.id.edit_company_info_button);
        buscarVistorias();
        botaoAlterarCodigoEspecial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String novoCodigo = novoCodigoEspecial.getText().toString();
                if (!novoCodigo.isEmpty()) {
                    firebaseRef.child("codigoEspecial").setValue(novoCodigo);
                    Toast.makeText(AdminActivity.this, "Código especial atualizado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminActivity.this, "Por favor, insira um novo código especial", Toast.LENGTH_SHORT).show();
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
    private void buscarVistorias() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("vistorias");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                vistorias.clear();
                for (DataSnapshot vistoriaSnapshot : dataSnapshot.getChildren()) {
                    ItensVistorias vistoria = vistoriaSnapshot.getValue(ItensVistorias.class);
                    vistorias.add(vistoria);
                    if (vistoria.isConcluida()) {
                        verificarVistoriaConcluida(vistoria.getIdAnuncio(), vistoria.getLocalizacao());
                    }
                }
                vistoriaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdminActivity.this, "Erro ao buscar vistorias: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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




    }