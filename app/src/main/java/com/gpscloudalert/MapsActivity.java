package com.gpscloudalert;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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


import static com.gpscloudalert.R.id.map;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMyLocationButtonClickListener, ResultCallback<Status>

{
    public static final int IMAGE_RED = 0;
    public static final int IMAGE_ORANGE = 1;
    public static final int IMAGE_YELLOW = 2;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 100.0f; // in meters
    private static final int REQ_PERMISSION = 999;
    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";
    public static TextView log, lat, dist;
    static int testInt = 0;
    private static Location lastLocation;
    private static String distance;
    private static Location geoLocation;
    private final int UPDATE_INTERVAL = 3 * 60 * 1000;// 3 minutes
    private final int FASTEST_INTERVAL = 30 * 1000;// 30 secs
    private final int GEOFENCE_REQ_CODE = 0;
    public Marker locationMarker;
    public String geoFenceAddress;
    public String  warningLevel;
    protected ArrayList<Geofence> mGeofenceList;
    List geofenceList;
    boolean shouldUpdateGpsText = true;
    Object lock;
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Marker geoFenceMarker;
    private PendingIntent geoFencePendingIntent;
    private Circle geoFenceLimits;
    private double distan;
    private boolean threadShouldBeRunning = true;
    private TextView warningStreet;
    private ArrayList<LatLng> latLngArrayList;
    private android.os.Handler handler;

    // Create a Intent send by the notification
    public static Intent createNotificationIntent(Context context, String msg) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.putExtra(NOTIFICATION_MSG, msg);
        return intent;
    }

    //Method to calculate the distance in between the two geo locations
    private static void getCoordiantesFromGPSAndCalculateDistance(Location lastLocation, Location geoLocation) {
        float results[] = new float[1];
        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), geoLocation.getLatitude(), geoLocation.getLongitude(), results);
        if (results[0] >= 1000)
            distance = Float.toString(results[0] / 1000) + " Km";
        else
            distance = Float.toString(results[0]) + " m";

