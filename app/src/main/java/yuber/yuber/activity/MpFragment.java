package yuber.yuber.activity;

/**
 * Created by Agustin on 20-Oct-16.
 */

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
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
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import yuber.yuber.R;

public class MpFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener {

    MapView mMapView;
    private GoogleMap googleMap;
    private static int REQUEST_LOCATION;

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private Marker mDestinationMarker;

    private final int[] MAP_TYPES = {GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE}; /// NO NECESARIO SE PUEDE SACAR YA QUE NO INTERESA LA FORMA DEL TERRENO
    private int curMapTypeIndex = 1;

    //Elementos del UI
    private Switch switchGPS;
    private TextView textoUbicacionOrigen;
    private TextView textoUbicacionDestino;
    private Button buttonLlammarUber;

    private enum state {ELIGIENDO_ORIGEN, LLAMANDO_YUBER, ELIGIENDO_DESTINO, DESTINO_ELEGIDO}

    ;
    private state mActualState;
    private Fragment actualFragment = null;

    // Progress Dialog Object
    ProgressDialog prgDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflat and return the layout
        View v = inflater.inflate(R.layout.fragment_mp, container,
                false);


        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


        //SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
        //       .findFragmentById(R.id.mapView);
        mMapView.getMapAsync(this);


        buttonLlammarUber = (Button) v.findViewById(R.id.callYuberButton);
        mActualState = state.ELIGIENDO_ORIGEN;
        displayView(mActualState);

        //seteando listener en boton
        buttonLlammarUber.setOnClickListener(createListenerBottomButton());


        //seteando listener en boton
        Button botonOK = (Button) v.findViewById(R.id.button3);
        ;
        botonOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                new FragmentDialogFinViaje().show(fragmentManager, "FragmentDialogFinViaje");
            }
        });

        // PARA TESTING... SEGURAMENTE SIN USO FUTURO, PODRIA SER ELIMINADO O REUSADO EN OTRO CODIGO
        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(getActivity());
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);



      //  switchGPS = (Switch) actualFragment.getView().findViewById(R.id.switchLocalization);

