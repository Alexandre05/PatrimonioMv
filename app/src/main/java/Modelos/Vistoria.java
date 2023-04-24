package Modelos;

import android.util.Log;

import androidx.annotation.NonNull;


import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.PropertyName;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Ajuda.ConFirebase;

public class Vistoria implements Serializable {
    private String idUsuario;

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Boolean getConcluida() {
        return concluida;
    }

    public void setConcluida(Boolean concluida) {
        this.concluida = concluida;
    }

    public Boolean getExcluidaVistoria() {
        return excluidaVistoria;
    }

    public void setExcluidaVistoria(Boolean excluidaVistoria) {
        this.excluidaVistoria = excluidaVistoria;
    }

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


    @PropertyName("localizacao")
    private String localizacao;


    @PropertyName("data")
    private String data;
    @PropertyName("nomePerfilU")
    private String nomePerfilU;

    @PropertyName("concluida")
    private Boolean concluida;

    @PropertyName("idInspector")
    private String idInspector;

    @PropertyName("excluidaVistoria")
    private Boolean excluidaVistoria;


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

    public Vistoria() {
        DatabaseReference anuncioRefe = ConFirebase.getFirebaseDatabase().child("vistorias");

        setIdVistoria(anuncioRefe.push().getKey());
        itens = new ArrayList<>();
    }


    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
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
        Log.d("id usuario","Teste"+idUsuario);
        setIdInspector(idUsuario);
        setIdUsuario(idUsuario); // Adicione esta linha
        DatabaseReference anuncioRefe = ConFirebase.getFirebaseDatabase().child("vistorias");
        DatabaseReference vistoriaRef = anuncioRefe.child(idUsuario).child(getLocalizacao()).child(getNomePerfilU()).child(getIdVistoria());
        vistoriaRef.setValue(this);
        vistoriaRef.child("itens").setValue(getItens());
          vistoriaRef.child("idUsuario").setValue(getIdUsuario());
        salvarAnuncioPublico();
    }


    public void remover() {
        String idiUsuario = ConFirebase.getIdUsuario();

        DatabaseReference anuncioRefe = ConFirebase.getFirebaseDatabase().child("vistorias").child("vistoriaPu").child(idiUsuario).child(getIdVistoria());
        anuncioRefe.removeValue();
        removerAPu();
    }


    public static Task<Void> atualizarAnuncio(Vistoria anuncio, String idUsuario) {
        // Recupera a referência do nó do anúncio
        DatabaseReference anunciosRef = ConFirebase.getFirebaseDatabase().child("vistorias").child(idUsuario).child(anuncio.getIdVistoria());

        // Atualiza os dados do anúncio no Firebase
        return anunciosRef.setValue(anuncio);
    }

    public static Task<Void> atualizarAnuncioPu(Vistoria anuncio, String idUsuario) {
        DatabaseReference anuncioPuRef = ConFirebase.getFirebaseDatabase().child("vistoriaPu").child(idUsuario).child(anuncio.getIdVistoria());

        return anuncioPuRef.setValue(anuncio);
    }

    // DETERA OS ANUNCIOS PARA TODOS
    public void removerAPu() {
        DatabaseReference anuncioRefe = ConFirebase.getFirebaseDatabase().child("vistoriaPu").child(getLocalizacao()).child(getIdVistoria());
        anuncioRefe.removeValue();
    }

    // SALVA ANUNCIOS PARA TODOS
    public void salvarAnuncioPublico() {
        DatabaseReference anuncioRefe = ConFirebase.getFirebaseDatabase().child("vistoriaPu");
        anuncioRefe.child(getLocalizacao()).child(getIdVistoria()).setValue(toMap());
    }


    public Map<String, Object> getItensAsMap() {
        Map<String, Object> itensMap = new HashMap<>();
        for (Item item : getItens()) {
            itensMap.put(item.getId(), item.toMap());
        }
        return itensMap;
    }


    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("idVistoria", idVistoria);
        result.put("localizacao_data", localizacao_data);
        result.put("qrCodeURL", qrCodeURL);
        result.put("localizacao", localizacao);
        result.put("data", data);
        result.put("nomePerfilU", nomePerfilU);
        result.put("concluida", concluida);
        result.put("idInspector", idInspector);
        result.put("idUsuario", idUsuario);
        result.put("excluidaVistoria", excluidaVistoria);
        result.put("itens", itens);
        return result;
    }
}