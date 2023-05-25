package Atividades;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import br.com.patrimoniomv.R;


public class ViewQRCodeActivity extends AppCompatActivity {

    private ImageView qrCodeImageView;
    Button saveButto;
    Button imprimir;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_qrcode);

        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        saveButto = findViewById(R.id.saveQRCodeButton);
        imprimir = findViewById(R.id.printQRCodeButton);

        Intent intent = getIntent();
        if (intent != null) {
            String qrCodeData = intent.getStringExtra("qrCodeData");
            Bitmap qrCodeBitmap = generateQRCode(qrCodeData);
            qrCodeImageView.setImageBitmap(qrCodeBitmap);
        }

        saveButto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap qrCodeBitmap = ((BitmapDrawable) qrCodeImageView.getDrawable()).getBitmap();
                saveQRCodeToGallery(ViewQRCodeActivity.this, qrCodeBitmap, "qrCodeData");
            }
        });
        imprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printQRCode();
            }
        });
    }

    // Copie o método generateQRCode() aqui ou mova-o para uma classe utilitária para compartilhar entre as atividades
    private Bitmap generateQRCode(String text) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        int width = 300;
        int height = 300;

        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void printQRCode() {
        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        Bitmap qrCodeBitmap = ((BitmapDrawable) qrCodeImageView.getDrawable()).getBitmap();
        photoPrinter.printBitmap("QR Code", qrCodeBitmap);
    }
    private void saveQRCodeToGallery(Context context, Bitmap bitmap, String fileName) {
        OutputStream outputStream;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "QR_Codes");

            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            try {
                outputStream = resolver.openOutputStream(imageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + "QR_Codes";
            File file = new File(imagesDir);

            if (!file.exists()) {
                file.mkdirs();
            }

            File image = new File(imagesDir, fileName + ".png");
            try {
                outputStream = new FileOutputStream(image);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        try {
            outputStream.flush();
            outputStream.close();
            Toast.makeText(context, "QR Code salvo com sucesso!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Erro ao salvar QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}