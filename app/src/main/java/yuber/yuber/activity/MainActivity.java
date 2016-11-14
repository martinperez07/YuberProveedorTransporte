package yuber.yuber.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import yuber.yuber.R;

public class MainActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener  {

    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;
    private String Ip = "54.213.51.6";
    private String Puerto = "8080";

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String EmailKey = "emailKey";
    public static final String TokenKey = "tokenKey";
    public static final String EnViaje = "enViaje";
    SharedPreferences sharedpreferences;


    MpFragment mapFragment = new MpFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        //Disparo el thread para actualizar las coordenadas del proveedor
        new Thread(new Runnable() {
            public void run() {
                temporizadorProv temp = new temporizadorProv();
                SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
                String email = sharedpreferences.getString(EmailKey, "");
                while(true) {
                    if (mapFragment != null)
                        mapFragment.actualizarCoordenadas(email);
                    temp.esperarXsegundos(10);
                }
            }
        }).start();

        displayView(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_cerrar_sesion) {
            SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
            String email = sharedpreferences.getString(EmailKey, "");
            String enViaje = sharedpreferences.getString(EnViaje, "");
            if (enViaje.contains("false")) {
                CerrarSesion(email);
            }else {
                Toast.makeText(getApplicationContext(), "No puede cerrar sesion mientras estas brindando un servicio", Toast.LENGTH_LONG).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void CerrarSesion(String email){
        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String enViaje = sharedpreferences.getString(EnViaje, "");
        if (enViaje.contains("false")) {
            String token = sharedpreferences.getString(TokenKey, "");
            MapJornadaActivaFragment m = new MapJornadaActivaFragment();
            m.dejarDeTrabajar(email);
            String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/Logout";
            JSONObject obj = new JSONObject();
            try {
                obj.put("correo", email);
                obj.put("password","");
                obj.put("deviceId",token);
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
            RequestHandle Rq = client.post(null, url, entity, "application/json", new AsyncHttpResponseHandler(){
                @Override
                public void onSuccess(String response) {
                    if (response.contains("true") ){
                        cambiarALogin();
                    }else{
                        Toast.makeText(getApplicationContext(), "No se pudo cerrar la sesión. Vuelva a intentar.", Toast.LENGTH_LONG).show();
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
        }else{
            Toast.makeText(getApplicationContext(), "No puede dejar cerrar la sesión mientras estas brindando un servicio", Toast.LENGTH_LONG).show();
        }
    }

    public void cambiarALogin(){
        Intent homeIntent = new Intent(getApplicationContext(), LoginActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case 0:
                //fragment = new MpFragment();
                fragment = mapFragment;
                title = getString(R.string.title_map);
                break;
            case 1:
                fragment = new HistoricFragment();
                title = getString(R.string.title_historic_viaje);
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();
            // set the toolbar title
            getSupportActionBar().setTitle(title);
        }
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
