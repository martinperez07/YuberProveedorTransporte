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
        String tituloNotificacion = remoteMessage.getNotification().getTitle();
        System.out.println("-->Nueva notificacion: " + tituloNotificacion);

        //ACA VAN LAS CADENAS DE NOTIFICACIONES QUE ME LLEGAN
        if (remoteMessage.getData().size() > 0) {
            if (tituloNotificacion.equals("Nueva solicitud")) {
                sendBodyToMapFragment(remoteMessage.getData());
            }else if (tituloNotificacion.equals("Destino elegido")) {
                sendBodyToMapFragmentComenzarViaje(remoteMessage.getData());
            }
        } else if (tituloNotificacion.equals("Tu viaje fue cancelado")) {
            sendBodyToMapFragmentCancelado();
        }
    }

    private void sendBodyToMapFragment(Map<String, String> data) {
        String aux = data.toString();
        Intent intent = new Intent("MpFragment.action.ACEPTAR_RECHAZAR");
        intent.putExtra("DATOS_USUARIOS", aux);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendBodyToMapFragmentCancelado() {
        Intent intent = new Intent("MpFragment.action.VIAJE_CANCELADO");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendBodyToMapFragmentComenzarViaje(Map<String, String> data) {
        String aux = data.toString();
        Intent intent = new Intent("MpFragment.action.DESTINO_ELEGIDO");
        intent.putExtra("DATOS_DESTINO", aux);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
