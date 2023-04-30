package Atividades;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import br.com.patrimoniomv.R;

public class FullscreenImageActivity extends AppCompatActivity {
    private ImageView fullscreenImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        fullscreenImageView = findViewById(R.id.fullscreen_image);
        String imageUrl = getIntent().getStringExtra("image_url");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).into(fullscreenImageView);
        } else {
            Toast.makeText(this, "Erro ao carregar a imagem.", Toast.LENGTH_SHORT).show();
            finish();
        }

        fullscreenImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
