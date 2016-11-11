package yuber.yuber.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import yuber.yuber.R;

public class FragmentDialogYuberAceptarViaje extends DialogFragment {

    private static final String TAG = FragmentDialogYuberAceptarViaje.class.getSimpleName();

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String ClienteUbicacionOrigenKey = "ubicacionOrigenKey";
    public static final String ClienteUbicacionDestinoKey = "ubicacionDestinoKey";
    SharedPreferences sharedpreferences;
    private JSONObject destino;
    private JSONObject origen;

    public FragmentDialogYuberAceptarViaje() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createLoginDialogo();
    }

    public AlertDialog createLoginDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialogo_iniciar_viaje, null);
        builder.setView(v);

        String DireccionDestino = "";
        String DireccionOrigen = "";

        //JSONDestino
        String JSONDestino = getArguments().getString("DatosDestino");
        //el destino lo obtengo del JSON y lo guardo en session
        sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(ClienteUbicacionDestinoKey, JSONDestino);
        editor.commit();
        //JSONOrigen
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String JSONOrigen = sharedpreferences.getString(ClienteUbicacionOrigenKey, "");
        try {
            destino = new JSONObject(JSONDestino);
            origen = new JSONObject(JSONOrigen);
            Double latO = origen.getDouble("latitud");
            Double latD = destino.getDouble("latitud");
            Double lonO = origen.getDouble("longitud");
            Double lonD = destino.getDouble("longitud");

            //Calculo la direccion
            DireccionOrigen = getAddressFromLatLng(latO, lonO);
            DireccionDestino = getAddressFromLatLng(latD, lonD);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView textoOrigen = (TextView) v.findViewById(R.id.text_origen);
        TextView textoDestino = (TextView) v.findViewById(R.id.text_destino);

        textoOrigen.setText(DireccionOrigen);
        textoDestino.setText(DireccionDestino);

        Button botonAceptar = (Button) v.findViewById(R.id.boton_aceptar_yuber);
        botonAceptar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                }
        );

        return builder.create();

    }

    private String getAddressFromLatLng(double lat, double lon) {
        Geocoder geocoder = new Geocoder( getActivity() );
        String address = "";
        try {
            address =geocoder
                    .getFromLocation( lat, lon, 1 )
                    .get( 0 ).getAddressLine( 0 ) ;
        } catch (IOException e ) {
            // this is the line of code that sends a real error message to the  log
            Log.e("ERROR", "ERROR IN CODE: " + e.toString());
            // this is the line that prints out the location in the code where the error occurred.
            e.printStackTrace();
            return "ERROR_IN_CODE";
        }
        return address;
    }

}

