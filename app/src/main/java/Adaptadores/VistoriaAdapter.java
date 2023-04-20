package Adaptadores;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import java.util.List;

import Modelos.Vistorias;

public class VistoriaAdapter extends ArrayAdapter<Vistorias> {

    private Context context;
    private List<Vistorias> vistorias;

    public VistoriaAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Vistorias> vistorias) {
        super(context, resource, vistorias);
        this.context = context;
        this.vistorias = vistorias;
    }


}
