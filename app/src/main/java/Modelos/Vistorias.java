package Modelos;

import androidx.annotation.NonNull;


import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Adaptadores.AdapterVistorias;
import Ajuda.ConFirebase;

public class Vistorias implements Serializable {
    private List<Item> itens;
    private Map<String, Object> itensMap;

    public Map<String, Object> getItensMap() {
        return itensMap;
    }

    public void setItensMap(Map<String, Object> itensMap) {
        this.itensMap = itensMap;
    }

    public List<Item> getItens() {
        return itens;
    }

    public void setItens(List<Item> itens) {
        this.itens = itens;
    }

    private int hour, minute, second;

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }



    private String idVistoria;


    private String localizacao_data;

    public String getLocalizacao_data() {
        return localizacao_data;
    }

    public void setLocalizacao_data(String localizacao_data) {
        this.localizacao_data = localizacao_data;
    }

    public void setExcluidaVistoria(boolean excluidaVistoria) {
        this.excluidaVistoria = excluidaVistoria;
    }

    public String getIdVistoria() {
        return idVistoria;
    }

    public void setIdVistoria(String idVistoria) {
        this.idVistoria = idVistoria;
    }

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


    public boolean isConcluida() {
        return concluida;
    }

    public void setConcluida(boolean concluida) {
        this.concluida = concluida;
    }









    public String getData() {
        return data;
    }

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

    public Vistorias() {
        DatabaseReference anuncioRefe = ConFirebase.getFirebaseDatabase().child("vistorias");

        setIdVistoria(anuncioRefe.push().getKey());
        itens = new ArrayList<>();
    }

    public static Task<List<Vistorias>> buscarVistorias() {
        TaskCompletionSource<List<Vistorias>> taskCompletionSource = new TaskCompletionSource<>();

        DatabaseReference vistoriasRef = ConFirebase.getFirebaseDatabase().child("vistoriaPu");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Vistorias> vistoriasList = new ArrayList<>();
                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot vistoriaSnapshot : locationSnapshot.getChildren()) {
                        Vistorias vistoria = vistoriaSnapshot.getValue(Vistorias.class);
                        vistoriasList.add(vistoria);
                    }
                }
                taskCompletionSource.setResult(vistoriasList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                taskCompletionSource.setException(databaseError.toException());
            }
        };

        vistoriasRef.addListenerForSingleValueEvent(valueEventListener);

        return taskCompletionSource.getTask();
    }

    public String getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(String tipoItem) {
        this.tipoItem = tipoItem;
    }

    public String getLocalizacao() {
        return localizacao;
    }

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

    // metodos
    public void salvar() {
        String idUsuario = ConFirebase.getIdUsuario();
        setIdInspector(idUsuario);
        DatabaseReference anuncioRefe = ConFirebase.getFirebaseDatabase()
                .child("vistorias");
        anuncioRefe.child(idUsuario)
                .child(getLocalizacao())
                .child(getNomePerfilU())
                .child(getIdVistoria())

                .setValue(this);
        salvarAnuncioPublico();
    }

    public void remover() {
        String idiUsuario = ConFirebase.getIdUsuario();

        DatabaseReference anuncioRefe = ConFirebase.getFirebaseDatabase()
                .child("vistorias")
                .child("vistoriaPu")
                .child(idiUsuario)
                .child(getIdVistoria());
        anuncioRefe.removeValue();
        removerAPu();
    }


    public static Task<Boolean> verificarPlacaExistente(String placa) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

        DatabaseReference anunciosRef = FirebaseDatabase.getInstance().getReference("vistoriaPu");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean placaExistente = false;
                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot anuncioSnapshot : locationSnapshot.getChildren()) {
                        Vistorias anuncio = anuncioSnapshot.getValue(Vistorias.class);
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

    public static Task<Void> atualizarAnuncio(Vistorias anuncio, String idUsuario) {
        // Recupera a referência do nó do anúncio
        DatabaseReference anunciosRef = ConFirebase.getFirebaseDatabase().child("vistorias").child(idUsuario).child(anuncio.getIdVistoria());

        // Atualiza os dados do anúncio no Firebase
        return anunciosRef.setValue(anuncio);
    }

    public static Task<Void> atualizarAnuncioPu(Vistorias anuncio, String idUsuario) {
        DatabaseReference anuncioPuRef = ConFirebase.getFirebaseDatabase()
                .child("vistoriaPu")
                .child(idUsuario)
                .child(anuncio.getIdVistoria());

        return anuncioPuRef.setValue(anuncio);
    }

    // DETERA OS ANUNCIOS PARA TODOS
    public void removerAPu() {
        DatabaseReference anuncioRefe = ConFirebase.getFirebaseDatabase()
                .child("vistoriaPu")
                .child(getLocalizacao())
                .child(getIdVistoria());
        anuncioRefe.removeValue();
    }

    // SALVA ANUNCIOS PARA TODOS
    public void salvarAnuncioPublico() {
        DatabaseReference anuncioRefe = ConFirebase.getFirebaseDatabase()
                .child("vistoriaPu");
        anuncioRefe.child(getLocalizacao())
                .child(getIdVistoria())
                .child(getNomePerfilU())
                .setValue(this);
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("idVistoria", idVistoria);
        result.put("localizacao_data", localizacao_data);
        result.put("qrCodeURL", qrCodeURL);
        result.put("tipoItem", tipoItem);
        result.put("localizacao", localizacao);
        result.put("nomeItem", nomeItem);
        result.put("placa", placa);
        result.put("data", data);
        result.put("latitude", latitude);
        result.put("longetude", longetude);
        result.put("outrasInformacoes", outrasInformacoes);
        result.put("nomePerfilU", nomePerfilU);
        result.put("concluida", concluida);
        result.put("idInspector", idInspector);
        result.put("excluidaVistoria", excluidaVistoria);
        result.put("fotos", fotos);
        result.put("itens", itens);
        result.put("itens", itensMap);
        return result;
    }
}