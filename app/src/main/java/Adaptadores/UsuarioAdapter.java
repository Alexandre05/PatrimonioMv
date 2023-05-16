package Adaptadores;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import Modelos.Usuario;
import br.com.patrimoniomv.R;

public class UsuarioAdapter extends ArrayAdapter<Usuario> {
    private final DatabaseReference usersRef;

    public UsuarioAdapter(Context context, int resource, List<Usuario> usuarios) {
        super(context, resource, usuarios);
        this.usersRef = FirebaseDatabase.getInstance().getReference("usuarios");
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_usuario, parent, false);
        }

        Usuario usuario = getItem(position);

        TextView nomeUsuario = convertView.findViewById(R.id.nomeUsuario);
        Button botaoAdicionarComissao = convertView.findViewById(R.id.botaoAdicionarComissao);

        nomeUsuario.setText(usuario.getNome());

        botaoAdicionarComissao.setOnClickListener(v -> {
            // Adicione o usuário à comissão aqui
            // Você pode precisar fazer isso de maneira diferente dependendo de como sua classe Administrar está configurada
            // adicionarMembroComissao(usuario.getMatricula());
            updateUserStatus(usuario);
        });

        return convertView;
    }

    private void updateUserStatus(Usuario usuario) {
        if (usuario.getIdU() != null) {
            usersRef.child(usuario.getIdU()).child("status").setValue("aprovado")
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Status atualizado com sucesso.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Falha ao atualizar o status.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "ID do usuário é nulo.", Toast.LENGTH_SHORT).show();
        }
    }


}
