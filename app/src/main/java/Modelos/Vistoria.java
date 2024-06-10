package Modelos;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.PropertyName;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Ajuda.ConFirebase;

public class Vistoria implements Serializable {
    private String idUsuario;
    private String idVistoriaAnexada;
    private Map<String, Item> itensMap;
    public String getIdVistoriaAnexada() {
        return idVistoriaAnexada;
    }

    public void setIdVistoriaAnexada(String idVistoriaAnexada) {
        this.idVistoriaAnexada = idVistoriaAnexada;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Boolean getConcluida() {
        return concluida == null ? false : concluida;
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





    public Map<String, Item> getItensMap() {
        return itensMap;
    }

    public void setItensMap(Map<String, Item> itensMap) {
        this.itensMap = itensMap;
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
    @Override
    public String toString() {
        return "Vistoria{" +
                "nomePerfilU='" + nomePerfilU + '\'' +
                ", data='" + data + '\'' +
                ", localizacao='" + localizacao + '\'' +
                ", itensMap=" + itensMap +
                '}';
    }


    public Vistoria() {

        // Este é o construtor existente com inicializações específicas
        DatabaseReference anuncioRefe = ConFirebase.getFirebaseDatabase().child("vistorias");
        setIdVistoria(anuncioRefe.push().getKey());
        itensMap = new LinkedHashMap<>();
    }

    // Adicione este construtor sem argumentos para atender aos requisitos do Firebase
    public Vistoria(String dummy) {
        // Construtor sem argumentos para atender aos requisitos do Firebase
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
    public void salvar(boolean isNew) {
        String idUsuario = ConFirebase.getIdUsuario();
        Log.d("id usuario", "Teste" + idUsuario);
        setIdInspector(idUsuario);
        setIdUsuario(idUsuario);

        DatabaseReference anuncioRefe = ConFirebase.getFirebaseDatabase().child("vistorias");
        DatabaseReference vistoriaRef = anuncioRefe.child(idUsuario).child(getLocalizacao()).child(getLocalizacao_data());

        try {
            if (isNew) {
                vistoriaRef.setValue(this);
                vistoriaRef.child("itens").setValue(itensMap);
            } else {
                vistoriaRef.child("itens").setValue(itensMap);
            }

            vistoriaRef.child("idUsuario").setValue(getIdUsuario());
            salvarAnuncioPublico();
            Log.d("Salvar", "Vistoria salva com sucesso.");
        } catch (Exception e) {
            Log.d("Salvar", "Erro ao salvar vistoria: " + e.getMessage());
        }
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
    public static Vistoria fromMap(Map<String, Object> map) {
        Vistoria vistoria = new Vistoria();

        vistoria.setIdVistoria((String) map.get("idVistoria"));
        vistoria.setLocalizacao_data((String) map.get("localizacao_data"));
        vistoria.setQrCodeURL((String) map.get("qrCodeURL"));
        vistoria.setLocalizacao((String) map.get("localizacao"));
        vistoria.setData((String) map.get("data"));
        vistoria.setNomePerfilU((String) map.get("nomePerfilU"));
        vistoria.setConcluida((Boolean) map.get("concluida"));
        vistoria.setIdInspector((String) map.get("idInspector"));
        vistoria.setIdUsuario((String) map.get("idUsuario"));
        vistoria.setExcluidaVistoria((Boolean) map.get("excluidaVistoria"));

        Map<String, Map<String, Object>> itensMap = (Map<String, Map<String, Object>>) map.get("itens");
        if (itensMap != null) {
            Map<String, Item> convertedItensMap = new HashMap<>();
            for (Map.Entry<String, Map<String, Object>> entry : itensMap.entrySet()) {
                Item item = Item.fromMap(entry.getValue());
                convertedItensMap.put(entry.getKey(), item);
            }
            vistoria.setItensMap(convertedItensMap);
        }

        return vistoria;
    }
    public static DatabaseReference getVistoriaReference(String idVistoria) {
        return ConFirebase.getFirebaseDatabase().child("vistorias").child(idVistoria);
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

        // Converta os itens para objetos antes de adicioná-los ao resultado
        HashMap<String, Object> itemObjectsMap = new HashMap<>();
        for (Map.Entry<String, Item> entry : itensMap.entrySet()) {
            itemObjectsMap.put(entry.getKey(), entry.getValue().toMap());
        }
        result.put("itens", itemObjectsMap);

        return result;
    }
    public void adicionarItem(Item item) {
        if (itensMap == null) {
            itensMap = new HashMap<>();
        }
    }

}