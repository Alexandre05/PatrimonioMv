package Mode;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import Helper.ConFirebase;
public class ItensVistorias implements Serializable {


  private String idAnuncio;
  private  String tipoItem;
  private String localizacao;
  private String nomeItem;
    private String placa;
    private String data;
    private int buttonColor;
    private Double latitude,longetude;
    private  String nomePerfilU;
    private boolean concluida;
    private boolean excluidaVistoria;

    public boolean isExcluidaVistoria() {
        return excluidaVistoria;
    }

    public void setExcluidaVistoria(boolean excluidaVistoria) {
        this.excluidaVistoria = excluidaVistoria;
    }
    @Exclude
    public int getButtonColor() {
        return buttonColor;
    }

    public void setButtonColor(int buttonColor) {
        this.buttonColor = buttonColor;
    }

    public boolean isConcluida() {
        return concluida;
    }

    public void setConcluida(boolean concluida) {
        this.concluida = concluida;
    }

    private String outrasInformacoes;





    public String getIdAnuncio() {
        return idAnuncio;
    }

    public void setIdAnuncio(String idAnuncio) {
        this.idAnuncio = idAnuncio;
    }



    public Double getLatitude() {
        return latitude;
    }



    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongetude() {
        return longetude;
    }

    public void setLongetude(Double longetude) {
        this.longetude = longetude;
    }

    public String getData() {
        return data;    }

    public void setData(String data) {
        this.data = data;
    }
    public String getNomePerfilU() {
        return nomePerfilU;
    }
    public void setNomePerfilU(String nomePerfilU) {
        this.nomePerfilU = nomePerfilU;
    }
  private List<String> fotos;
  public ItensVistorias() {
    DatabaseReference anuncioRefe=ConFirebase.getFirebaseDatabase().child("anuncios") ;

    setIdAnuncio(anuncioRefe.push().getKey());
  }
  public  void  salvar(){
    String idUsuario= ConFirebase.getIdUsuario();
    DatabaseReference anuncioRefe= ConFirebase.getFirebaseDatabase()
            .child("anuncios");
    anuncioRefe.child(idUsuario)
            .child(getIdAnuncio())
            .setValue(this);
  salvarAnuncioPublico();
  }

