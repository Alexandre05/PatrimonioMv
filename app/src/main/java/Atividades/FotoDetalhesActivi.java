package Atividades;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

import Adaptadores.FotosAdapter;
import br.com.patrimoniomv.R;


public class FotoDetalhesActivi extends AppCompatActivity {
    private ViewPager2 viewPager;
    private FotosAdapter fotosAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto_detalhes);
        viewPager = findViewById(R.id.viewPager);
        final LinearLayout dotsIndicator = findViewById(R.id.dots_indicator);

        String urlFoto = getIntent().getStringExtra("url_foto");
        List<String> fotos = getIntent().getStringArrayListExtra("fotos");

        fotosAdapter = new FotosAdapter(fotos, this);

        viewPager.setAdapter(fotosAdapter);
        createDotsIndicator(fotos.size(), dotsIndicator, 0);
        dotsIndicator.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                createDotsIndicator(fotos.size(), dotsIndicator, viewPager.getCurrentItem());
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                createDotsIndicator(fotos.size(), dotsIndicator, position);
            }
        });

        if (urlFoto != null && fotos != null) {
            int fotoIndex = fotos.indexOf(urlFoto);
            if (fotoIndex != -1) {
                viewPager.setCurrentItem(fotoIndex, false);
            }
        }
    }

    private void createDotsIndicator(int count, LinearLayout linearLayout, int currentIndex) {
        float scale = getResources().getDisplayMetrics().density;
        int selectedDotSize = (int) (12 * scale + 0.5f);
        int unselectedDotSize = (int) (8 * scale + 0.5f);

        linearLayout.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(i == currentIndex ? selectedDotSize : unselectedDotSize, i == currentIndex ? selectedDotSize : unselectedDotSize);
            layoutParams.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(layoutParams);
            dot.setBackgroundResource(R.drawable.dot_indicator);
            dot.setActivated(i == currentIndex);
            linearLayout.addView(dot);
        }
    }


}