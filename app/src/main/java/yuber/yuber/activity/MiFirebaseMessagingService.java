package yuber.yuber.activity;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;


public class MiFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MESSAGES";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            System.out.println("--> Nueva notificacion");
            //ACA VAN LAS CADENAS DE NOTIFICACIONES QUE ME LLEGAN
            String tituloNotificacion = remoteMessage.getNotification().getTitle();
            if (tituloNotificacion.equals("Nueva solicitud")) {
                sendBodyToMapFragment(remoteMessage.getData());
            }else if (tituloNotificacion.equals("Tu viaje fue cancelado")) {

            }else if (tituloNotificacion.equals("")) {

            }
        }
    }

    private void sendBodyToMapFragment(Map<String, String> data) {
        String aux = data.toString();
        Intent intent = new Intent("MpFragment.action.ACEPTAR_RECHAZAR");
        intent.putExtra("DATOS_USUARIOS", aux);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendBodyToMapFragmentCancelado(String text) {
        Intent intent = new Intent("MpFragment.action.VIAJE_CANCELADO");
        intent.putExtra("DATOS_USUARIOS", text);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
