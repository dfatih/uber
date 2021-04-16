package com.parse.starter;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class YourLocation extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;

    TextView infoTextView;
    Button requestUberButton;

    Boolean requestActive;
    ParseGeoPoint driverLocation = new ParseGeoPoint(0,0);
    String driverUsername = "";

    LocationManager locationManager;
    String provider;
    Handler handler = new Handler();

    public void requestUber(View view){

        if(requestActive == false){

            Log.i("MyApp", "Uber angefragt");

            ParseObject request = new ParseObject("Requests");
            request.put("requesterUserName", ParseUser.getCurrentUser().getObjectId());
            ParseACL parseACL = new ParseACL();
            parseACL.setPublicWriteAccess(true);
            parseACL.setPublicReadAccess(true);
            request.setACL(parseACL);

            request.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {

                    if (e == null) {

                        infoTextView.setText("Finde Uber Fahrer...");
                        requestUberButton.setText("Uber abbrechen");
                        requestActive = true;

                    }


                }
            });


        }else{

            infoTextView.setText("Uber abgebrochen");
            requestUberButton.setText("Finde Uber");
            requestActive = false;

            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
            query.whereEqualTo("requesterUserName", ParseUser.getCurrentUser().getObjectId());

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if(e == null){

                        if(objects.size() >0){

                            for(ParseObject object: objects){

                                object.deleteInBackground();

                            }

                        }


                    }


                }
            });


        }



    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        infoTextView = (TextView) findViewById(R.id.infoTextView);
        requestUberButton = (Button) findViewById(R.id.requerstUber);
        requestActive = false;
        infoTextView.setText("");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);

        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null && mMap !=null) {

            updateLocation(location);

            Log.i("MyApp", "oncreate aufgerufen");
        }


    }

    @Override
    protected void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onMapReady(mMap);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);

        Location location = locationManager.getLastKnownLocation(provider);
        // Add a marker in Sydney and move the camera
        if(mMap != null){
           updateLocation(location);

        }

        Log.i("MyApp", "map ready");
    }

    public void updateLocation(final Location location){

        if(requestActive == false){

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");
            query.whereEqualTo("requesterUserName", ParseUser.getCurrentUser().getObjectId());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e==null){

                        if(objects.size() >0){

                            for(ParseObject object: objects){
                                requestActive = true;

                                infoTextView.setText("Finde Fahrer...");
                                requestUberButton.setText("Uber abbrechen");

                                if(object.get("driverUsername") !=null){

                                    driverUsername = object.getString("driverUsername");
                                    infoTextView.setText("Fahrer ist auf dem Weg!");
                                    requestUberButton.setVisibility(View.INVISIBLE);

                                    Log.i("AppInfo", driverUsername);

                                }


                            }


                        }


                    }
                }
            });

        }



        if(mMap != null){


            if(driverUsername.equals("")){

                mMap.clear();

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));

                mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Dein Standort"));

            }

        }

        if(requestActive == true){

            if(!driverUsername.equals("")){

                ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
                userQuery.whereEqualTo("objectId", driverUsername);
                userQuery.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> objects, ParseException e) {
                        if(e== null){

                            if(objects.size() >0){

                                for (ParseUser driver: objects){

                                    driverLocation = driver.getParseGeoPoint("location");

                                }


                            }


                        }


                    }
                });

            }

            if(driverLocation.getLatitude() !=0 && driverLocation.getLongitude() !=0){

                Log.i("AppInfo", driverLocation.toString());

                Double distanceInKilomteres = driverLocation.distanceInKilometersTo(new ParseGeoPoint(location.getLatitude(), location.getLongitude()));

                Double distanceOneDP = (double) Math.round(distanceInKilomteres/10) * 10;

                infoTextView.setText("Dein Fahrer ist " + distanceOneDP.toString() + " Km entfernt");



                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                ArrayList<Marker> markers = new ArrayList<Marker>();

                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title("Rider")));
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Deine Position")));

                for(Marker marker: markers){

                    builder.include(marker.getPosition());

                }

                LatLngBounds bounds = builder.build();

                int padding = 100;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);


            }


            final ParseGeoPoint userLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");

            query.whereEqualTo("requesterUserName", ParseUser.getCurrentUser().getObjectId());

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if(e == null){

                        if(objects.size() >0){
                            Log.i("MyApp", "Request gefunden");
                            for(ParseObject object : objects){

                                object.put("requesterLocation", userLocation);
                                object.saveInBackground();
                            }

                        }

                    }


                }
            });
        }


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLocation(location);
            }
        }, 5000);



    }


    @Override
    public void onLocationChanged(Location location) {


        updateLocation(location);




        Log.i("MyApp", "location changed");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
