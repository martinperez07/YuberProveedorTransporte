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

import org.json.JSONException;
import org.json.JSONObject;

import yuber.yuber.R;

public class FragmentDialogYuberAceptarRechazar extends DialogFragment {
    private static final String TAG = FragmentDialogYuberAceptarRechazar.class.getSimpleName();
    private JSONObject mProveedor;
    private JSONObject mCliente;
    private String Ip = "54.213.51.6";
    private String Puerto = "8080";

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String EmailKey = "emailKey";
    public static final String TokenKey = "tokenKey";
    SharedPreferences sharedpreferences;

    public FragmentDialogYuberAceptarRechazar() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createLoginDialogo();
    }

    public AlertDialog createLoginDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String proveedorString = getArguments().getString("DatosUsuario");
        try {
            mProveedor = new JSONObject(proveedorString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialogo_yuber_aceptar_rechazar, null);
        builder.setView(v);

        TextView textoNombreProv = (TextView) v.findViewById(R.id.text_dialog_yub_disp_nombre);
        TextView textoAppellidoProv = (TextView) v.findViewById(R.id.text_dialog_yub_disp_apellido);
        TextView textoTelefonoProv = (TextView) v.findViewById(R.id.text_dialog_yub_disp_telefono);
        RatingBar ratingBarPuntajeProv = (RatingBar) v.findViewById(R.id.ratingBarYuberDispo);

        double puntaje = 0;
        String instanciaId = "";
        try {
            String JSONcliente = mProveedor.getString("cliente");
            mCliente = new JSONObject(JSONcliente);
            textoNombreProv.setText(mCliente.getString("usuarioNombre"));
            textoAppellidoProv.setText(mCliente.getString("usuarioApellido"));
            textoTelefonoProv.setText(mCliente.getString("usuarioTelefono"));
            puntaje = mCliente.getDouble("usuarioPromedioPuntaje");
            instanciaId = mProveedor.getString("instanciaServicioId");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ratingBarPuntajeProv.setRating(((float) puntaje));
        Button botonAceptar = (Button) v.findViewById(R.id.boton_aceptar_yuber);
        Button botonCancelar = (Button) v.findViewById(R.id.boton_cancelar_yuber);

        final String finalInstanciaId = instanciaId;
        botonAceptar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //envio al server que acepto el viaje
                        AceptarServicio(finalInstanciaId);
                        dismiss();
                    }
                }
        );

        botonCancelar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                }
        );

        return builder.create();
    }

    public void AceptarServicio(String instanciaServicioId){
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String email = sharedpreferences.getString(EmailKey, "");
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/AceptarServicio/" + instanciaServicioId + "," + email;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
            }
        });
    }

}

