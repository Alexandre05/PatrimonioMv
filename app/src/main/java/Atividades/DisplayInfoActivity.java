package Atividades;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.List;

import Modelos.Vistoria;
import br.com.patrimoniomv.R;

public class DisplayInfoActivity extends AppCompatActivity {
    public static final String EXTRA_ITEM_LIST_JSON = "item_list_json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_info);
        String itemListJson = getIntent().getStringExtra(EXTRA_ITEM_LIST_JSON);
        List<Vistoria> itemList = jsonToItemList(itemListJson);

        for (Vistoria item : itemList) {
            showItemInfo(item);
        }
    }

    private List<Vistoria> jsonToItemList(String jsonString) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Vistoria>>() {}.getType();
        return gson.fromJson(jsonString, listType);
    }

    private void showItemInfo(Vistoria item) {
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

    private String formatItemInfo(Vistoria item) {
        String formattedInfo = "";

        formattedInfo += "Data: " + item.getData() + "\n";
        formattedInfo += "Localização: " + item.getLocalizacao() + "\n";


        return formattedInfo;
    }
}
