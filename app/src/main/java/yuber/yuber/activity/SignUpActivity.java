package yuber.yuber.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import yuber.yuber.R;


public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private String Ip = "54.213.51.6";
    private String Puerto = "8080";
    private EditText nameText;
    private EditText addressText;
    private EditText emailText;
    private EditText mobileText;
    private EditText passwordText;
    private EditText reEnterPasswordText;
    private EditText LastNameText;
    private EditText ciudadText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Button signupButton = (Button) findViewById(R.id.btn_signup);
        TextView loginLink = (TextView) findViewById(R.id.link_login);

        nameText = (EditText) findViewById(R.id.input_name);
        addressText = (EditText) findViewById(R.id.input_address);
        emailText = (EditText) findViewById(R.id.input_email);
        mobileText = (EditText) findViewById(R.id.input_mobile);
        passwordText = (EditText) findViewById(R.id.input_password);
        reEnterPasswordText = (EditText) findViewById(R.id.input_reEnterPassword);
        LastNameText = (EditText) findViewById(R.id.input_LastName);
        ciudadText = (EditText) findViewById(R.id.input_ciudad);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

    }

    public void signup() {
        if (!validate()) {
            onSignupFailed();
            return;
        }
        Button signupButton = (Button) findViewById(R.id.btn_signup);
        signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this,
                R.style.AppTheme_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creando su cuenta...");
        progressDialog.show();

        String name = nameText.getText().toString();
        String LastName = LastNameText.getText().toString();
        String address = addressText.getText().toString();
        String email = emailText.getText().toString();
        String mobile = mobileText.getText().toString();
        String ciudad = ciudadText.getText().toString();
        String password = passwordText.getText().toString();

        //*****************  Consulta a BD si existe el user ********************//
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/RegistrarProveedor/";
        JSONObject obj = new JSONObject();
        try {
            obj.put("usuarioDireccion", address);
            obj.put("usuarioContraseña", password);
            obj.put("usuarioTelefono", mobile);
            obj.put("usuarioApellido", LastName);
            obj.put("usuarioNombre", name);
            obj.put("usuarioPromedioPuntaje", 0.0);
            obj.put("usuarioCorreo", email);
            obj.put("usuarioCiudad", ciudad);
            obj.put("estado", "OK");
            obj.put("gananciaTotal", 0);
            obj.put("porCobrar", 0);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        AsyncHttpClient client = new AsyncHttpClient();
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity(obj.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        client.post(null, url, entity, "application/json", new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                if (response.contains("Ok")){
                    //llamo a login para que cree la session
                    login();
                }else{
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
        //*****************  Lo redirecciono al MainActivity ********************//

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {onSignupSuccess();progressDialog.dismiss(); }
                }, 3000);

    }

    public void login(){
        //token
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                String value = getIntent().getExtras().getString(key);
            }
        }
        //adding token
        String token = FirebaseInstanceId.getInstance().getToken();

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        /*****************  Consulta a BD si existe el user ********************/
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/Login";
        JSONObject obj = new JSONObject();
        try {
            obj.put("correo", email);
            obj.put("password", password);
            obj.put("deviceId", token);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        AsyncHttpClient client = new AsyncHttpClient();
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity(obj.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        client.post(null, url, entity, "application/json", new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                if (response.contains("Ok")){
                    Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(homeIntent);
                }else{
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void onSignupSuccess() {
        Button signupButton = (Button) findViewById(R.id.btn_signup);
        signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "La creación de cuenta ha fallado", Toast.LENGTH_LONG).show();
        Button signupButton = (Button) findViewById(R.id.btn_signup);
        signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = nameText.getText().toString();
        String LastName = LastNameText.getText().toString();
        String ciudad = ciudadText.getText().toString();
        String address = addressText.getText().toString();
        String email = emailText.getText().toString();
        String mobile = mobileText.getText().toString();
        String password = passwordText.getText().toString();
        String reEnterPassword = reEnterPasswordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            nameText.setError("Al menos 3 caracteres");
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (LastName.isEmpty() || LastName.length() < 3) {
            LastNameText.setError("Al menos 3 caracteres");
            valid = false;
        } else {
            LastNameText.setError(null);
        }

        if (ciudad.isEmpty() || ciudad.length() < 3) {
            ciudadText.setError("Al menos 4 caracteres");
            valid = false;
        } else {
            ciudadText.setError(null);
        }

        if (address.isEmpty()) {
            addressText.setError("Ingrese una contraseña válida");
            valid = false;
        } else {
            addressText.setError(null);
        }


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Ingrese un email válido");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (mobile.isEmpty() || mobile.length()!=9) {
            mobileText.setError("Ingrese un número de celular válido");
            valid = false;
        } else {
            mobileText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError("La contraseña debe ser de entre 4 y 10 caracteres alfanuméricos");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            reEnterPasswordText.setError("Las contraseñas no coinciden");
            valid = false;
        } else {
            reEnterPasswordText.setError(null);
        }

        return valid;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
