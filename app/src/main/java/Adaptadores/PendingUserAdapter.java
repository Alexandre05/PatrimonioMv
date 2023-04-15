package Adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Modelos.Usuario;
import br.com.patrimoniomv.R;

public class PendingUserAdapter extends RecyclerView.Adapter<PendingUserAdapter.ViewHolder> {

    private Context context;
    private List<Usuario> pendingUsers;
    private OnUserActionListener userActionListener;

    public PendingUserAdapter(Context context, List<Usuario> pendingUsers, OnUserActionListener userActionListener) {
        this.context = context;
        this.pendingUsers = pendingUsers;
        this.userActionListener = userActionListener;
    }

    @NonNull
    @Override
    public PendingUserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_user, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Usuario user = pendingUsers.get(position);
        holder.tvName.setText(user.getNome());
        holder.tvEmail.setText(user.getEmail());

        holder.btnApprove.setOnClickListener(v -> userActionListener.onApprove(user));
        holder.btnReject.setOnClickListener(v -> userActionListener.onReject(user));
    }

    @Override
    public int getItemCount() {
        return pendingUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvEmail;
        private Button btnApprove;
        private Button btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_pending_user_name);
            tvEmail = itemView.findViewById(R.id.tv_pending_user_email);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }
    }

    public interface OnUserActionListener {
        void onApprove(Usuario user);
        void onReject(Usuario user);
    }
}
