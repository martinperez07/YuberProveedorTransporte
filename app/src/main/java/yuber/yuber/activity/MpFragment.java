package yuber.yuber.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import yuber.yuber.R;

public class MpFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener {

    MapView mMapView;
    private GoogleMap googleMap;
    private static int REQUEST_LOCATION;

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private Marker mOrigenMarker;
    private LatLng mOrigenLatLng;
    private Marker mDestinationMarker;


    private static final String TAG = "MAPA";

    //NOTIFICACIONES
    public static final String ACTION_INTENT_ACEPTAR_RECHAZAR = "MpFragment.action.ACEPTAR_RECHAZAR";
    public static final String ACTION_INTENT_CANCELARON_VIAJE = "MpFragment.action.VIAJE_CANCELADO";
    public static final String ACTION_INTENT_DESTINO_ELEGIDO = "MpFragment.action.DESTINO_ELEGIDO";

    //BOTONES
    public static final String ACTION_PRENDE_INICIAR = "MpFragment.action.PRENDE_INICIAR";
    public static final String ACTION_PRENDE_FIN = "MpFragment.action.PRENDE_FIN";
    public static final String ACTION_APAGA_INICIAR = "MpFragment.action.APAGA_INICIAR";
    public static final String ACTION_APAGA_FIN = "MpFragment.action.APAGA_FIN";
    public static final String ACTION_MARCAR_ORIGEN = "MpFragment.action.MARCAR_ORIGEN";



    private Switch JornadaActiva;

    private enum estado_app {YENDO_CLIENTE, ESPERANDO_CLIENTE, EN_VIAJE};
    private estado_app estado = estado_app.ESPERANDO_CLIENTE;
    private Fragment actualFragment = null;

    ProgressDialog prgDialog;
    Fragment topFragment = null;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String ClienteInstanciaServicioKey = "clienteInstanciaServicioKey";
    public static final String ClienteUbicacionDestinoKey = "ubicacionDestinoKey";
    public static final String EnViaje = "enViaje";
    private SharedPreferences sharedpreferences;
    private String Ip = "54.213.51.6";
    private String Puerto = "8080";

