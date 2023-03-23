package Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import Mode.ItensVistorias;
import br.com.patrimoniomv.R;

public class VistoriaAdapter extends ArrayAdapter<ItensVistorias> {

    private Context context;
    private List<ItensVistorias> vistorias;

    public VistoriaAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<ItensVistorias> vistorias) {
        super(context, resource, vistorias);
        this.context = context;
        this.vistorias = vistorias;
    }


}
