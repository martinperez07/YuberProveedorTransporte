package yuber.yuber.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Geocoder;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.IOException;

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
    private Marker mDestinationMarker;

    public static final String ACTION_INTENT_ACEPTAR_RECHAZAR = "MpFragment.action.ACEPTAR_RECHAZAR";

    private Switch JornadaActiva;

    private enum estado_app {YENDO_CLIENTE, ESPERANDO_CLIENTE, EN_VIAJE};
    private estado_app estado = estado_app.ESPERANDO_CLIENTE;
    private Fragment actualFragment = null;

    ProgressDialog prgDialog;
    Fragment topFragment = null;

    private String Ip = "54.213.51.6";
    private String Puerto = "8080";
    //papa frita

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
        // filter.addAction(ACTION_INTENT2);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(ActivityDataReceiver, filter);

        return v;
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
        //improvisacion para ver si anda con ubicacio inventada
        LatLng myActualLatLng = new LatLng( location.getLatitude(),location.getLongitude() );//new LatLng(-34.9, -56.16);
        //
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

    private String getAddressFromLatLng( LatLng latLng ) {
        Geocoder geocoder = new Geocoder( getActivity() );
        String address = "";
        try {
            address =geocoder
                    .getFromLocation( latLng.latitude, latLng.longitude, 1 )
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
        if(ACTION_INTENT_ACEPTAR_RECHAZAR.equals(intent.getAction()) ){
            String jsonUsuario = intent.getStringExtra("DATOS_USUARIOS");
            mostrarDialAceptarRechazar(jsonUsuario);
        }
        }
    };

    private void mostrarDialAceptarRechazar(String JUser){
        Bundle args = new Bundle();
        args.putString("DatosUsuario", JUser);
        FragmentDialogYuberAceptarRechazar newFragmentDialog = new FragmentDialogYuberAceptarRechazar();
        newFragmentDialog.setArguments(args);
        newFragmentDialog.show(getActivity().getSupportFragmentManager(), "TAG");
    }

    public void actualizarCoordenadas(String email) {
        if (mCurrentLocation != null){
            String latitud = String.valueOf(mCurrentLocation.getLatitude());
            String longitud = String.valueOf(mCurrentLocation.getLongitude());
            //OBTENGO MIS COORDENADAS Y LAS PASO A STRING
            //REGISTRO EN LA BD
            String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/ActualizarCoordenadas/" + email + "," + latitud + "," + longitud;
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