package com.macapps.developer.ridertrash;


import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,OnMarkerClickListener {
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference, paradasRef;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private Marker mCurrLocationMarker;
    private String paradaStr;
    private ArrayList<LatLng> paradasLatLngs;
    private boolean ready;//Verifica que ya este listo la cadena de parada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference("driver");
        paradasRef =firebaseDatabase.getReference("paradas");


        if (android.os.Build.VERSION.SDK_INT >= M) {
            checkLocationPermission();
        }
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        paradasLatLngs=new ArrayList<>();
        mapFragment.getMapAsync(this);
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Object o = dataSnapshot.getValue();

                try {
                    if (mCurrLocationMarker != null) {
                        mCurrLocationMarker.remove();
                    }
                    JSONObject jsonObject = new JSONObject(o.toString());
                    String lat = jsonObject.getString("latitude");
                    String lng = jsonObject.getString("longitude");

                    LatLng latLng=new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Current Position");

                    mCurrLocationMarker = mMap.addMarker(markerOptions);

                } catch (JSONException e) {
                    //Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        reference.addChildEventListener(childEventListener);

        paradasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                paradaStr=dataSnapshot.getValue(Object.class).toString();
                ready=true;


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });//Descarga la info de una parada
    }

    /*
    Metodos para decodificar la info de una parada

     */
    public void decodeParada(View view){
        if(ready){
            //Toast.makeText(this, "Bajando Paradas  ", Toast.LENGTH_SHORT).show();

            JSONObject jsonObject,jsonObject1;
            JSONArray jsonArray1;
            String paradasStr="parada";
            List<HashMap> hashMaps= new ArrayList<>();
            try{
                jsonObject1=new JSONObject(paradaStr);
                Toast.makeText(this, "lngth: "+jsonObject1.length(), Toast.LENGTH_SHORT).show();

              //
                for (Integer j=0;j<=jsonObject1.length()-1;j++) {

                    paradasStr=paradasStr+j.toString();
                    Toast.makeText(this, paradasStr, Toast.LENGTH_SHORT).show();
                    jsonObject=jsonObject1.getJSONObject(paradasStr);
                    String Lat = jsonObject.getString("lat");
                    String Lng = jsonObject.getString("lng");
                    LatLng latLng = new LatLng(Double.parseDouble(Lat), Double.parseDouble(Lng));
                    paradasLatLngs.add(latLng);
                    Marker marker=mMap.addMarker(new MarkerOptions().position(latLng).title(paradasStr).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                    jsonArray1 = jsonObject.getJSONArray("rutas");
                    for (int i = 0; i <= jsonArray1.length() - 1; i++) {
                    //    Toast.makeText(this, jsonArray1.get(i).toString(), Toast.LENGTH_SHORT).show();
                    }
                    paradasStr="parada";
                }


            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    /*
    OnMapReadyCallback metodos de GoogleMaps
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
                mMap.setOnMarkerClickListener(this);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }
    /*
    ConectionCallbacks location listener
     */

    @Override
    public void onLocationChanged(Location location) {


    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);

        mLocationRequest.setSmallestDisplacement(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {


                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, "Name: "+marker.getTitle(), Toast.LENGTH_SHORT).show();
        return true;
    }
}
