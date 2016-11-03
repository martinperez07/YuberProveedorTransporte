package yuber.yuber.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import org.json.JSONObject;

import yuber.yuber.R;

public class Intro extends AppCompatActivity {


    private String Ip = "54.191.204.230";
    private String Puerto = "8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                String value = getIntent().getExtras().getString(key);
            }
        }
        String token = FirebaseInstanceId.getInstance().getToken();
        TengoSession(token);

    }

    public void TengoSession(String token){
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/ValidarSesion/" + token;
        JSONObject obj = new JSONObject();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestHandle Rq = client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                if (response.contains("ERROR")){
                    cambiarALogin();
                }else{
                    //guardo token y email

                    cambiarAHome();
                }
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Unexpected Error occured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void cambiarAHome(){
        Intent homeIntent = new Intent(getApplicationContext(), MapActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public void cambiarALogin(){
        Intent homeIntent = new Intent(getApplicationContext(), LoginActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

}
