package Api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String PDF_URI_EXTRA = "pdf_uri_extra";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.hasExtra(PDF_URI_EXTRA)) {
            Uri pdfUri = intent.getParcelableExtra(PDF_URI_EXTRA);
            Intent viewPdfIntent = new Intent(Intent.ACTION_VIEW);
            viewPdfIntent.setDataAndType(pdfUri, "application/pdf");
            viewPdfIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(viewPdfIntent);
        }
    }
}

