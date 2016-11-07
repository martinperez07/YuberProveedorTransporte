package yuber.yuber.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import yuber.yuber.R;

/**
 * Fragmento con un diálogo personalizado
 */
public class FragmentDialogYuberAceptarRechazar extends DialogFragment {
    private static final String TAG = FragmentDialogYuberAceptarRechazar.class.getSimpleName();
    private JSONObject mProveedor;

    public FragmentDialogYuberAceptarRechazar() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createLoginDialogo();
    }

    /**
     * Crea un diálogo con personalizado para comportarse
     * como formulario de login
     *
     * @return Diálogo
     */
    public AlertDialog createLoginDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

       // String proveedorString = getArguments().getString("proveedorJson");
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
        try {
            textoNombreProv.setText(mProveedor.getString("usuarioNombre"));
            textoAppellidoProv.setText(mProveedor.getString("usuarioApellido"));
            textoTelefonoProv.setText(mProveedor.getString("usuarioTelefono"));
            puntaje = mProveedor.getDouble("usuarioPromedioPuntaje");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ratingBarPuntajeProv.setRating(((float) puntaje));
        Button botonAceptar = (Button) v.findViewById(R.id.boton_aceptar_yuber);
        Button botonCancelar = (Button) v.findViewById(R.id.boton_cancelar_yuber);

        System.out.println(mProveedor);

        botonAceptar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //envio al server que acepto el viaje




                        //dismiss();
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

}

