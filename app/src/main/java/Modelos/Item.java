package Modelos;

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

public class Item  implements Serializable {
    private String localizacao;
    private String id;
    private String nome;

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    private String placa;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    private List<String> fotos;
    private String observacao;
    private String fotoURL;
    private double latitude; // Adicione esta linha
    private double longitude; // Adicione esta linha

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

    public Item() {
    }

    @PropertyName("id")
    public String getId() {
        return id;
    }

    @PropertyName("id")
    public void setId(String id) {
        this.id = id;
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

    public static Task<Boolean> verificarPlacaExistente(String placa) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();

        DatabaseReference vistoriasRef = FirebaseDatabase.getInstance().getReference("vistoriaPu");
        Query query = vistoriasRef.orderByChild("itens/placa").equalTo(placa);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean placaExistente = dataSnapshot.exists();
                taskCompletionSource.setResult(placaExistente);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                taskCompletionSource.setException(databaseError.toException());
            }
        };

        query.addListenerForSingleValueEvent(valueEventListener);

        return taskCompletionSource.getTask();
    }

    @PropertyName("fotos")
    public void setFotos(List<String> fotos) {
        this.fotos = fotos;
    }
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("nome", nome);
        result.put("placa", placa);
        result.put("observacao", observacao);
        result.put("fotos", fotos);
        result.put("localizacao",localizacao);
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        return result;
    }

}
