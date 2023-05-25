package Atividades;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import Modelos.Vistoria;
import br.com.patrimoniomv.R;


public class QRCodeDetailsActivity extends AppCompatActivity {
    private TextView formattedQRData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_details);
        formattedQRData = findViewById(R.id.formatted_qr_data);

        if (getIntent().hasExtra("qr_code_data")) {
            String qrCodeData = getIntent().getStringExtra("qr_code_data");
            List<Vistoria> itemList = parseQRCodeData(qrCodeData);

            if (itemList != null && !itemList.isEmpty()) {
                displayFormattedQRData(itemList);
            } else {
                Toast.makeText(this, "Nenhum dado encontrado no QR Code", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private List<Vistoria> parseQRCodeData(String qrCodeData) {
        Log.d("QRCodeDetailsActivity", "QR Code data received in activity: " + qrCodeData);

        Gson gson = new Gson();
        Type listType = new TypeToken<List<Vistoria>>() {
        }.getType();
        List<Vistoria> itemList = gson.fromJson(qrCodeData, listType);

        Log.d("QRCodeDetailsActivity", "Item list after parsing: " + itemList);
        return itemList;
    }

    private void displayFormattedQRData(List<Vistoria> itemList) {
        Log.d("QRCodeDetailsActivity", "Displaying formatted QR data: " + itemList);

        StringBuilder sb = new StringBuilder();

        for (Vistoria item : itemList) {
            sb.append("Localização: ").append(item.getLocalizacao()).append("\n");
            sb.append("ID Inspetor: ").append(item.getIdInspector()).append("\n");
            sb.append("Data: ").append(item.getData()).append("\n");
            sb.append("Nome Perfil U: ").append(item.getNomePerfilU()).append("\n");

        }

        formattedQRData.setText(sb.toString());
    }
}
