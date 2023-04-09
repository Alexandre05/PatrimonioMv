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
import com.google.firebase.database.PropertyName;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import Helper.ConFirebase;
public class ItensVistorias implements Serializable {
    @PropertyName("idAnuncio")
    private String idAnuncio;

    @PropertyName("qrCodeURL")
    private String qrCodeURL;

    @PropertyName("tipoItem")
    private String tipoItem;

    @PropertyName("localizacao")
    private String localizacao;

    @PropertyName("nomeItem")
    private String nomeItem;

    @PropertyName("placa")
    private String placa;

    @PropertyName("data")
    private String data;

    @PropertyName("latitude")
    private Double latitude;

    @PropertyName("longetude")
    private Double longetude;
    @PropertyName("outrasInformacoes")
    private String outrasInformacoes;
    @PropertyName("nomePerfilU")
    private String nomePerfilU;

    @PropertyName("concluida")
    private boolean concluida;

    @PropertyName("idInspector")
    private String idInspector;

    @PropertyName("excluidaVistoria")
    private boolean excluidaVistoria;


    public String getQrCodeURL() {
        return qrCodeURL;
    }
    @PropertyName("qrCodeURL")
    public void setQrCodeURL(String qrCodeURL) {
        this.qrCodeURL = qrCodeURL;
    }



    public String getIdInspector() {
        return idInspector;
    }

    public void setIdInspector(String idInspector) {
        this.idInspector = idInspector;
    }



    public boolean isExcluidaVistoria() {
        return excluidaVistoria;
    }

    public void setExcluidaVistoria(boolean excluidaVistoria) {
        this.excluidaVistoria = excluidaVistoria;
    }
    public String getFormattedData() {
        String formattedData = "Nome do item: " + nomeItem +
                "\nPlaca: " + placa +
                "\nLocalização: " + localizacao +
                "\nData: " + data +
                "\nVistoriador: " + nomePerfilU +
                "\nObservações: " + outrasInformacoes;

        return formattedData;
    }


    public boolean isConcluida() {
        return concluida;
    }

    public void setConcluida(boolean concluida) {
        this.concluida = concluida;
    }


    @PropertyName("idAnuncio")
    public String getIdAnuncio() {
        return idAnuncio;
    }
    @PropertyName("idAnuncio")
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
    @PropertyName("fotos")
  private List<String> fotos;
  public ItensVistorias() {
    DatabaseReference anuncioRefe=ConFirebase.getFirebaseDatabase().child("anuncios") ;

    setIdAnuncio(anuncioRefe.push().getKey());
  }
  public  void  salvar(){
    String idUsuario= ConFirebase.getIdUsuario();
      setIdInspector(idUsuario);
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
    public static Task<List<ItensVistorias>> buscarVistoriasConcluidasPorDataELocalizacao(String localizacao, String dataInicial, String dataFinal) {
        TaskCompletionSource<List<ItensVistorias>> taskCompletionSource = new TaskCompletionSource<>();

        DatabaseReference vistoriasConcluidasRef = FirebaseDatabase.getInstance().getReference("vistoriasConcluidas");
        Query query = vistoriasConcluidasRef.child(localizacao).orderByChild("data").startAt(dataInicial).endAt(dataFinal);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ItensVistorias> vistoriasList = new ArrayList<>();
                for (DataSnapshot vistoriaSnapshot : dataSnapshot.getChildren()) {
                    ItensVistorias vistoria = vistoriaSnapshot.getValue(ItensVistorias.class);
                    if (vistoria.isConcluida()) {
                        vistoriasList.add(vistoria);
                    }
                }
                taskCompletionSource.setResult(vistoriasList);
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

    public static Task<Void> atualizarAnuncioPu(ItensVistorias anuncio, String idUsuario) {
        DatabaseReference anuncioPuRef = ConFirebase.getFirebaseDatabase()
                .child("anunciosPu")
                .child(idUsuario)
                .child(anuncio.getIdAnuncio());

        return anuncioPuRef.setValue(anuncio);
    }

    // DETERA OS ANUNCIOS PARA TODOS
  public  void removerAPu(){
      DatabaseReference anuncioRefe= ConFirebase.getFirebaseDatabase()
              .child("anunciosPu")
              .child(getLocalizacao())
              .child(getIdAnuncio());
    anuncioRefe.removeValue();
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
    @PropertyName("outrasInformacoes")
    public String getOutrasInformacoes() {
        return outrasInformacoes;
    }
    @PropertyName("outrasInformacoes")
    public void setOutrasInformacoes(String outrasInformacoes) {
        this.outrasInformacoes = outrasInformacoes;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }
    @PropertyName("fotos")
    public List<String> getFotos() {
        return fotos;
    }
    @PropertyName("fotos")
  public void setFotos(List<String> fotos) {
    this.fotos = fotos;
  }
}