    private Button IniciarViaje;
    private Button FinViaje;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflat and return the layout
        View v = inflater.inflate(R.layout.fragment_mp, container,
                false);

        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        prgDialog = new ProgressDialog(getActivity());
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false);

        topFragment = new MapJornadaActivaFragment();
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.FlashBarLayout, topFragment);
        fragmentTransaction.commit();



        IntentFilter filter = new IntentFilter(ACTION_INTENT_ACEPTAR_RECHAZAR);
        filter.addAction(ACTION_INTENT_CANCELARON_VIAJE);
        filter.addAction(ACTION_INTENT_DESTINO_ELEGIDO);
        filter.addAction(ACTION_PRENDE_INICIAR);
        filter.addAction(ACTION_PRENDE_FIN);
        filter.addAction(ACTION_APAGA_INICIAR);
        filter.addAction(ACTION_APAGA_FIN);
        filter.addAction(ACTION_MARCAR_ORIGEN);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(ActivityDataReceiver, filter);

        IniciarViaje = (Button) v.findViewById(R.id.IniciarViaje);
        IniciarViaje.setOnClickListener(crearBotonIniciar());

        FinViaje = (Button) v.findViewById(R.id.FinViaje);
        FinViaje.setOnClickListener(crearBotonFin());

        sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);

        return v;
    }

    public void ocultarIV(){
        IniciarViaje.setVisibility(View.GONE);
    }

    public void ocultarFV(){
        FinViaje.setVisibility(View.GONE);
    }

    public void mostrarIV(){
        IniciarViaje.setVisibility(View.VISIBLE);
    }

    public void mostrarFV(){
        FinViaje.setVisibility(View.VISIBLE);
    }



    private View.OnClickListener crearBotonIniciar(){
        View.OnClickListener clickListtener = new View.OnClickListener() {
            public void onClick(View v) {
                //Se llama a comenzar viaje
                comenzarViaje();
                //Cambio de botones
                ocultarIV();
                mostrarFV();
            }
        };
        return clickListtener;
    }

    private View.OnClickListener crearBotonFin(){
        View.OnClickListener clickListtener = new View.OnClickListener() {
            public void onClick(View v) {
                //Se llama a fin servicio
                terminarViaje();
                ocultarFV();
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(EnViaje, "false");
                editor.commit();
                //Se llama a calificar cliente
                mostrarDialCalificacion();
            }
        };
        return clickListtener;
    }

    public void comenzarViaje(){
        String instanciaID = sharedpreferences.getString(ClienteInstanciaServicioKey, "");
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/IniciarServicio/" + instanciaID;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                if(statusCode == 404){
                    Toast.makeText(getActivity().getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getActivity().getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "Unexpected Error occured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void terminarViaje(){
  //      SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String instanciaID = sharedpreferences.getString(ClienteInstanciaServicioKey, "");

        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/FinServicio/" + instanciaID + ",5";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                if(statusCode == 404){
                    Toast.makeText(getActivity().getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getActivity().getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "Unexpected Error occured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMapParam) {
        googleMap = googleMapParam;
        LatLng myLocatLatLng;
        LatLng mdeoLatLng = new LatLng(-34, -56);
        Location myLocation = null;

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        if (myLocation == null){
            myLocatLatLng = mdeoLatLng;
        } else{
            myLocatLatLng = new LatLng( myLocation.getLatitude(), myLocation.getLongitude());
        }
        initListeners();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void initListeners() {
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setOnMapClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            // permission has been granted, continue as usual
            Location myLocation =
                    LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        mCurrentLocation = LocationServices
                .FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        initCamera(mCurrentLocation);
    }

    @Override
    public void onConnectionSuspended(int i) {
        //handle play services disconnecting if location is being constantly used
    }

    private void initCamera(Location location) {
        LatLng myActualLatLng;
        //si no esta el GPS prendido va a la ubicacion (-34.9, -56.16)
        if(location!= null)
            myActualLatLng = new LatLng( location.getLatitude(),location.getLongitude() );
        else
            myActualLatLng = new LatLng(-34.9, -56.16);

        CameraPosition position = CameraPosition.builder()
                .target(myActualLatLng)
                .zoom(16f)
                .bearing(0.0f)
                .tilt(0.0f)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), null);

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled( true );
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Create a default location if the Google API Client fails. Placing location at Googleplex
        mCurrentLocation = new Location( "" );
        mCurrentLocation.setLatitude( -34.9 );
        mCurrentLocation.setLongitude( -56.16 );
        initCamera(mCurrentLocation);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText( getActivity(), "Clicked on marker", Toast.LENGTH_SHORT ).show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    protected BroadcastReceiver ActivityDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Adentro de BROADCASTER: " + intent.getAction());
            if(ACTION_INTENT_ACEPTAR_RECHAZAR.equals(intent.getAction()) ){
                String jsonUsuario = intent.getStringExtra("DATOS_USUARIOS");
                double latitud = -34.9133764;
                double longitud = -56.1690546;
                try {
                    JSONObject datosUsuario = new JSONObject(jsonUsuario);
                    JSONObject datosOrigenUsuario = new JSONObject(datosUsuario.getString("ubicacion"));
                    latitud = datosOrigenUsuario.getDouble("latitud");
                    longitud = datosOrigenUsuario.getDouble("longitud");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mOrigenLatLng = new LatLng(latitud,longitud);
                mostrarDialAceptarRechazar(jsonUsuario);
            }else if(ACTION_INTENT_CANCELARON_VIAJE.equals(intent.getAction()) ){
                mostrarDialCancelaronViaje();
            }else if(ACTION_INTENT_DESTINO_ELEGIDO.equals(intent.getAction()) ){
                String jsonDestino = intent.getStringExtra("DATOS_DESTINO");
                //PARA QUE LO DE ABAJO? CREO QUE NO ES NECESARIO...
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(ClienteUbicacionDestinoKey, jsonDestino);
                editor.commit();
                //fin innecesariedad?
                ponerMarcadorDestino(jsonDestino);
                //pongo el punto en el mapa y trazo la ruta
                Toast.makeText(getActivity().getApplicationContext(), "pongo el punto en el mapa y trazo la ruta", Toast.LENGTH_LONG).show();
            }else if(ACTION_PRENDE_INICIAR.equals(intent.getAction())) {
                mostrarIV();
                ponerMarcadorOrigen();

            }else if(ACTION_PRENDE_FIN.equals(intent.getAction())) {//TODO eliminar?, se paso a ACTION_APAGA_FIN
                //ponerMarcadorOrigen();
                mostrarFV();

                //VINIENDO DEL DIALOGO RECHAZAR ACEPTAR SOLO ENTRA CON EL SIGUIENTE INTENT
            }else if(ACTION_APAGA_FIN.equals(intent.getAction())) {
                ocultarFV();


            }else if(ACTION_APAGA_INICIAR.equals(intent.getAction())) {
                ocultarIV();

            }else if(ACTION_MARCAR_ORIGEN.equals(intent.getAction())) {// TODO ELIMINAR, SE PASO A ACTION_APAGA_FIN
                ponerMarcadorOrigen();
            }

        }
    };

    private void ponerMarcadorDestino(String jsonDestino) {
        Log.d(TAG, "Adentro de ponerMarcadorDestino" + jsonDestino);
        double latitud = -34.9133764;
        double longitud = -56.1690546;
        try {
            JSONObject datosUsuario = new JSONObject(jsonDestino);
            JSONObject datosOrigenUsuario = new JSONObject(datosUsuario.getString("ubicacion"));
            latitud = datosOrigenUsuario.getDouble("latitud");
            longitud = datosOrigenUsuario.getDouble("longitud");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mOrigenLatLng = new LatLng(latitud,longitud);
    }

    private void eliminarMarcadorOrigen(){
        if (mOrigenMarker != null)
            mOrigenMarker.remove();
    }
    private void ponerMarcadorOrigen() {
        eliminarMarcadorOrigen();
        MarkerOptions options;
        options = new MarkerOptions().position(mOrigenLatLng);
        options.icon(BitmapDescriptorFactory.defaultMarker());
        mOrigenMarker = googleMap.addMarker(options);
        mOrigenMarker.setTitle("Ubicacion cliente");
    }

    private void mostrarDialAceptarRechazar(String JUser){
        Bundle args = new Bundle();
        args.putString("DatosUsuario", JUser);
        FragmentDialogYuberAceptarRechazar newFragmentDialog = new FragmentDialogYuberAceptarRechazar();
        newFragmentDialog.setArguments(args);
        newFragmentDialog.show(getActivity().getSupportFragmentManager(), "TAG");
    }

    private void mostrarDialCancelaronViaje(){
        Bundle args = new Bundle();
        FragmentDialogYuberCancelaronViaje newFragmentDialog = new FragmentDialogYuberCancelaronViaje();
        newFragmentDialog.setArguments(args);
        newFragmentDialog.show(getActivity().getSupportFragmentManager(), "TAG");
    }

    private void mostrarDialCalificacion(){
        Bundle args = new Bundle();
        FragmentDialogYuberCalificar newFragmentDialog = new FragmentDialogYuberCalificar();
        newFragmentDialog.setArguments(args);
        newFragmentDialog.show(getActivity().getSupportFragmentManager(), "TAG");
    }

    public void actualizarCoordenadas(String email) {
        if (mCurrentLocation != null){
            String enViaje = sharedpreferences.getString(EnViaje, "");
            String latitud = String.valueOf(mCurrentLocation.getLatitude());
            String longitud = String.valueOf(mCurrentLocation.getLongitude());
            String instanciaID = "-1";
            if (enViaje.contains("true")) {
                instanciaID = sharedpreferences.getString(ClienteInstanciaServicioKey, "");
                enViaje = "True";
            }else{
                enViaje = "False";
            }
            //OBTENGO MIS COORDENADAS Y LAS PASO A STRING
            //REGISTRO EN LA BD
            String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/ActualizarCoordenadas/" + email + "," + latitud + "," + longitud + "," + enViaje + "," + instanciaID;
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(null, url, new AsyncHttpResponseHandler(){
                @Override
                public void onSuccess(String response) {
                    System.out.println("Actualice las coordenadas!");
                }
                @Override
                public void onFailure(int statusCode, Throwable error, String content){
                }
            });
        }
    }

}