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
    private String id;
    private String NomeItem;
    private String placa;
    private List<String> fotos;
    private String observacao;
    private String fotoURL;

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

    public String getNomeItem() {
        return NomeItem;
    }

    public void setNomeItem(String nomeItem) {
        NomeItem = nomeItem;
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
        result.put("nome", NomeItem);
        result.put("placa", placa);
        result.put("observacao", observacao);
        result.put("fotos", fotos);
        return result;
    }

}