/*
        // EVENTO ASOCIADO AL SWITCH
        switchGPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                 @Override
                                                 public void onCheckedChanged(CompoundButton cb, boolean on) {
                                                     if (on) {
                                                         //LA JODA DEL PEDIDO DE PERMISO
                                                         Location myLocation = null;
                                                         if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                                                                 != PackageManager.PERMISSION_GRANTED) {
                                                             // Check Permissions Now
                                                             ActivityCompat.requestPermissions(getActivity(),
                                                                     new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                                     REQUEST_LOCATION);
                                                         } else {
                                                             // permission has been granted, continue as usual
                                                             myLocation =
                                                                     LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                                                         }
                                                         if (myLocation != null){
                                                             LatLng myLatLng = new LatLng( myLocation.getLatitude(), myLocation.getLongitude());
                                                             googleMap.addMarker(new MarkerOptions().position(myLatLng).title("Ubicacion actual"));
                                                             googleMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));

                                                         }


                                                         //Do something when Switch button is on/checked
                                                         textoUbicacionOrigen.setText("Tu ubicacion actual");
                                                     } else {
                                                         //Do something when Switch is off/unchecked
                                                         textoUbicacionOrigen.setText("Ubicacion del GPS... no funciona");
                                                     }
                                                 }
        });
*/



        // Perform any camera updates here
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

            // Check Permissions Now
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
        else {
            // permission has been granted, continue as usual
            myLocation =
                    LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        // Add a marker in Montevideo and move the camera
        if (myLocation == null){
            myLocatLatLng = mdeoLatLng;
        }
        else{
            myLocatLatLng = new LatLng( myLocation.getLatitude(), myLocation.getLongitude());
        }

        /*



        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);



        Location location = locationManager.getLastKnownLocation(provider);

        if(location!=null){
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(provider, 20000, 0, this);
*/
      //  googleMap.addMarker(new MarkerOptions().position(myLocatLatLng).title("Ubicacion actual"));
       // googleMap.moveCamera(CameraUpdateFactory.newLatLng(myLocatLatLng));

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
        googleMap.setOnMapLongClickListener(this);
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

    private void mostrarViajeFinalizado(){
        FragmentManager fragmentManager = getFragmentManager();
        new FragmentDialogFinViaje().show(fragmentManager, "FragmentDialogFinViaje");
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

        //marker inicial
        mDestinationMarker = googleMap.addMarker(new MarkerOptions().position(myActualLatLng).title(getAddressFromLatLng(myActualLatLng)));

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), null);

        googleMap.setMapType(MAP_TYPES[curMapTypeIndex]);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
        MarkerOptions options;
        switch (mActualState) {
            case ELIGIENDO_ORIGEN:
                switchGPS = (Switch) actualFragment.getView().findViewById(R.id.switchLocalization);
                textoUbicacionOrigen = (TextView) actualFragment.getView().findViewById(R.id.textUbicacionOrigen);
                switchGPS.setChecked(false);
                if (mDestinationMarker != null)
                    mDestinationMarker.remove();
                options = new MarkerOptions().position(latLng);
                options.title(getAddressFromLatLng(latLng));
                options.icon(BitmapDescriptorFactory.defaultMarker());
                mDestinationMarker = googleMap.addMarker(options);
                textoUbicacionOrigen.setText(getAddressFromLatLng(latLng));
                break;
            case ELIGIENDO_DESTINO:
                //ELEGIR DESTINO /// AGREGAR CODIGO
                textoUbicacionDestino = (TextView) actualFragment.getView().findViewById(R.id.textUbicacionDestino);
                if (mDestinationMarker != null)
                    mDestinationMarker.remove();
                options = new MarkerOptions().position(latLng);
                options.title(getAddressFromLatLng(latLng));
                options.icon(BitmapDescriptorFactory.defaultMarker());
                mDestinationMarker = googleMap.addMarker(options);
                textoUbicacionDestino.setText(getAddressFromLatLng(latLng));

                break;
            default:
                break;
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        MarkerOptions options = new MarkerOptions().position( latLng );
        options.title( getAddressFromLatLng( latLng ) );

        options.icon( BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource( getResources(),
                        R.mipmap.ic_launcher ) ) );

        googleMap.addMarker( options );
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


    private void displayView(state estado) {
        switch (estado) {
            case ELIGIENDO_ORIGEN:
                actualFragment = new MapCallYuberFragment();
                break;
            case LLAMANDO_YUBER:
                actualFragment = new MapWaitYFragment();
                break;
            case ELIGIENDO_DESTINO:
                //ELEGIR DESTINO /// AGREGAR CODIGO
                //mActualState = state.ELIGIENDO_ORIGEN;
                actualFragment = new MapYubConfirmadoFragment();
                if (mDestinationMarker != null)
                    mDestinationMarker.remove();
                mDestinationMarker = null;

                break;
            default:
                break;
        }
        if (actualFragment != null) {
            FragmentManager fragmentManager = getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.FlashBarLayout, actualFragment);
            fragmentTransaction.commit();
        }
    }


    private View.OnClickListener createListenerBottomButton(){
        View.OnClickListener clickListtener = new View.OnClickListener() {
            public void onClick(View v) {
                // Estados del boton en funcion de los clicks
                switch (mActualState) {
                    case ELIGIENDO_ORIGEN:
                        mActualState = state.LLAMANDO_YUBER;
                        displayView(mActualState);
                        buttonLlammarUber.setText("CANCELAR YUBER");
                        break;
                    case LLAMANDO_YUBER:
                        mActualState = state.ELIGIENDO_ORIGEN;
                        displayView(mActualState);
                        if (mDestinationMarker != null)
                            mDestinationMarker.remove();
                        buttonLlammarUber.setText("SOLICITAR YUBER");
                        break;
                    case ELIGIENDO_DESTINO:
                        //ELEGIR DESTINO /// AGREGAR CODIGO
                        if (mDestinationMarker != null) { //.isVisible())

                            mActualState = state.DESTINO_ELEGIDO;
                            buttonLlammarUber.setEnabled(false);
                        } else
                            Toast.makeText(getActivity().getApplicationContext(), "Por favor, elija un destino", Toast.LENGTH_LONG).show();


                        break;
                    default:
                        break;
                }
            }
        };
        return clickListtener;
    }

    /**
     * Method gets triggered when Login button is clicked
     *
     * @param view
     */
    public void loginUser(View view){
        //under button properties
        //android:onClick="loginUser"

        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();
        // When Email Edit View and Password Edit View have values other than Null

        params.put("lat", "43"); //http://api.geonames.org/findNearByWeatherJSON?lat=43&lng=-2&username=demo
        params.put("lng", "-2");
        params.put("username", "demo");
       /*
        // Put Http parameter username with value of Email Edit View control
        params.put("username", email);
        // Put Http parameter password with value of Password Edit Value control
        params.put("password", password);
        */
        // Invoke RESTful Web Service with Http parameters
        invokeWS(params);

    }

    /**
     * Method that performs RESTful webservice invocations
     *
     * @param params
     */
    public void invokeWS(RequestParams params){
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        //  SyncHttpClient client = new SyncHttpClient();

        AsyncHttpClient client = new AsyncHttpClient();
        //client.get("http://api.geonames.org/findNearByWeatherJSON?",params ,new AsyncHttpResponseHandler() {
        client.get("http://api.geonames.org/findNearByWeatherJSON?",params ,new AsyncHttpResponseHandler() {
            // ANTERIOR
            // client.get("http://192.168.2.2:9999/useraccount/login/dologin",params ,new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(String response) {
                // Hide Progress Dialog
                prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    Boolean funcionaWS = true;
                    if (obj.has("status"))
                        funcionaWS = obj.get("status").toString().contains("been exceeded");
                    else
                        funcionaWS = obj.has("weatherObservation");

                    // When the JSON response has status boolean value assigned with true
                    if( funcionaWS){ //|| obj.getString()
                        Toast.makeText(getActivity().getApplicationContext(), "Se conecto con el WS!", Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                        mActualState = state.ELIGIENDO_DESTINO;
                        buttonLlammarUber.setText("ELEGIR DESTINO");
                        displayView(state.ELIGIENDO_DESTINO);
                    }
                    // Else display error message
                    else{
                        //errorMsg.setText(obj.getString("error_msg"));
                        //Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                        Toast.makeText(getActivity().getApplicationContext(), obj.toString(), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getActivity().getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }
            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Throwable error,
                                  String content) {
                // Hide Progress Dialog
                prgDialog.hide();
                // When Http response code is '404'
                if(statusCode == 404){
                    Toast.makeText(getActivity().getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if(statusCode == 500){
                    Toast.makeText(getActivity().getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else{
                    Toast.makeText(getActivity().getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
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
}