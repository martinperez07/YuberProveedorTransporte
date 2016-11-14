package yuber.yuber.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import yuber.yuber.R;

public class FragmentDialogYuberCalificar extends DialogFragment {

    private String Ip = "54.213.51.6";
    private String Puerto = "8080";

    private static final String TAG = FragmentDialogYuberCancelaronViaje.class.getSimpleName();
    private RatingBar ratingBarPuntaje;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String ClienteInstanciaServicioKey = "clienteInstanciaServicioKey";
    public static final String ClienteNombreKey = "clienteNombreKey";
    public static final String ClienteApellidoKey = "clienteApellidoKey";
    public static final String ClienteUbicacionOrigenKey = "ubicacionOrigenKey";
    public static final String ClienteTelefonoKey = "clienteTelefonoKey";
    public static final String ClienteUbicacionDestinoKey = "ubicacionDestinoKey";
    public static final String EnViaje = "enViaje";

    SharedPreferences sharedpreferences;

    public FragmentDialogYuberCalificar() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createLoginDialogo();
    }

    public AlertDialog createLoginDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialogo_calificar, null);
        builder.setView(v);

        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String Nombre = sharedpreferences.getString(ClienteNombreKey, "");
        String Apellido = sharedpreferences.getString(ClienteApellidoKey, "");

        TextView texto = (TextView) v.findViewById(R.id.text_titulo_calificacion);
        texto.setText("Califica a " + Nombre + " " + Apellido);

        ratingBarPuntaje = (RatingBar) v.findViewById(R.id.ratingBarDialogHistorial);
        double puntaje = 2.5;
        ratingBarPuntaje.setRating(((float) puntaje));

        Button botonConfirmar = (Button) v.findViewById(R.id.boton_confirmar);
        botonConfirmar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Limpio las variable en sesion relacionadaas al viaje
                        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.remove(ClienteNombreKey);
                        editor.remove(ClienteApellidoKey);
                        editor.remove(ClienteUbicacionOrigenKey);
                        editor.remove(ClienteTelefonoKey);
                        editor.remove(ClienteUbicacionDestinoKey);
                        editor.putString(EnViaje, "false");
                        editor.commit();
                        //Envio el puntaje al servidor
                        enviarPuntaje();
                        dismiss();
                    }
                }
        );
        return builder.create();
    }

    public void enviarPuntaje(){
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String instanciaID = sharedpreferences.getString(ClienteInstanciaServicioKey, "");
        String puntaje;
        float number = ratingBarPuntaje.getRating();
        int punt = new Float(number).intValue();

        puntaje = String.valueOf(number);
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Cliente/PuntuarCliente/" + punt + ",Sin comentario," + instanciaID;

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                System.out.println("--------------->Success");
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                System.out.println("--------------->Fail");
            }
        });
    }

}


