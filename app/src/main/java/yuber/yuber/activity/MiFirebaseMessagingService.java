package yuber.yuber.activity;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MiFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MESSAGES";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Message title: " + remoteMessage.getNotification().getTitle() );
        Log.d(TAG, "Message data body: " + remoteMessage.getNotification().getBody());

        if (remoteMessage.getData().size() > 0) {
            //ACA VAN LAS CADENAS DE NOTIFICACIONES QUE ME LLEGAN
            String tituloNotificacion = remoteMessage.getNotification().getTitle();
            if (tituloNotificacion.equals("Nueva solicitud")) {
                Log.d(TAG, "ADENTRO DEL NUEVA SOLICITUD");
                sendBodyToMapFragment(remoteMessage.getNotification().getBody());
            }else if (tituloNotificacion.equals("Tu viaje fue cancelado")) {

            }else if (tituloNotificacion.equals("")) {

            }
        }
    }

    private void sendBodyToMapFragment(String text) {
        Intent intent = new Intent("MpFragment.action.ACEPTAR_RECHAZAR");
        intent.putExtra("DATOS_USUARIOS", text);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendBodyToMapFragmentCancelado(String text) {
        Intent intent = new Intent("MpFragment.action.VIAJE_CANCELADO");
        intent.putExtra("DATOS_USUARIOS", text);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
