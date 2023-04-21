package Adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.List;

import Atividades.VistoriasEmAndamentoActivity;
import Modelos.Vistoria;
import br.com.patrimoniomv.R;

public class VistoriaAndamentoAdapter extends ArrayAdapter<Vistoria> {
    private VistoriasEmAndamentoActivity activity;
    private int resource;

    public VistoriaAndamentoAdapter(@NonNull VistoriasEmAndamentoActivity activity, int resource, @NonNull List<Vistoria> vistorias) {
        super(activity, resource, vistorias);
        this.activity = activity;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(resource, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.concluirVistoriaButton = convertView.findViewById(R.id.concluirVistoriaButton);
            viewHolder.nomePerfilTextView = convertView.findViewById(R.id.nomePerfilUTextView);
            viewHolder.dataTextView = convertView.findViewById(R.id.dataTextView);
            viewHolder.localizacaoTextView = convertView.findViewById(R.id.localizacaoTextView);
            viewHolder.nomeItem = convertView.findViewById(R.id.nomeItemTextView);
            viewHolder.numeroPlaca = convertView.findViewById(R.id.numeroPlacaTextView);
            viewHolder.outrasInformacoes = convertView.findViewById(R.id.outrasInformacoesTextView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Vistoria vistoriaAtual = getItem(position);
        viewHolder.nomePerfilTextView.setText(vistoriaAtual.getNomePerfilU());
        viewHolder.dataTextView.setText(vistoriaAtual.getData());
        viewHolder.localizacaoTextView.setText(vistoriaAtual.getLocalizacao());

        if (vistoriaAtual.isConcluida() && !isDuplicateVistoria(vistoriaAtual)) {
            viewHolder.concluirVistoriaButton.setBackgroundColor(ContextCompat.getColor(activity, R.color.green));
            viewHolder.concluirVistoriaButton.setEnabled(false);
        } else {
            viewHolder.concluirVistoriaButton.setBackgroundColor(ContextCompat.getColor(activity, R.color.red));
            viewHolder.concluirVistoriaButton.setEnabled(true);
        }

        viewHolder.concluirVistoriaButton.setTag(position);
        viewHolder.concluirVistoriaButton.setOnClickListener(view -> {
            int pos = (int) view.getTag();
            Vistoria vistoriaAtualClick = getItem(pos);
            activity.concluirVistoria(vistoriaAtualClick, pos); // Adicione pos como um parâmetro adicional
        });

        return convertView;
    }


    private boolean isDuplicateVistoria(Vistoria vistoriaAtual) {
        for (int i = 0; i < getCount(); i++) {
            Vistoria vistoria = getItem(i);
            if (vistoria != null && vistoriaAtual != null &&
                    vistoria.getLocalizacao() != null && vistoria.getData() != null &&
                    vistoria.getLocalizacao().equals(vistoriaAtual.getLocalizacao()) &&
                    vistoria.getData().equals(vistoriaAtual.getData()) &&
                    vistoria.getHour() == vistoriaAtual.getHour() &&
                    vistoria.getMinute() == vistoriaAtual.getMinute() &&
                    vistoria.getSecond() == vistoriaAtual.getSecond()) {

                return true;
            }
        }
        return false;
    }

    static class ViewHolder {
        Button concluirVistoriaButton;
        TextView nomePerfilTextView, nomeItem, numeroPlaca, outrasInformacoes;
        TextView dataTextView;
        TextView localizacaoTextView;
    }
}


