package Adaptadores;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import java.util.List;

import Modelos.ItensVistorias;

public class VistoriaAdapter extends ArrayAdapter<ItensVistorias> {

    private Context context;
    private List<ItensVistorias> vistorias;

    public VistoriaAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<ItensVistorias> vistorias) {
        super(context, resource, vistorias);
        this.context = context;
        this.vistorias = vistorias;
    }


}
