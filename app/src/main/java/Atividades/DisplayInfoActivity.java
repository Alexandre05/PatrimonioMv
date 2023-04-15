package Atividades;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.List;

import Modelos.ItensVistorias;
import br.com.patrimoniomv.R;

public class DisplayInfoActivity extends AppCompatActivity {
    public static final String EXTRA_ITEM_LIST_JSON = "item_list_json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_info);
        String itemListJson = getIntent().getStringExtra(EXTRA_ITEM_LIST_JSON);
        List<ItensVistorias> itemList = jsonToItemList(itemListJson);

        for (ItensVistorias item : itemList) {
            showItemInfo(item);
        }
    }

    private List<ItensVistorias> jsonToItemList(String jsonString) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<ItensVistorias>>() {}.getType();
        return gson.fromJson(jsonString, listType);
    }

    private void showItemInfo(ItensVistorias item) {
        TextView textViewItemInfo = findViewById(R.id.textViewItemInfo);
        String formattedInfo = formatItemInfo(item);
        textViewItemInfo.setText(formattedInfo);

        LinearLayout photosContainer = findViewById(R.id.photos_container);
        photosContainer.removeAllViews();

        if (item.getFotos() != null) {
            for (String photoUrl : item.getFotos()) {
                ImageView imageView = new ImageView(this);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(0, 10, 0, 10);

                Glide.with(this).load(photoUrl).into(imageView);
                photosContainer.addView(imageView);
            }
        }
    }

    private String formatItemInfo(ItensVistorias item) {
        String formattedInfo = "";

        formattedInfo += "Data: " + item.getData() + "\n";
        formattedInfo += "Localização: " + item.getLocalizacao() + "\n";
        formattedInfo += "Placa: " + item.getPlaca() + "\n";
        formattedInfo += "Nome do Item: " + item.getNomeItem() + "\n";
        formattedInfo += "Outras Informações: " + item.getOutrasInformacoes() + "\n";

        return formattedInfo;
    }
}
