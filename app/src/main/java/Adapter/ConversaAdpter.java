package Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import Mode.ConversaAssunto;
import Mode.Usuario;
import br.com.patrimoniomv.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class ConversaAdpter extends RecyclerView.Adapter<ConversaAdpter.MyVilHolder> {

    private List<ConversaAssunto> conversas;
    private Context context;
    public ConversaAdpter(List<ConversaAssunto> lista, Context c) {
        this.conversas=lista;
        this.context=c;
    }


    @Override
    public MyVilHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_adapter_co, parent, false);
        return new MyVilHolder(itemLista);
    }

    @Override
    public void onBindViewHolder( MyVilHolder holder, int position) {
        ConversaAssunto conversaAssunto = conversas.get(position);
        holder.ult.setText(conversaAssunto.getUltimaMensagem());

        Usuario u = conversaAssunto.getUruarioExibicao();
        holder.nome.setText(u.getNome());
        //holder.status.setText(u.getStatus());


        if(u.getFoto()!=null){

            Uri uri = Uri.parse(u.getFoto());
            Glide.with(context).load(uri).into(holder.foto);


        }else {


            holder.foto.setImageResource(R.drawable.c_pessoa);
        }



    }

    @Override
    public int getItemCount() {
        return conversas.size();
    }

    public  class  MyVilHolder extends  RecyclerView.ViewHolder{

        CircleImageView foto;
        TextView nome,ult,status;

        public MyVilHolder(@NonNull View itemView) {
            super(itemView);

            foto = itemView.findViewById(R.id.imageContato);
            nome=itemView.findViewById(R.id.nomeContato);
            ult=itemView.findViewById(R.id.ultimaContato);
            //status=itemView.findViewById(R.id.status);
        }
    }

}

