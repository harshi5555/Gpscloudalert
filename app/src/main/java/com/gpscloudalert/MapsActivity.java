package com.gpscloudalert;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import static com.gpscloudalert.R.id.map;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMyLocationButtonClickListener, ResultCallback<Status>


{
    private static final String TAG = MapsActivity.class.getSimpleName();

    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 100.0f; // in meters
    private static final int REQ_PERMISSION = 1;
    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";
    private final int UPDATE_INTERVAL = 3 * 60 * 1000;// 3 minutes
    private final int FASTEST_INTERVAL = 30 * 1000;// 30 secs
    private final int GEOFENCE_REQ_CODE = 0;
    public TextView log, lat, dist;
    public double distanceInMeters;
    public double latL;
    public double lon;
    public double geoLat;
    public double geoLon;
    public Marker locationMarker;
    public double distanceMeters;
    protected ArrayList<Geofence> mGeofenceList;
    private Location mLocation;
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private FusedLocationProviderApi fusedLocationProviderApi;
    private Marker geoFenceMarker;
    private PendingIntent geoFencePendingIntent;
    private String distance;
    private RequestQueue requestQueue;
    private Circle geoFenceLimits;
    private double distan;
    private  String address;
    private Location geoLocation;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);




        initialized();
        createGoogleApi();
       // populateGeofenceList();




    }
    // Create a Intent send by the notification
    public static Intent createNotificationIntent(Context context, String msg) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.putExtra(NOTIFICATION_MSG, msg);
        return intent;}

    // initialized textview and array
    private void initialized(){
        log = (TextView) findViewById(R.id.lat);
        lat = (TextView) findViewById(R.id.lont);
        dist = (TextView) findViewById(R.id.distance);
        mGeofenceList = new ArrayList<Geofence>();
        requestQueue = Volley.newRequestQueue(this);

    }


   // create GoogleApi
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setMyLocationEnabled(true);
        LatLng stockholm =new LatLng(latL,lon);
        address = getAddress(this,latL,lon);
        //add marker and the address to the location
        mMap.addMarker(new MarkerOptions().position(stockholm).title(address));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(stockholm));
        startGeofence();


          // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        getLastKnownLocation();
        // startLocationUpdates();


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Call GoogleApiClient connection when starting the Activity
        googleApiClient.connect();
        if (googleApiClient != null) googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        markerForLocation(new LatLng(location.getLatitude(),location.getLongitude()));
        //mMap.addMarker(new MarkerOptions().position(new LatLng(latL,lon)).title("Marker "));
        writeActualLocation(location);

        // startGeofence();


    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if (checkPermission()) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {

                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());

                writeLastLocation();
                startLocationUpdates();


            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        } else askPermission();


    }

    // Start location Updates
    private void startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (checkPermission())

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    // Write location coordinates on UI
    private void writeActualLocation(Location location) {
        markerForLocation(new LatLng(location.getLatitude(), location.getLongitude()));

        log.setText("Lat: " + location.getLatitude());
        lat.setText("Long: " + location.getLongitude());


    }

    private void writeLastLocation() {
        writeActualLocation(lastLocation);
    }

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERMISSION
        );
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
    }

    @Override
    public boolean onMyLocationButtonClick() {

        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick(" + latLng + ")");

        markerForGeofence(latLng);
        drawGeofence();
        startGeofence();
        countDistance(lastLocation,geoLocation);




    }






    // Create a Location Marker
    private void markerForLocation(LatLng latLng) {
        Log.i(TAG, "markerLocation(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        address = getAddress(this,latLng.latitude,latLng.longitude);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(address);
        if (mMap != null) {
            // Remove the anterior marker
            if (locationMarker != null)
                locationMarker.remove();
            locationMarker = mMap.addMarker(markerOptions);
            float zoom = 14f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            mMap.animateCamera(cameraUpdate);

        }
    }

    // Create a marker for the geofence creation
    private void markerForGeofence(LatLng latLng) {

        Log.i(TAG, "markerForGeofence(" + latLng + ")");
        geoLocation = new Location("geoLocation");
        geoLocation.setLongitude(latLng.longitude);
        geoLocation.setLatitude(latLng.latitude);
        address = getAddress(this,latLng.latitude,latLng.longitude);

        // Define marker options
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.warning_icon);
        MarkerOptions markerOptions = new MarkerOptions()

                .position(latLng)
                .icon(icon)
                .title(address);
        if (mMap != null) {
            // Remove last geoFenceMarker
            if (geoFenceMarker != null)
                geoFenceMarker.remove();

            geoFenceMarker = mMap.addMarker(markerOptions);
        }



    }
    //Method to calculate the distance in between the two geo locations
    private void countDistance(Location lastLocation,Location geoLocation) {
        float results[] = new float[1];
        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), geoLocation.getLatitude(), geoLocation.getLongitude(), results);
        if(results[0] >= 1000 )
            distance = Float.toString(results[0]/1000 ) + " Km";
        else
            distance = Float.toString(results[0]) + " m";

        dist.setText("Distance to destination : " +  distance );


       //distance = lastLocation.distanceTo(geoLocation);

    }
    private Geofence createGeofence(LatLng latLng, float radius) {
        Log.d(TAG, "createGeofence");
        List geofenceList = new ArrayList();
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(GEO_DURATION)
                //  .setNotificationResponsiveness(1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }


    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if (geoFencePendingIntent != null)
            return geoFencePendingIntent;

        Intent intent = new Intent(this, GeofenceTrasitionIntentService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(MapsActivity.this);
    }


    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if (status.isSuccess()) {
            drawGeofence();
            //  startGeofence();
        } else {
            // inform about fail
        }
    }


    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()1234567788899");

        if (geoFenceLimits != null)
            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center(geoFenceMarker.getPosition())
                .strokeColor(Color.argb(200, 255, 0, 0))
                .fillColor(Color.argb(100, 255, 0, 0))
                .radius(GEOFENCE_RADIUS);
        geoFenceLimits = mMap.addCircle(circleOptions);

    }


    private void startGeofence() {
        Log.i(TAG, "startGeofence()");
        if (geoFenceMarker != null) {
            Geofence geofence = createGeofence(geoFenceMarker.getPosition(), GEOFENCE_RADIUS);
            GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
            addGeofence(geofenceRequest);
        } else {
            Log.e(TAG, "Geofence marker is null");
        }
    }


   /* public void populateGeofenceList() {
        for (Map.Entry<String, LatLng> entry : Constants.LANDMARKS.entrySet()) {
            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId(entry.getKey())
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }

    }*/
    public String getAddress(Context ctx,double latL, double lon){
        String fullAdd =null;
        Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
        try {
            List <android.location.Address> addresses =geocoder.getFromLocation(latL,lon,1);
            if(addresses.size()>0){
                android.location.Address address = addresses.get(0);
                fullAdd = address.getAddressLine(0);
            }
        } catch (IOException e) {
            System.out.println("ERROR CAPTURING ADDRESS");
            e.printStackTrace();

        }


        return fullAdd;
    }



}




