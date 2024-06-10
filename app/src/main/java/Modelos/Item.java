package Modelos;

import android.os.Parcel;
import android.os.Parcelable;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Item implements Parcelable, Serializable {
    private String localizacao;
    private String idVistoria;

    public String getIdVistoria() {
        return idVistoria;
    }

    public void setIdVistoria(String idVistoria) {
        this.idVistoria = idVistoria;
    }

    private String id;
    private String nome;
    private String placa;
    private List<String> fotos;
    private String observacao;
    private String fotoURL;
    private double latitude;
    private double longitude;

    public Item() {
    }

    // Métodos necessários para Parcelable
    protected Item(Parcel in) {
        localizacao = in.readString();
        id = in.readString();
        nome = in.readString();
        placa = in.readString();
        fotos = in.createStringArrayList();
        observacao = in.readString();
        fotoURL = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(localizacao);
        dest.writeString(id);
        dest.writeString(nome);
        dest.writeString(placa);
        dest.writeStringList(fotos);
        dest.writeString(observacao);
        dest.writeString(fotoURL);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    @PropertyName("id")
    public String getId() {
        return id;
    }

    @PropertyName("id")
    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @PropertyName("placa")
    public String getPlaca() {
        return placa;
    }

    @PropertyName("placa")
    public void setPlaca(String placa) {
        this.placa = placa;
    }

    @PropertyName("observacao")
    public String getObservacao() {
        return observacao;
    }

    @PropertyName("observacao")
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    @PropertyName("fotoURL")
    public String getFotoURL() {
        return fotoURL;
    }

    @PropertyName("fotoURL")
    public void setFotoURL(String fotoURL) {
        this.fotoURL = fotoURL;
    }

    @PropertyName("fotos")
    public List<String> getFotos() {
        return fotos;
    }

    public void setFotos(List<String> fotos) {
        this.fotos = fotos;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public static Task<Boolean> verificarPlacaExistente(String placa) {
        DatabaseReference vistoriasRef = FirebaseDatabase.getInstance().getReference("vistoriaPu");
        Query query = vistoriasRef.orderByChild("itens/placa").equalTo(placa);

        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean placaExistente = dataSnapshot.exists();
                taskCompletionSource.setResult(placaExistente);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                taskCompletionSource.setException(databaseError.toException());
            }
        });

        return taskCompletionSource.getTask();
    }

    public static Item fromMap(Map<String, Object> map) {
        Item item = new Item();
        item.setId((String) map.get("id"));
        item.setNome((String) map.get("nome"));
        item.setPlaca((String) map.get("placa"));
        item.setObservacao((String) map.get("observacao"));
        item.setFotos((List<String>) map.get("fotos"));
        item.setLocalizacao((String) map.get("localizacao"));
        item.setLatitude((double) map.get("latitude"));
        item.setLongitude((double) map.get("longitude"));

        if (item.getFotos() != null && !item.getFotos().isEmpty()) {
            item.setFotoURL(item.getFotos().get(0));
        }

        return item;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        if (id != null) result.put("id", id);
        if (nome != null) result.put("nome", nome);
        if (placa != null) result.put("placa", placa);
        if (observacao != null) result.put("observacao", observacao);
        if (fotos != null) result.put("fotos", fotos);
        if (localizacao != null) result.put("localizacao", localizacao);
        if (latitude != 0.0) result.put("latitude", latitude);
        if (longitude != 0.0) result.put("longitude", longitude);
        if (fotoURL != null) result.put("fotoURL", fotoURL);

        return result;
    }


    @Override
    public String toString() {
        return "Item{" +
                "nome='" + nome + '\'' +
                ", placa='" + placa + '\'' +
                ", observacao='" + observacao + '\'' +
                '}';
    }
}
