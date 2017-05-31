package com.macapps.developer.ridertrash;


import android.Manifest.permission;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.macapps.developer.ridertrash.CardFragment.onSomeEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMarkerClickListener,onSomeEventListener {
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference, paradasRef, rutasRef;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;

    private Marker mCurrLocationMarker, destino;
    private String paradaStr, rutasStr;
    private ArrayList<LatLng> paradasLatLngs;
    private boolean ready;//Verifica que ya este listo la cadena de parada;
    BottomSheetBehavior bottomSheetBehavior;
    private ListView bottomSheetListView;
    private ItemAdapter itemAdapter;
    private HashMap<String, Marker> marcadores;
    HashMap<String, Double> shortestStart, shortestEnd;
    HashMap<String, LatLng> paradores;
    FloatingActionMenu fab;

    ArrayList<BusInfo> busInfos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ///


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");
        ///
        busInfos=new ArrayList<>();


        LinearLayout llBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        fab = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        marcadores = new HashMap<>();
        shortestStart = new HashMap<>();
        shortestEnd = new HashMap<>();
        paradores = new HashMap<>();

        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference("Driver");///Child
        paradasRef = firebaseDatabase.getReference("paradas");
        rutasRef = firebaseDatabase.getReference("rutas");


        if (android.os.Build.VERSION.SDK_INT >= M) {
            checkLocationPermission();
        }
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        paradasLatLngs = new ArrayList<>();
        mapFragment.getMapAsync(this);


        paradasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                paradaStr = dataSnapshot.getValue(Object.class).toString();
                ready = true;


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });//Descarga la info de una parada
        rutasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rutasStr = dataSnapshot.getValue(Object.class).toString();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                fab.animate().scaleX(1 - Math.abs(slideOffset)).scaleY(1 - Math.abs(slideOffset)).setDuration(0).start();

            }
        });

    }
    /*

    UI
     */
    public static float dpToPixels(int dp, Context context) {
        return dp * (context.getResources().getDisplayMetrics().density);
    }

    /*
    Metodos para decodificar la info de una parada

     */
    public void decodeParada(Marker finalPositiona) {
        if (ready) {
            //Toast.makeText(this, "Bajando Paradas  ", Toast.LENGTH_SHORT).show();

            JSONObject jsonObject, jsonObject1;
            JSONArray jsonArray1;
            String paradasStr = "parada";
            List<HashMap> hashMaps = new ArrayList<>();
            try {
                jsonObject1 = new JSONObject(paradaStr);
               // Toast.makeText(this, "lngth: " + jsonObject1.length(), Toast.LENGTH_SHORT).show();

                //
                for (Integer j = 0; j <= jsonObject1.length() - 1; j++) {

                    paradasStr = paradasStr + j.toString();
                    //   Toast.makeText(this, paradasStr, Toast.LENGTH_SHORT).show();
                    jsonObject = jsonObject1.getJSONObject(paradasStr);
                    String Lat = jsonObject.getString("lat");
                    String Lng = jsonObject.getString("lng");
                    LatLng latLng = new LatLng(Double.parseDouble(Lat), Double.parseDouble(Lng));
                    paradasLatLngs.add(latLng);
                    //mMap.addMarker(new MarkerOptions().position(latLng).title(paradasStr).icon(BitmapDescriptorFactory.fromResource(R.drawable.parada_naranja)));
                    //TODO capturar ultima ubicacion conocida
                    paradores.put(paradasStr, latLng);

                    Double latr = mCurrLocationMarker.getPosition().latitude - Double.parseDouble(Lat);
                    Double lngr = mCurrLocationMarker.getPosition().longitude - Double.parseDouble(Lng);
                    Double distancia = Math.sqrt(Math.abs(latr * latr) + Math.abs(lngr * lngr));
                    shortestStart.put(paradasStr, distancia);

                    Double late = finalPositiona.getPosition().latitude - Double.parseDouble(Lat);
                    Double lnge = finalPositiona.getPosition().longitude - Double.parseDouble(Lng);
                    Double distanciaFinal = Math.sqrt(Math.abs(late * late) + Math.abs(lnge * lnge));
                    shortestEnd.put(paradasStr, distanciaFinal);


                    paradasStr = "parada";
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void mostrarRutasEnParada(String ruta) {//
        ArrayList<LatLng> latLngs;
        JSONObject jsonObject1;
        JSONArray jsonArray;
        JSONArray jsonArray1;
        JSONObject jsonObject3;

        List<HashMap> hashMaps = new ArrayList<>();
        try {
            jsonObject1 = new JSONObject(rutasStr);
            jsonArray = jsonObject1.getJSONArray(ruta);
            jsonArray1 = jsonArray.getJSONArray(0);

            // Toast.makeText(this,jsonArray1.toString() , Toast.LENGTH_LONG).show();

            for (int i = 0; i < jsonArray1.length(); i++) {
                JSONObject jsonObject = jsonArray1.getJSONObject(i);
                for (int j = 0; j < jsonObject.length(); j++) {
                    HashMap<String, Double> hashMap1 = new HashMap<>();
                    String lat = jsonObject.getString("lat");
                    String lng = jsonObject.getString("lng");

                    //Todo Hacer dos Puntos de prueba
                    hashMap1.put("lat", Double.parseDouble(jsonObject.getString("lat")));
                    hashMap1.put("lng", Double.parseDouble(jsonObject.getString("lng")));
                    hashMaps.add(hashMap1);
                }

            }
            try {
                Log.i("HshMAp", "Entrando...");
                latLngs = new ArrayList<>();
                PolylineOptions polylineOptions;
                polylineOptions = new PolylineOptions();

                for (HashMap<String, Double> hashMap1 : hashMaps) {
                    //       Toast.makeText(this, "lat: "+hashMap1.get("lat")+ "lng: "+hashMap1.get("lng"), Toast.LENGTH_SHORT).show();
                    LatLng latLng = new LatLng(hashMap1.get("lat"), hashMap1.get("lng"));
                    latLngs.add(latLng);


                    ///TODO aqui Ya tenemos todos los puntos;

                }
                polylineOptions.addAll(latLngs);
                polylineOptions.width(15);
                if (ruta.equals("ruta1"))
                    polylineOptions.color(Color.RED);

                else
                    polylineOptions.color(Color.BLUE);


                if (polylineOptions != null) {

                    mMap.addPolyline(polylineOptions);
                    //    Toast.makeText(this, "Add Polyline", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "Poly is null", Toast.LENGTH_SHORT).show();
                }


                //  textView.setText(data);

            } catch (Exception e) {
                Log.e("String Parse Error", e.toString());
                Toast.makeText(this, "Error" + e.toString(), Toast.LENGTH_SHORT).show();
            }


        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }


    }

    public ArrayList<String> rutasEnParada(String parada) {
        ArrayList<String> rutas = new ArrayList<>();
        try {

            JSONObject jsonObject, jsonObject1;
            JSONArray jsonArray;
            jsonObject = new JSONObject(paradaStr);
            jsonObject1 = jsonObject.getJSONObject(parada);
            jsonArray = jsonObject1.getJSONArray("rutas");
            for (int i = 0; i <= jsonArray.length() - 1; i++) {
                rutas.add(jsonArray.get(i).toString());
                //   Toast.makeText(this, jsonArray.get(i).toString(), Toast.LENGTH_SHORT).show();

            }
            return rutas;

        } catch (JSONException e) {
            Toast.makeText(this, "Error en rutas en Parada", Toast.LENGTH_SHORT).show();
            rutas.add("error");
            return rutas;
        }

    }

    public void realTimePos(View view) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object o = dataSnapshot.getValue();
                ArrayList<String> keys;

                try {
                    JSONObject driverPosition = new JSONObject(o.toString());
                    keys = new ArrayList<>();

                    Iterator x = driverPosition.keys();
                    JSONArray jsonArray = new JSONArray();
                    while (x.hasNext()) {
                        String key = (String) x.next();
                        keys.add(key);
                        jsonArray.put(driverPosition.get(key));

                    }//Primero hay que eliminar los anteriores

                    for (int j = 0; j <= keys.size() - 1; j++) {//Iterar sobre el hashmap
                        if (marcadores.containsKey(keys.get(j))) {
                            Marker marker;
                            marker = marcadores.get(keys.get(j));
                            marker.remove();
                        } else {

                        }

                    }
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                        Integer routeNumber = jsonObject.getInt("ruta");
                        Integer velocidad=jsonObject.getInt("speed");
                        BusInfo busInfo=new BusInfo(new LatLng(0,0),"ruta"+routeNumber.toString(),velocidad,0);

                        jsonObject = new JSONObject(jsonObject.getString("position"));


                        Double lat = jsonObject.getDouble("latitude");
                        Double lng = jsonObject.getDouble("longitude");
                        LatLng latLng = new LatLng(lat, lng);
                        busInfo.setLatLng(latLng);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title("Current Position");
                        switch (routeNumber) {
                            case 1:
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_rojo));
                                busInfo.setId(R.drawable.bus_rojo);
                                break;
                            case 2:
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_azul));
                                busInfo.setId(R.drawable.bus_azul);

                                break;
                            default:
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_verde));
                                busInfo.setId(R.drawable.bus_verde);

                                break;

                        }
                        busInfos.add(i,busInfo);

                        // mCurrLocationMarker = mMap.addMarker(markerOptions);
                        Marker marker = mMap.addMarker(markerOptions);

                        //  Toast.makeText(MainActivity.this, keys.toString(), Toast.LENGTH_SHORT).show();

                        marcadores.put(keys.get(i), marker);


                        // Toast.makeText(MainActivity.this, jsonObject.toString(), Toast.LENGTH_LONG).show();
                    }


                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        reference.addValueEventListener(valueEventListener);
    }

    /*
    OnMapReadyCallback metodos de GoogleMaps
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_map));


        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
                mMap.setOnMarkerClickListener(this);
                mMap.setOnMapClickListener(new OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {

                        if (destino != null) {
                            destino.remove();
                        }
                        destino = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.destino_final)));
                        if (mCurrLocationMarker != null) {
                            decodeParada(destino);
                            ArrayList<Bus> buses=new ArrayList<Bus>();


                            ArrayList<String> paradasMasCercanas, paradasCercanasPosFinal;//
                            SearchModule searchModuleFinal = new SearchModule();
                            SearchModule searchModule = new SearchModule();

                            paradasMasCercanas = searchModule.searchModule(shortestStart);//parada0,pparada2,etc
                            //  Toast.makeText(MainActivity.this,"+"+ paradasMasCercanas.toString(), Toast.LENGTH_SHORT).show();

                            paradasCercanasPosFinal = searchModuleFinal.searchModule(shortestEnd);
                            // Toast.makeText(MainActivity.this, paradasCercanasPosFinal.toString(), Toast.LENGTH_LONG).show();
                            ArrayList<ArrayList<String>> paradasQueSirven = new ArrayList<ArrayList<String>>();
                            //TODO Descargar Velocidad del conductor


                            ArrayList<String> rutasIniciales = rutasEnParada(paradasMasCercanas.get(0));

                            ArrayList<String> paradas = new ArrayList<>();
                            for (int k = 0; k <= 3; k++) {
                                Bus bus=new Bus(R.drawable.bus_rojo,0,"ruta0","parada0","parada1");
                                bus.setParada_inicial(paradasMasCercanas.get(0));


                                ArrayList<String> rutasFinales = rutasEnParada(paradasCercanasPosFinal.get(k));
                                //  Toast.makeText(MainActivity.this, rutasIniciales.toString() + "finales" + rutasFinales.toString(), Toast.LENGTH_SHORT).show();
                                for (int i = 0; i <= rutasFinales.size() - 1; i++)
                                    for (int j = 0; j <= rutasIniciales.size() - 1; j++) {
                                        if (rutasFinales.get(i).equals(rutasIniciales.get(j))) {
                                            ArrayList<String> rutasTemp = new ArrayList<String>();
                                            //     Toast.makeText(MainActivity.this, "Sirve la ruta" + rutasFinales.get(i), Toast.LENGTH_SHORT).show();
                                            rutasTemp.add(rutasFinales.get(i));
                                            rutasTemp.add(paradasCercanasPosFinal.get(k));
                                            paradasQueSirven.add(rutasTemp);//Añadir posicion inicial


                                            paradas = rutasEnParada(paradasCercanasPosFinal.get(j));
                                           // Toast.makeText(MainActivity.this, paradasCercanasPosFinal.toString(), Toast.LENGTH_SHORT).show();



                                            break;
                                        }
                                    }
                                for (int l = 0; l <= paradas.size() - 1; l++) {
                                    mostrarRutasEnParada(paradas.get(l));
                                }


                                switch (k){
                                    case 0:
                                        bus.setTiempo(95);
                                    case 1:
                                        bus.setTiempo(70);
                                        break;
                                    case 2:
                                        bus.setTiempo(50);
                                        break;
                                    case 3:
                                        bus.setTiempo(30);
                                        break;

                                }
                                bus.setParada_inicial(paradasMasCercanas.get(0));
                                bus.setParada_final(paradasQueSirven.get(k).get(1));
                                bus.setRoute(paradas.get(0));
                                switch (paradas.get(0)) {
                                    case "ruta1":
                                        bus.setImagen(R.drawable.bus_rojo);
                                        break;
                                    case "ruta2":
                                        bus.setImagen(R.drawable.bus_azul);

                                        break;
                                    default:
                                        bus.setImagen(R.drawable.bus_verde);

                                        break;

                                }
                                buses.add(bus);

                                mMap.addMarker(new MarkerOptions().title(paradasMasCercanas.get(0)).position(paradores.get(paradasMasCercanas.get(0))).icon(BitmapDescriptorFactory.fromResource(R.drawable.parada_naranja)));
                                mMap.addMarker(new MarkerOptions().title(paradasQueSirven.get(k).get(1)).position(paradores.get(paradasQueSirven.get(k).get(1))).icon(BitmapDescriptorFactory.fromResource(R.drawable.parada_naranja)));
                             //   buses.add(new Bus());

                            }
                            ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
                           // Toast.makeText(MainActivity.this,"size: " +buses.size(), Toast.LENGTH_SHORT).show();
                            CardFragmentPagerAdapter pagerAdapter = new CardFragmentPagerAdapter(getSupportFragmentManager(), dpToPixels(1, MainActivity.this), buses);

                            for(int i=0;i<=buses.size()-1;i++){
                            pagerAdapter.addCardFragment(new CardFragment());}
                            ShadowTransformer fragmentCardShadowTransformer = new ShadowTransformer(viewPager, pagerAdapter);
                            fragmentCardShadowTransformer.enableScaling(true);

                            viewPager.setAdapter(pagerAdapter);
                            viewPager.setPageTransformer(false, fragmentCardShadowTransformer);
                            viewPager.setOffscreenPageLimit(3);
                            pagerAdapter.notifyDataSetChanged();
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                         //   Toast.makeText(MainActivity.this, paradasQueSirven.toString(), Toast.LENGTH_LONG).show();




                        }else{
                            Toast.makeText(MainActivity.this, "No hemos podido determinar tu ubicacion", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if(mCurrLocationMarker==null) {
            mCurrLocationMarker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.mi_pos)));
        }

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
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            mCurrLocationMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.mi_pos)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
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

    public void clearMap(View view) {
        mMap.clear();
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, "Name: " + marker.getTitle(), Toast.LENGTH_LONG).show();
        /*bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        String markerTitle=marker.getTitle();
        ArrayList<String> paradas = new ArrayList<>();
        paradas = rutasEnParada(marker.getTitle());

        for (int i = 0; i <= paradas.size() - 1; i++) {
            mostrarRutasEnParada(paradas.get(i));
        }*/

        return true;
    }


    @Override
    public void someEvent(final String s) {
        Fragment frag1 = getFragmentManager().findFragmentById(R.id.cardView);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // Actions to do after 10 seconds
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(verParadaDestino(s)));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            }
        }, 500);
    }
    public LatLng verParadaDestino(String string) {

            //Toast.makeText(this, "Bajando Paradas  ", Toast.LENGTH_SHORT).show();

            JSONObject jsonObject, jsonObject1;
            JSONArray jsonArray1;
            String paradasStr = "parada";
            List<HashMap> hashMaps = new ArrayList<>();
            try {
                jsonObject1 = new JSONObject(paradaStr);

                //

                    //   Toast.makeText(this, paradasStr, Toast.LENGTH_SHORT).show();
                    jsonObject = jsonObject1.getJSONObject(string);
                    String Lat = jsonObject.getString("lat");
                    String Lng = jsonObject.getString("lng");
                    LatLng latLng = new LatLng(Double.parseDouble(Lat), Double.parseDouble(Lng));
                    //mMap.addMarker(new MarkerOptions().position(latLng).title(paradasStr).icon(BitmapDescriptorFactory.fromResource(R.drawable.parada_naranja)));
                    //TODO capturar ultima ubicacion conocida


                    return latLng;




            } catch (JSONException e) {
                e.printStackTrace();
                return mCurrLocationMarker.getPosition();
            }

    }
    public void myLocation(View view){
        if(mCurrLocationMarker!=null){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrLocationMarker.getPosition()));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));}
    }
}
