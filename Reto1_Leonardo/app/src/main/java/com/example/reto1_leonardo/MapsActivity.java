package com.example.reto1_leonardo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.*;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraMoveStartedListener

{

    private GoogleMap mMap;
    private FusedLocationProviderClient myLocation;
    private boolean allowMarkers;
    private ArrayList<MarkerOptions> markers;
    private Button addMarker;
    private Button allowMarker;
    private LatLng marker;
    private EditText nameMarker;
    private PlacesClient placesClient;
    private TextView placeNearest;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Places.initialize(getApplicationContext(), "AIzaSyAkFDqKr5meUSikzkTSwI6sy8pAZOZWkuY");
        PlacesClient placesClient = Places.createClient(this);
        allowMarkers = false;
        markers = new ArrayList<>();
        addMarker = findViewById(R.id.but_addMarker);
        nameMarker = findViewById(R.id.name);
        FusedLocationProviderClient c = new FusedLocationProviderClient(this);
        placeNearest = findViewById(R.id.nearest);


        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(2000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(markers.size()>0){
                                    Location loc = myLocation.getLastLocation().getResult();
                                    float[] nearest = new float[1];
                                    Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
                                            markers.get(0).getPosition().latitude, markers.get(0).getPosition().longitude, nearest);
                                    int search = 0;
                                    for (int i=0; i< markers.size();i++){
                                        float[] recent = new float[1];

                                        Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
                                                markers.get(i).getPosition().latitude, markers.get(i).getPosition().longitude, recent);
                                        if(recent[0]<nearest[0]){
                                            nearest[0] = recent[0];
                                            search = i;
                                        }
                                    }
                                    placeNearest.setText(markers.get(search).getTitle()+" que está ubicado a "+ nearest[0]+" metros");
                                }
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        addMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(allowMarkers){
                    String name = nameMarker.getText().toString();
                    if(!name.equals("")){
                        if(marker!=null){
                            markers.add(new MarkerOptions().position(marker).title(name));
                            mMap.addMarker(new MarkerOptions().position(marker).title(name));
                        }else Toast.makeText(MapsActivity.this, "No ha seleccionado una ubicación para agregar el marcador", Toast.LENGTH_SHORT).show();

                    }else Toast.makeText(MapsActivity.this, "Escriba un nombre para el marcador", Toast.LENGTH_SHORT).show();
                }else Toast.makeText(MapsActivity.this, "Habilite la opción de agregar marcadores", Toast.LENGTH_SHORT).show();
                marker = null;

                Location loc = myLocation.getLastLocation().getResult();
                LatLng me = new LatLng(loc.getLatitude(), loc.getLongitude());
                for (MarkerOptions mark: markers) {

                    mark.getPosition();


                }

            }
        });
        allowMarker = findViewById(R.id.but_allowMarker);
        allowMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowMarkers = !allowMarkers;
                if(allowMarkers){
                    Toast.makeText(MapsActivity.this,"Se ha habilitado la función de agregar marcadores", Toast.LENGTH_LONG).show();
                }else Toast.makeText(MapsActivity.this,"Se ha inhabilitado la función de agregar marcadores", Toast.LENGTH_LONG).show();
            }
        });


        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraMoveListener(this);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        Location loc = myLocation.getLastLocation().getResult();
        LatLng me = new LatLng(loc.getLatitude(), loc.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(me));
    }

    @Override
    public void onMyLocationClick(@NonNull final Location location) {
        List<Place.Field> placeFields = Collections.singletonList(Place.Field.ADDRESS);
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);
        placesClient.findCurrentPlace(request).addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                if(task.isSuccessful()){
                    if(task.getResult().getPlaceLikelihoods().size()>0){
                        FindCurrentPlaceResponse response = task.getResult();
                        double value = 0.0;
                        PlaceLikelihood nearestResult = response.getPlaceLikelihoods().get(0);
                        for (int i=0; i< response.getPlaceLikelihoods().size(); i++){
                            if(value< response.getPlaceLikelihoods().get(i).getLikelihood()){
                                value = response.getPlaceLikelihoods().get(i).getLikelihood();
                                nearestResult = response.getPlaceLikelihoods().get(i);
                            }
                        }
                        Toast.makeText(MapsActivity.this, nearestResult.getPlace().getAddress(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });



    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(allowMarkers){
            marker = latLng;
            Toast.makeText(this, "Ahora escriba el nombre del marcador y presione agregar marcador", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCameraMove() {
        Location loc = myLocation.getLastLocation().getResult();
        LatLng me = new LatLng(loc.getLatitude(), loc.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(me));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Location loc = myLocation.getLastLocation().getResult();
        LatLng anotherPlace = marker.getPosition();
        float[] results = new float[1];
        Location.distanceBetween(loc.getLatitude(),loc.getLongitude(),anotherPlace.latitude,anotherPlace.longitude, results);
        if(results[0]<50.0){
            Toast.makeText(this, "Ya se encuentra ubicado en ese lugar", Toast.LENGTH_LONG).show();
        }else Toast.makeText(this, "La distancia entre usted y este marcador es de "+results[0]+" metros", Toast.LENGTH_LONG).show();

        return false;
    }

    @Override
    public void onCameraMoveStarted(int i) {
        Location loc = myLocation.getLastLocation().getResult();
        LatLng me = new LatLng(loc.getLatitude(), loc.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(me));
    }
}