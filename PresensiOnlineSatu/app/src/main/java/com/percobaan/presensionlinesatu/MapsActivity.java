package com.percobaan.presensionlinesatu;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


public class MapsActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        ResultCallback<Status> {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final long DOUBLE_PRESS_INTERVAL = 500000000/* some value in ns. */;
    private long lastPressTime;
    private long waktu_jeda, waktu_monitor;

    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    private TextView textLat, textLong, textKet, textNama, textLatGeo, textLonGeo, textStatus;

    private MapFragment mapFragment;

//    private LocationManager locationManager=null;
//    private LocationListener locationListener=null;

    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";
    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent( context, MapsActivity.class );
        intent.putExtra( NOTIFICATION_MSG, msg );
        return intent;
    }

    Button button;
    String nama_pegawai, status;
    int i;

    //Broadcast
    private MyBroadcastReceiver myBroadcastReceiver;
    String lokasi_pegawai;
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String update = intent.getStringExtra("EXTRA_UPDATE");
            if (update.equals("masuk")) {

                //Setting button check in/out
                button.setVisibility(View.VISIBLE);
                if (status.equals("1")){
                    button.setText("OUT");
                }
                else if (status.equals("0")){
                    button.setText("IN");
                }
                lokasi_pegawai="didalam";

                //Save action when entering geofence while check in
                if (status.equals("1")){
                    String keterangan = "Masuk Kembali";
                    String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    BackgroundInsertCekIn backgroundInsertCekIn = new BackgroundInsertCekIn();
                    backgroundInsertCekIn.execute(keterangan, id);
                }

                waktu_jeda=0;
            }else if (update.equals("keluar")) {
                button.setVisibility(View.INVISIBLE);
                lokasi_pegawai="diluar";

                //Save action when out from geofence while check in
                if (status.equals("1")){
                    String keterangan = "Mulai Keluar Kantor";
                    String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    BackgroundInsertCekIn backgroundInsertCekIn = new BackgroundInsertCekIn();
                    backgroundInsertCekIn.execute(keterangan, id);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        textLat = (TextView) findViewById(R.id.lat);
        textLong = (TextView) findViewById(R.id.lon);
        textKet = (TextView) findViewById(R.id.fake);
        textNama = (TextView) findViewById(R.id.nama);
        textLatGeo = (TextView) findViewById(R.id.latGeo);
        textLonGeo = (TextView) findViewById(R.id.lonGeo);
        textStatus = (TextView) findViewById(R.id.status);
        waktu_jeda = 0;
        waktu_monitor=0;


        // initialize GoogleMaps
        initGMaps();

        // create GoogleApiClient
        createGoogleApi();
        button = (Button) findViewById(R.id.button);
        button.setVisibility(View.INVISIBLE);
        //button.setEnabled(false);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String keterangan;
                String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                if (status.equals("1")){
                    keterangan="Cek Out";
                    status="0";
                    BackgroundInsertCekIn backgroundInsertCekIn = new BackgroundInsertCekIn();
                    backgroundInsertCekIn.execute(keterangan, id);


                    button.setText("OUT");
                } else if (status.equals("0")){
                    keterangan="Cek In";
                    status="1";
                    BackgroundInsertCekIn backgroundInsertCekIn = new BackgroundInsertCekIn();
                    backgroundInsertCekIn.execute(keterangan, id);


                    button.setText("IN");
                }



            }
        });


        //Broadcast
        myBroadcastReceiver = new MyBroadcastReceiver();

        IntentFilter intentFilter = new IntentFilter("com.percobaan.presensionlinesatu.UPDATE");
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(myBroadcastReceiver, intentFilter);