  public  void remover(){
    String idiUsuario= ConFirebase.getIdUsuario();
    DatabaseReference anuncioRefe= ConFirebase.getFirebaseDatabase()
            .child("anuncios")
            .child(idiUsuario)
            .child(getIdAnuncio());
    anuncioRefe.removeValue();
    removerAPu();
  }
    public static Task<List<ItensVistorias>> recuperarAnunciosPorCategoria(String localizacao) {
        final TaskCompletionSource<List<ItensVistorias>> taskCompletionSource = new TaskCompletionSource<>();

        DatabaseReference anunciosPuRef = FirebaseDatabase.getInstance().getReference().child("anunciosPu").child(localizacao);

        anunciosPuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ItensVistorias> anunciosList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ItensVistorias anuncio = snapshot.getValue(ItensVistorias.class);
                    anunciosList.add(anuncio);
                    Log.d("FETCH_DATA", "Anuncio encontrado: " + anuncio.getIdAnuncio());
                }
                taskCompletionSource.setResult(anunciosList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                taskCompletionSource.setException(databaseError.toException());
            }
        });

        return taskCompletionSource.getTask();
    }
    public static Task<List<ItensVistorias>> recuperarAnunciosPorPlaca(String licensePlate) {
        TaskCompletionSource<List<ItensVistorias>> taskCompletionSource = new TaskCompletionSource<>();

        DatabaseReference anunciosRef = FirebaseDatabase.getInstance().getReference("anunciosPu");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ItensVistorias> anunciosList = new ArrayList<>();
                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot anuncioSnapshot : locationSnapshot.getChildren()) {
                        ItensVistorias anuncio = anuncioSnapshot.getValue(ItensVistorias.class);
                        if (anuncio.getPlaca().equalsIgnoreCase(licensePlate)) {
                            anunciosList.add(anuncio);
                        }
                    }
                }
                taskCompletionSource.setResult(anunciosList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                taskCompletionSource.setException(databaseError.toException());
            }
        };

        anunciosRef.addListenerForSingleValueEvent(valueEventListener);

        return taskCompletionSource.getTask();
    }
    public static Task<Boolean> verificarPlacaExistente(String placa) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

        DatabaseReference anunciosRef = FirebaseDatabase.getInstance().getReference("anunciosPu");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean placaExistente = false;
                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot anuncioSnapshot : locationSnapshot.getChildren()) {
                        ItensVistorias anuncio = anuncioSnapshot.getValue(ItensVistorias.class);
                        if (anuncio.getPlaca().equalsIgnoreCase(placa)) {
                            placaExistente = true;
                            break;
                        }
                    }
                    if (placaExistente) {
                        break;
                    }
                }
                taskCompletionSource.setResult(placaExistente);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                taskCompletionSource.setException(databaseError.toException());
            }
        };

        anunciosRef.addListenerForSingleValueEvent(valueEventListener);

        return taskCompletionSource.getTask();
    }
    public static Task<Void> atualizarAnuncio(ItensVistorias anuncio, String idUsuario) {
        // Recupera a referência do nó do anúncio
        DatabaseReference anunciosRef = ConFirebase.getFirebaseDatabase().child("anuncios").child(idUsuario).child(anuncio.getIdAnuncio());

        // Atualiza os dados do anúncio no Firebase
        return anunciosRef.setValue(anuncio);
    }




    public static Task<Void> concluirVistoria(ItensVistorias vistoria, String idUsuario) {
        // Recupera a referência do nó da vistoria
        DatabaseReference vistoriasRef = ConFirebase.getFirebaseDatabase().child("anuncios").child(idUsuario).child(vistoria.getIdAnuncio());

        // Atualiza o status da vistoria para "concluída"
        vistoria.setConcluida(true);

        // Atualiza os dados da vistoria no Firebase
        return vistoriasRef.setValue(vistoria);
    }



    // DETERA OS ANUNCIOS PARA TODOS
  public  void removerAPu(){
      DatabaseReference anuncioRefe= ConFirebase.getFirebaseDatabase()
              .child("anunciosPu")
              .child(getLocalizacao())
              .child(getIdAnuncio());
    anuncioRefe.removeValue();
  }
    public void salvarVistoriaConcluida() {
        String idUsuario = ConFirebase.getIdUsuario();
        DatabaseReference vistoriaConcluidaRef = ConFirebase.getFirebaseDatabase()
                .child("vistoriasConcluidas");
        vistoriaConcluidaRef.child(idUsuario)
                .child(getIdAnuncio())
                .setValue(this);
    }

    public static Task<List<ItensVistorias>> recuperarVistoriasEmAndamento(String localizacao) {
        TaskCompletionSource<List<ItensVistorias>> taskCompletionSource = new TaskCompletionSource<>();

        DatabaseReference anunciosPuRef = FirebaseDatabase.getInstance().getReference().child("anunciosPu").child(localizacao);

        anunciosPuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ItensVistorias> anunciosList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ItensVistorias anuncio = snapshot.getValue(ItensVistorias.class);
                    if (!anuncio.isConcluida()) {
                        anunciosList.add(anuncio);
                    }
                }
                taskCompletionSource.setResult(anunciosList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                taskCompletionSource.setException(databaseError.toException());
            }
        });

        return taskCompletionSource.getTask();
    }


// SALVA ANUNCIOS PARA TODOS
  public  void  salvarAnuncioPublico(){
    DatabaseReference anuncioRefe= ConFirebase.getFirebaseDatabase()
            .child("anunciosPu");
    anuncioRefe.child(getLocalizacao())
            .child(getIdAnuncio())
            .setValue(this);
  }

  public String getTipoItem()
    {
        return tipoItem;
  }

    public void setTipoItem(String tipoItem) {
        this.tipoItem = tipoItem;
    }
    public String getLocalizacao() {
        return localizacao;    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao.toUpperCase();
    }
    public String getNomeItem() {
        return nomeItem;
    }
    public void setNomeItem(String nomeItem) {
        this.nomeItem = nomeItem;
    }
    public String getOutrasInformacoes() {
        return outrasInformacoes;
    }

    public void setOutrasInformacoes(String outrasInformacoes) {
        this.outrasInformacoes = outrasInformacoes;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public List<String> getFotos() {
        return fotos;
    }







  public void setFotos(List<String> fotos) {
    this.fotos = fotos;
  }
}
