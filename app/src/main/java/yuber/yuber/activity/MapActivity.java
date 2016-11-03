package yuber.yuber.activity;

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

import yuber.yuber.R;

public class MapActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener  {

    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;
    private String Ip = "54.191.204.230";
    private String Puerto = "8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //borro toda la pila de actividades
     /*   Intent goToMainActivity = new Intent(getApplicationContext(), MapActivity.class);
        goToMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Will clear out your activity history stack till now
        startActivity(goToMainActivity);
*/
        setContentView(R.layout.activity_map);

        //Prendo el temporizador
     //   temporizadorProv temp = new temporizadorProv();
      //  temp.run(true);

        //ADDED FOR TOOLBAR
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        // display the first navigation drawer view on app launch
        displayView(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
    //        CerrarSesion();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
/*
    public void CerrarSesion(String email){
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/Logout";
        JSONObject obj = new JSONObject();
        try {
            obj.put("correo", email);
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

                }else{
                    prgDialogCargando.hide();
                    Toast.makeText(getApplicationContext(), "El usuario y/o contraseña son incorrectos", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                prgDialogCargando.hide();
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
*/
    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    private void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                title = getString(R.string.title_home);
                break;
            case 1:
                fragment = new MpFragment();
                title = getString(R.string.title_map);
                break;
            case 2:
                fragment = new HistoricFragment();
                title = getString(R.string.title_historic);
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

}
