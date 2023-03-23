package Adapter;

import android.content.res.ColorStateList;
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
import Mode.ItensVistorias;
import br.com.patrimoniomv.R;

public class VistoriaAndamentoAdapter extends ArrayAdapter<ItensVistorias> {
    private VistoriasEmAndamentoActivity activity;
    private int resource;

    public VistoriaAndamentoAdapter(@NonNull VistoriasEmAndamentoActivity activity, int resource, @NonNull List<ItensVistorias> vistorias) {
        super(activity, resource, vistorias);
        this.activity = activity;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(resource, parent, false);
        }

        TextView nomePerfilTextView = convertView.findViewById(R.id.nomePerfilUTextView);
        TextView dataTextView = convertView.findViewById(R.id.dataTextView);
        TextView localizacaoTextView = convertView.findViewById(R.id.localizacaoTextView);
        Button concluirVistoriaButton = convertView.findViewById(R.id.concluirVistoriaButton);

        ItensVistorias vistoriaAtual = getItem(position);

        nomePerfilTextView.setText(vistoriaAtual.getNomePerfilU());
        dataTextView.setText(vistoriaAtual.getData());
        localizacaoTextView.setText(vistoriaAtual.getLocalizacao());

        updateConcluirVistoriaButton(concluirVistoriaButton, vistoriaAtual);

        concluirVistoriaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!vistoriaAtual.isConcluida()) {
                    vistoriaAtual.setConcluida(true);
                    activity.concluirVistoria(vistoriaAtual);
                    updateConcluirVistoriaButton(concluirVistoriaButton, vistoriaAtual);
                }
            }
        });

        return convertView;
    }
    private void updateConcluirVistoriaButton(Button concluirVistoriaButton, ItensVistorias vistoriaAtual) {
        if (vistoriaAtual.isConcluida()) {
            concluirVistoriaButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.green)));
            concluirVistoriaButton.setText(R.string.vistoria_finalizada);
            concluirVistoriaButton.setEnabled(false);
        } else {
            concluirVistoriaButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.accentColor)));
            concluirVistoriaButton.setText(R.string.concluir_vistoria);
            concluirVistoriaButton.setEnabled(true);
        }
    }
}
