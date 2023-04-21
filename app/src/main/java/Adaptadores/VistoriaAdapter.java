package Adaptadores;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import java.util.List;

import Modelos.Vistoria;

public class VistoriaAdapter extends ArrayAdapter<Vistoria> {

    private Context context;
    private List<Vistoria> vistorias;

    public VistoriaAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Vistoria> vistorias) {
        super(context, resource, vistorias);
        this.context = context;
        this.vistorias = vistorias;
    }


}
