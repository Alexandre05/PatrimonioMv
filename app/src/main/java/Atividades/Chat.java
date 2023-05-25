package Atividades;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.ArrayList;
import java.util.List;

import Fragment.ContatosT1;
import Fragment.Conversas;
import Modelos.Vistoria;

import br.com.patrimoniomv.R;
import retrofit2.Retrofit;

public class Chat extends AppCompatActivity {
    private Retrofit retrofit;
    private String baseUrl;
    private TextView nome;
    private ViewPager viewPager;
    private SmartTabLayout smartTabLayout;
    private List<Vistoria> listaciContatoVenda = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        viewPager= findViewById(R.id.viewPager);
        smartTabLayout= findViewById(R.id.viewPagerTab);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.setTitle("Comissião Patrimonio");

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(

                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                        .add("Conversas", Conversas.class)
                        .add("Contatos", ContatosT1.class)
                        .create()

        );

        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        SmartTabLayout view=findViewById(R.id.viewPagerTab);
        view.setViewPager(viewPager);


    }



}