/*
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (status.equals("1")) {
                        String keterangan = "Monitoring";
                        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                        BackgroundInsertCekIn backgroundInsertCekIn = new BackgroundInsertCekIn();
                        backgroundInsertCekIn.execute(keterangan, id);
                    }
                }
            }, 60000);
*/

        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                if (status.equals("1")) {
                    String keterangan = "Monitoring";
                    String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    BackgroundInsertCekIn backgroundInsertCekIn = new BackgroundInsertCekIn();
                    backgroundInsertCekIn.execute(keterangan, id);
                }
                start();
            }
        }.start();

    }


    //Get data from database using JSON and start geofence
    private class BackgroundLoadData extends AsyncTask<String, Void, String> {

        Context context;
        String JSON_URL, JSON_STRING;
        Double lat_geo, lon_geo;

        @Override
        protected void onPreExecute() {
            JSON_URL = "https://sword-shaped-splint.000webhostapp.com/android/lokasi.php";
        }

        @Override
        protected String doInBackground(String... params) {
            String type = params[0];
            if (type.equals("register")) {
                try {
                    String id = params[1];
                    URL url = new URL(JSON_URL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);

                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("id_device","UTF-8")+"="+URLEncoder.encode(id,"UTF-8");
                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();


                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((JSON_STRING = bufferedReader.readLine()) != null) {

                        stringBuilder.append(JSON_STRING + "\n");

                    }

                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();
                    return stringBuilder.toString().trim();


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            JSON_STRING = result;

            if (JSON_STRING.equals("GAGAL")){
                Toast.makeText(getApplicationContext(), "Maaf No Pegawai Tidak DIkenal", Toast.LENGTH_LONG).show();
            }
            else {
                try {
                    JSONObject jsonObject = new JSONObject(JSON_STRING);
                    JSONArray data = jsonObject.getJSONArray("hasil data");

                    JSONObject JO = data.getJSONObject(0);
                    nama_pegawai = JO.getString("nama_pegawai");
                    //id = JO.getString("id_device");
                    lat_geo = JO.getDouble("lat_kantor");
                    lon_geo = JO.getDouble("lon_kantor");
                    status = JO.getString("status");
                    textNama.setText( "Nama : " + nama_pegawai);
                    textLatGeo.setText( "LatGeo : " + lat_geo);
                    textLonGeo.setText( "LonGeo : " + lon_geo);
                    textStatus.setText( "Status : " + status);

                    markerForGeofence(new LatLng(lat_geo,lon_geo));
                    startGeofence();


                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }

        }

    }



    // Create GoogleApiClient instance
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if ( googleApiClient == null ) {
            googleApiClient = new GoogleApiClient.Builder( this )
                    .addConnectionCallbacks( this )
                    .addOnConnectionFailedListener( this )
                    .addApi( LocationServices.API )
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Call GoogleApiClient connection when starting the Activity
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.main_menu, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
            case R.id.geofence: {
                startGeofence();
                return true;
            }
            case R.id.clear: {
                clearGeofence();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
*/
    @Override
    public void onBackPressed() {
        long pressTime = System.nanoTime();
        if(pressTime - lastPressTime <= DOUBLE_PRESS_INTERVAL) {
            // this is a double click event
            super.onBackPressed();
            if (status.equals("1")) {
                String keterangan = "Perangkat Dimatikan";
                String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                BackgroundInsertCekIn backgroundInsertCekIn = new BackgroundInsertCekIn();
                backgroundInsertCekIn.execute(keterangan, id);
            }
            finishAffinity();
        }
        else {
            Toast.makeText(getApplicationContext(), "Tekan BACK 2x untuk keluar dan HOME untuk minimize", Toast.LENGTH_LONG).show();

        }
        lastPressTime = pressTime;

    }

    private final int REQ_PERMISSION = 999;

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }

    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                REQ_PERMISSION
        );
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch ( requestCode ) {
            case REQ_PERMISSION: {
                if ( grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    // Permission granted
                    getLastKnownLocation();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    // App cannot work without the permissions
    private void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
        // TODO close app and warn user
    }

    // Initialize GoogleMaps
    private void initGMaps(){
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // Callback called when Map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");
        map = googleMap;
        UiSettings config = map.getUiSettings();
        config.setMapToolbarEnabled(false);
        config.setZoomControlsEnabled(false);
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);

        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String type = "register";

        BackgroundLoadData backgroundLoadData = new BackgroundLoadData();
        backgroundLoadData.execute(type, id);


    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick("+latLng +")");
        //markerForGeofence(latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClickListener: " + marker.getPosition() );
        return false;
    }

    private LocationRequest locationRequest;
    // Defined in mili seconds.
    // This number in extremely low, and should be used only for debug
    private final int UPDATE_INTERVAL =  300;
    private final int FASTEST_INTERVAL = 100;

    // Start location Updates
    private void startLocationUpdates(){
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if ( checkPermission() )
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged ["+location+"]");
        lastLocation = location;
        writeActualLocation(location);
        if (status.equals("1")) {
            if (lokasi_pegawai != null && lokasi_pegawai.equals("diluar")) {
                waktu_jeda=waktu_jeda+1;
                if (waktu_jeda == 5){
                    String keterangan = "Diluar Kantor";
                    String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    BackgroundInsertCekIn backgroundInsertCekIn = new BackgroundInsertCekIn();
                    backgroundInsertCekIn.execute(keterangan, id);
                    waktu_jeda=0;

                }
            }

        }

    }

    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        getLastKnownLocation();
        recoverGeofenceMarker();
    }

    // GoogleApiClient.ConnectionCallbacks suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    // GoogleApiClient.OnConnectionFailedListener fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }

    // Get last known location
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if ( checkPermission() ) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if ( lastLocation != null ) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                writeLastLocation();
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        }
        else askPermission();
    }

    private void writeActualLocation(Location location) {
        textLat.setText( "Lat: " + location.getLatitude() );
        textLong.setText( "Long: " + location.getLongitude() );
        textKet.setText( "Fake : " + location.isFromMockProvider());

        // untuk cek fake
        if (location.isFromMockProvider()==true){
            Toast.makeText(getApplicationContext(), "Anda Fake", Toast.LENGTH_LONG).show();
            button.setVisibility(View.INVISIBLE);
        }


        markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));

    }

    private void writeLastLocation() {
        writeActualLocation(lastLocation);
    }

    private Marker locationMarker;
    private void markerLocation(LatLng latLng) {
        Log.i(TAG, "markerLocation("+latLng+")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        if ( map!=null ) {
            if ( locationMarker != null )
                locationMarker.remove();
            locationMarker = map.addMarker(markerOptions);
            float zoom = 17f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            map.animateCamera(cameraUpdate);
        }
    }


    private Marker geoFenceMarker;
    private void markerForGeofence(LatLng latLng) {
        Log.i(TAG, "markerForGeofence("+latLng+")");
        String title = latLng.latitude + ", " + latLng.longitude;
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title);
        if ( map!=null ) {
            // Remove last geoFenceMarker
            if (geoFenceMarker != null)
                geoFenceMarker.remove();

            geoFenceMarker = map.addMarker(markerOptions);

        }
    }

    // Start Geofence creation process
    private void startGeofence() {
        Log.i(TAG, "startGeofence()");
        if( geoFenceMarker != null ) {
            Geofence geofence = createGeofence( geoFenceMarker.getPosition(), GEOFENCE_RADIUS );
            GeofencingRequest geofenceRequest = createGeofenceRequest( geofence );
            addGeofence( geofenceRequest );
        } else {
            Log.e(TAG, "Geofence marker is null");
        }
    }

    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 50.0f; // in meters

    // Create a Geofence
    private Geofence createGeofence( LatLng latLng, float radius ) {
        Log.d(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion( latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration( GEO_DURATION )
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest( Geofence geofence ) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofence( geofence )
                .build();
    }

    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;
    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if ( geoFencePendingIntent != null )
            return geoFencePendingIntent;

        Intent intent = new Intent( this, GeofenceTrasitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if ( status.isSuccess() ) {
            saveGeofence();
            drawGeofence();
        } else {
            // inform about fail
        }
    }

    // Draw Geofence circle on GoogleMap
    private Circle geoFenceLimits;
    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()");

        if ( geoFenceLimits != null )
            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center( geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70,70,70))
                .fillColor( Color.argb(100, 150,150,150) )
                .radius( GEOFENCE_RADIUS );
        geoFenceLimits = map.addCircle( circleOptions );
    }

    private final String KEY_GEOFENCE_LAT = "GEOFENCE LATITUDE";
    private final String KEY_GEOFENCE_LON = "GEOFENCE LONGITUDE";

    // Saving GeoFence marker with prefs mng
    private void saveGeofence() {
        Log.d(TAG, "saveGeofence()");
        SharedPreferences sharedPref = getPreferences( Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putLong( KEY_GEOFENCE_LAT, Double.doubleToRawLongBits( geoFenceMarker.getPosition().latitude ));
        editor.putLong( KEY_GEOFENCE_LON, Double.doubleToRawLongBits( geoFenceMarker.getPosition().longitude ));
        editor.apply();
    }

    // Recovering last Geofence marker
    private void recoverGeofenceMarker() {
        Log.d(TAG, "recoverGeofenceMarker");
        SharedPreferences sharedPref = getPreferences( Context.MODE_PRIVATE );

        if ( sharedPref.contains( KEY_GEOFENCE_LAT ) && sharedPref.contains( KEY_GEOFENCE_LON )) {
            double lat = Double.longBitsToDouble( sharedPref.getLong( KEY_GEOFENCE_LAT, -1 ));
            double lon = Double.longBitsToDouble( sharedPref.getLong( KEY_GEOFENCE_LON, -1 ));
            LatLng latLng = new LatLng( lat, lon );
            markerForGeofence(latLng);
            drawGeofence();
        }
    }

    // Clear Geofence
    private void clearGeofence() {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                googleApiClient,
                createGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if ( status.isSuccess() ) {
                    // remove drawing
                    removeGeofenceDraw();
                }
            }
        });
    }

    private void removeGeofenceDraw() {
        Log.d(TAG, "removeGeofenceDraw()");
        if ( geoFenceMarker != null)
            geoFenceMarker.remove();
        if ( geoFenceLimits != null )
            geoFenceLimits.remove();
    }

    // Save Aktivitas Pegawai
    private class BackgroundInsertCekIn extends AsyncTask <String, Void, String> {

        String DATA_URL;
        Double l1=lastLocation.getLatitude();
        Double l2=lastLocation.getLongitude();
        String coordlat = l1.toString();
        String coordlon = l2.toString();
        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        @Override
        protected void onPreExecute() {
            DATA_URL = "https://sword-shaped-splint.000webhostapp.com/android/simpanaktivitas.php";
            //DATA_URL = "http://35.187.247.224/simpanaktivitas.php";
        }

        public void showToast(String message) {
            final String msg = message;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                }
            });
        }


        @Override
        protected String doInBackground(String... params) {
            String keterangan = params[0];
            String id = params [1];
            try {
                URL url =  new URL(DATA_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);


                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data1 = URLEncoder.encode("keterangan","UTF-8")+"="+URLEncoder.encode(keterangan,"UTF-8");
                post_data1 += "&"+URLEncoder.encode("id_device","UTF-8")+"="+URLEncoder.encode(id,"UTF-8");
                post_data1 += "&"+URLEncoder.encode("lat_cek","UTF-8")+"="+URLEncoder.encode(coordlat,"UTF-8");
                post_data1 += "&"+URLEncoder.encode("lon_cek","UTF-8")+"="+URLEncoder.encode(coordlon,"UTF-8");
                bufferedWriter.write(post_data1);
                bufferedWriter.flush();
                bufferedWriter.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String result = "";
                String line = "";
                while ((line=bufferedReader.readLine()) != null) {
                    result += line;

                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return result;



            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {

            showToast(result);

            BackgroundUbahStatus backgroundUbahStatus = new BackgroundUbahStatus();
            backgroundUbahStatus.execute(status, id);
        }

    }


    //Change status login pegawai
    private class BackgroundUbahStatus extends AsyncTask <String, Void, String> {

        String DATA_URL;

        @Override
        protected void onPreExecute() {
            DATA_URL = "https://sword-shaped-splint.000webhostapp.com/android/ubahstatus.php";
            //DATA_URL = "http://35.187.247.224/ubahstatus.php";
        }

        public void showToast(String message) {
            final String msg = message;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                }
            });
        }


        @Override
        protected String doInBackground(String... params) {
            String status = params[0];
            String id = params [1];
            try {
                URL url =  new URL(DATA_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);


                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data1 = URLEncoder.encode("status","UTF-8")+"="+URLEncoder.encode(status,"UTF-8");
                post_data1 += "&"+URLEncoder.encode("id_device","UTF-8")+"="+URLEncoder.encode(id,"UTF-8");
                bufferedWriter.write(post_data1);
                bufferedWriter.flush();
                bufferedWriter.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String result = "";
                String line = "";
                while ((line=bufferedReader.readLine()) != null) {
                    result += line;

                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return result;



            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            textStatus.setText( "Status : " + status);
            if (status.equals("1")){
                button.setText("OUT");
            }
            else if (status.equals("0")){
                button.setText("IN");
            }
            showToast(result);
        }

    }





}
