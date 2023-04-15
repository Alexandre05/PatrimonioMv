package Serviços;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import Atividades.Chat;
import br.com.patrimoniomv.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private DatabaseReference database;

    private TextView mensagem;
    // metodo que funciona com ap em primeiro plano




    @Override
    public void onMessageReceived( RemoteMessage notificacao) {

        if (notificacao.getNotification() != null) {

            String titulo = notificacao.getNotification().getTitle();

            String corpo = notificacao.getNotification().getBody();


            enviarNotifica(titulo, corpo);

        }








    }


    private void enviarNotifica(String titulo, String corpo) {


        // Create an Intent for the activity you want to start
        Intent intent = new Intent(this, Chat.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Create the TaskStackBuilder and add the intent, which inflates the back stack

        // Get the PendingIntent containing the entire back stack
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
        String canal = getString(R.string.default_notification_channel_id);

        Uri som = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // cria a notificação
        NotificationCompat.Builder notifica = new NotificationCompat.Builder(this, canal)
                .setContentTitle(titulo)
                .setContentText(corpo)
                .setSmallIcon(R.drawable.send)
                .setSound(som)
                .setAutoCancel(true)

                .setWhen(System.currentTimeMillis())
                .setVibrate(new  long[]{1000,1000})
                .setContentIntent(pendingIntent);
        NotificationManager notificaf=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);



// recupera notificação


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chanel = new NotificationChannel(canal, "canal", NotificationManager.IMPORTANCE_DEFAULT);
            notificaf.createNotificationChannel(chanel);


        }
        ;


        // envia notificação
        notificaf.notify(0, notifica.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        // salvar token



    }






}