//        dist.setText("Distance to destination : " + distance);

        //distance = lastLocation.distanceTo(geoLocation);


    }

    public static synchronized void updateDistance() {
        getCoordiantesFromGPSAndCalculateDistance(lastLocation, geoLocation);
        Log.e(TAG, "count distance6666" + distance);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lock = new Object();

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);

        initialized();
        createGoogleApi();


    }

    // initialized textview and array
    private void initialized() {

        mGeofenceList = new ArrayList<Geofence>();

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
        markerForLocation(new LatLng(location.getLatitude(), location.getLongitude()));
        //mMap.addMarker(new MarkerOptions().position(new LatLng(latL,lon)).title("Marker "));
        writeActualLocation(location);


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

        populatePositions(latLng);
        // threadKeeper();


    }

    private void populatePositions(LatLng tmpLocation) {
        //LatLng latLng = new LatLng(tmpLocation.latitude, tmpLocation.longitude);
        //array list to store the temporary locations generated
        ArrayList<MyLocationList> myGeoLocations = new ArrayList<MyLocationList>();
        //LatLng latLngTmp = null;
        Marker myMarker = null;

        int randomInt = (int) (Math.random() * 4);


        //following loop generates 5 temporary locations around the central geolocation highlighted on the map
        // double tmpRandNum ;
        for (double i = 0; i < 1; i++) {


            if (randomInt == 0 || randomInt == 1) //set red when even number 2,4
                myGeoLocations.add(new MyLocationList(tmpLocation, IMAGE_RED, 1));
            else if (randomInt == 2) //set blue when odd number 3,5
                myGeoLocations.add(new MyLocationList(tmpLocation, IMAGE_ORANGE, 2));
            else // in other cases set color green
                myGeoLocations.add(new MyLocationList(tmpLocation, IMAGE_YELLOW, 3));
        }

        //loop through the temporary geo locations created and populate on the map
        for (int i = 0; i < myGeoLocations.size(); i++) {
             warningLevel = String.valueOf(myGeoLocations.get(i).getWarningLevel());
            myMarker = getMarkerForGeofence(myGeoLocations.get(i).getLatLng(),warningLevel);

            drawGeofence(myMarker);
            startGeofence(myMarker);
            threadKeeper(popup(getAddress(this, myGeoLocations.get(i).getLatLng().longitude, myGeoLocations.get(i).getLatLng().latitude) + " Warning Level " + warningLevel, myGeoLocations.get(i).getImage()));

        }


        getCoordiantesFromGPSAndCalculateDistance(lastLocation, geoLocation);
    }

    // Create a Location Marker
    private void markerForLocation(LatLng latLng) {
        Log.i(TAG, "markerLocation(" + latLng + ")");
        //String title = latLng.latitude + ", " + latLng.longitude;
        String markerLocationAddress = getAddress(this, latLng.latitude, latLng.longitude);
        Log.i(TAG, "markerLocationAddress " + markerLocationAddress );
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(markerLocationAddress);
        
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




    private Marker getMarkerForGeofence(LatLng latLng,String warningLevel) {
        Log.i(TAG, "markerForGeofence(" + latLng + ")");
        geoLocation = new Location("geoLocation");
        geoLocation.setLongitude(latLng.longitude);
        geoLocation.setLatitude(latLng.latitude);
         geoFenceAddress = getAddress(this, latLng.latitude, latLng.longitude);
        Log.e(TAG, "geoFenceAddress... " + geoFenceAddress );
        // Define marker options
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.warning_icon);
        MarkerOptions markerOptions = new MarkerOptions()

                .position(latLng)
                .icon(icon)
                .title(geoFenceAddress + "  Warning Level  " + warningLevel);
        /*if (mMap != null) {
            // Remove last geoFenceMarker
            if (geoFenceMarker != null)
                geoFenceMarker.remove();

            geoFenceMarker = mMap.addMarker(markerOptions);

        }*/

        geoFenceMarker = mMap.addMarker(markerOptions);
        return geoFenceMarker;

    }

    private Geofence createGeofence(LatLng latLng, float radius) {
        Log.d(TAG, "createGeofence");

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
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    Log.d(TAG, "STATUS" + status.getStatus());
                }
            });
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if (status.isSuccess()) {
            //drawGeofence();
            //  startGeofence();
        } else {
            // inform about fail
        }
    }

    private void drawGeofence(Marker geoFenceMarker) {

        //if (geoFenceLimits != null)
        //    geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center(geoFenceMarker.getPosition())
                .strokeColor(Color.argb(200, 255, 0, 0))
                .fillColor(Color.argb(100, 255, 0, 0))
                .radius(GEOFENCE_RADIUS);
        geoFenceLimits = mMap.addCircle(circleOptions);

    }

    private void startGeofence(Marker geoFenceMarker) {
        Log.i(TAG, "startGeofence()");
        if (geoFenceMarker != null) {
            Geofence geofence = createGeofence(geoFenceMarker.getPosition(), GEOFENCE_RADIUS);
            GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
            addGeofence(geofenceRequest);
        } else {
            Log.e(TAG, "Geofence marker is null");
        }
    }

    public String getAddress(Context ctx, double latL, double lon) {
        String fullAdd = null;
        Log.e(TAG, "getAddress......" +latL +lon);
        Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
        try {
            List<android.location.Address> addresses = geocoder.getFromLocation(latL, lon, 1);
            if (addresses.size() > 0) {
                android.location.Address address = addresses.get(0);
                fullAdd = address.getAddressLine(0);
            }
        } catch (IOException e) {
            System.out.println("ERROR CAPTURING ADDRESS");
            e.printStackTrace();

        }
        Log.e(TAG, "full....getAddress......" +fullAdd);

        return fullAdd;
    }

    public AlertDialog popup(String address, int whichImage) {
        AlertDialog dialog = showPopupFromMainActivity();

        dialog.show();
        TextView countDone = (TextView) dialog.findViewById(R.id.countDown);
        countDone.setText(distance);
        warningStreet = (TextView) dialog.findViewById(R.id.warningStreet);
        warningStreet.setText("Warning Street:  " + geoFenceAddress  );
        TextView warningLev =(TextView)dialog.findViewById(R.id.warningLevel);
        warningLev.setText("Warning Level "+warningLevel);

        Log.e(TAG, "address........" +address);

        ImageView imageView = (ImageView) dialog.findViewById(R.id.imageView);
        if (imageView != null) {
            switch (whichImage) {
                case IMAGE_RED:
                    imageView.setImageResource(R.drawable.ic_launcher_red);
                    //imageView.setImageDrawable();
                    break;

                case IMAGE_ORANGE:
                    imageView.setImageResource(R.drawable.orange);
                    break;

                case IMAGE_YELLOW:
                    imageView.setImageResource(R.drawable.ic_launcher_yellow);
                    break;
            }
        }

        //PopupWarning popupWarning = new PopupWarning();
        //popupWarning.show(getSupportFragmentManager(), "popup warning");

        Bundle args = new Bundle();
        args.putString("key", address);
        //popupWarning.setArguments(args);

        return dialog;

    }

    public AlertDialog showPopupFromMainActivity() {
        LayoutInflater inflater;
        View view;
        TextView warningStreet;
        String strtext;
        Button btnOk;
        TextView countDone;

        String countDoneText;
        String dataPoint;

        double longitude, latiitude;


        inflater = this.getLayoutInflater();
        view = inflater.inflate(R.layout.activity_popup_warning, null);
        if (view == null) {
            Log.d("ouch", "view was null");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //get arguments from the bundle
        //Bundle mArgs = getArguments();
        //String key = mArgs.getString("key");
        //longitude = mArgs.getDouble("longitude");
        //latiitude = mArgs.getDouble("latitude");


        btnOk = (Button) view.findViewById(R.id.btnOk);

        countDone = (TextView) view.findViewById(R.id.countDown);
        //countDone.setText(dataPoint.getS());
        // Log.e(TAG, "count distance5555 " + dataPoint.getS());


        final AlertDialog d = builder.setView(view).create();
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // When button is clicked, call up to owning activity.
                d.dismiss();
            }
        });
        return d;
    }

    public void updateDistanceThread(final AlertDialog alertDialog) {
        final Thread thread = new Thread(new Runnable() {


            @Override
            public void run() {


                updateDistance();


                //TODO stop this from spamming updates if coordinate has not changed
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (alertDialog != null && alertDialog.isShowing()) {
                            TextView countDone = (TextView) alertDialog.findViewById(R.id.countDown);
                            countDone.setText(distance);
                            Log.e(TAG, "count distance4444" + distance);
                            synchronized (lock) {
                                lock.notify();
                            }
                        } else {
                            shouldUpdateGpsText = false;
                        }
                    }
                });
            }

        });
        thread.start();

    }


    public void threadKeeper(final AlertDialog alertDialog) {

        final Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (shouldUpdateGpsText) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateDistanceThread(alertDialog);
                        }
                    });
                    try {
                        synchronized (lock) {
                            lock.wait();
                        }
                    } catch (InterruptedException e) {

                    }

                }
//                if (d != null && d.isShowing()){
//                    TextView countDone = (TextView)d.findViewById(R.id.countDown);
//                    countDone.setText(distance);
//                    Log.e(TAG, "count distance4444" + distance);
//                }
                //PopupWarning.dataPoint.setS("" + distance);
            }
        });

        thread2.start();
    }

}















