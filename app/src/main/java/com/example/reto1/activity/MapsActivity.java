package com.example.reto1.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.reto1.R;
import com.example.reto1.communication.HTTPSWebUtilDomi;
import com.example.reto1.communication.LocationWorker;
import com.example.reto1.communication.TrackHolesWorker;
import com.example.reto1.communication.TrackUsersWorker;
import com.example.reto1.model.Hole;
import com.example.reto1.model.Position;
import com.example.reto1.model.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener, View.OnClickListener {

    public static final char USER_TYPE='U';
    public static final char HOLE_TYPE='H';

    private GoogleMap mMap;
    private User user;
    private LocationManager manager;
    private ArrayList<Marker> pointsUsers;
    private ArrayList<Marker> pointsHoles;
    private ArrayList<Hole> holes;
    private ArrayList<User> users;
    private LocationWorker locationWorker;
    private TrackUsersWorker trackUsersWorker;
    private TrackHolesWorker trackHolesWorker;
    private Position currPos;
    private Marker myPosition;
    private Button addBtn;
    private TextView distanceTV;
    private Button confirmButton;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        addBtn = findViewById(R.id.addBtn);
        distanceTV = findViewById(R.id.distanceTV);

        user = new User(getIntent().getExtras().getString("user"), currPos);
        confirmButton=findViewById(R.id.confirmButton);
        geocoder = new Geocoder(this, Locale.getDefault());

        pointsHoles = new ArrayList<>();
        pointsUsers = new ArrayList<>();

        holes = new ArrayList<>();
        users = new ArrayList<>();
        status=false;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        addBtn.setOnClickListener(this);
        confirmButton.setOnClickListener(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, this);

        setInitalPos();

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);

        locationWorker = new LocationWorker(this);
        locationWorker.start();
        trackUsersWorker = new TrackUsersWorker(this);
        trackUsersWorker.start();
        trackHolesWorker = new TrackHolesWorker(this);
        trackHolesWorker.start();
    }

    @Override
    protected void onDestroy() {
        locationWorker.finish();
        trackUsersWorker.finish();
        trackHolesWorker.finish();
        super.onDestroy();
    }

    @SuppressLint("MissingPermission")
    public void setInitalPos(){
        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null) {
            updateMyLocation(location);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        updateMyLocation(location);
    }

    public void updateMyLocation(Location location){
        LatLng myPos = new LatLng(location.getLatitude(), location.getLongitude());
        if(myPosition == null) {
            myPosition = mMap.addMarker(new MarkerOptions().position(myPos));
        }else{
            myPosition.setPosition(myPos);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPos, 20));
        currPos =  new Position(location.getLatitude(), location.getLongitude());
        confirmHole();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, marker.getPosition().latitude+", "+marker.getPosition().longitude, Toast.LENGTH_LONG).show();
        Log.e(">>>",marker.getPosition().latitude+", "+marker.getPosition().longitude);
        marker.showInfoWindow();
        return true;
    }

    public Position getCurrPos(){
        return currPos;
    }

    public User getUser(){
        return user;
    }

    public void updateHoleMarkers(ArrayList<Hole> holesList){
        runOnUiThread(()-> {
            for (int i = 0; i < pointsHoles.size(); i++) pointsHoles.get(i).remove();
            pointsHoles.clear();
            holes.clear();
            for (int i = 0; i < holesList.size(); i++) {
                Marker m = null;
                Position loc = (holesList.get(i)).getLocation();
                if ((holesList.get(i)).isConfirmed()) {
                    m = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(loc.getLat(), loc.getLng()))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.confirmed_hole)));

                } else {
                    m = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(loc.getLat(), loc.getLng()))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.unconfirmed_hole)));
                }
                holes.add(holesList.get(i));
                pointsHoles.add(m);
            }
        });
    }

    public void updateUserMarkers(ArrayList<User> usersList){
        runOnUiThread(()->{
            for(int i = 0; i < pointsUsers.size(); i++) pointsUsers.get(i).remove();
            pointsUsers.clear();
            users.clear();
            for(int i=0;i<usersList.size();i++){
                Marker m = null;
                Position loc = (usersList.get(i)).getLocation();
                if(!loc.equals(currPos)) {
                    m = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(loc.getLat(), loc.getLng()))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    pointsUsers.add(m);
                }
                users.add(usersList.get(i));
            }
        });
    }

    public void computeDistances() {
        runOnUiThread(
                ()->{
                    if(holes.isEmpty()){
                        distanceTV.setText("No confirmed holes yet");
                    }else {
                        double distance = 1e18;
                        for (int i = 0; i < holes.size(); i++) {
                            if(holes.get(i).isConfirmed()) {
                                Marker marker = pointsHoles.get(i);
                                LatLng markerLoc = marker.getPosition();
                                LatLng meLoc = myPosition.getPosition();
                                distance = Math.min(distance, SphericalUtil.computeDistanceBetween(meLoc, markerLoc));
                            }
                        }
                        if(distance == 1e18){
                            distanceTV.setText("No confirmed holes yet");
                        }else {
                            long finalDistance = Math.round(distance);
                            distanceTV.setText("Nearest hole in " + finalDistance + " meters");
                        }
                    }
                }
        );
    }

    private Hole holeToConfirm;

    public void confirmHole(){
        for(int i = 0; i<holes.size();i++){
            Position holePos=holes.get(i).getLocation();
            String holeUserId = holes.get(i).getUserID();
            boolean holeIsConfirmed = holes.get(i).isConfirmed();
            if(currPos.equals(holePos) && !holeIsConfirmed && !user.getId().equals(holeUserId)){
                confirmButton.setVisibility(View.VISIBLE);
                holeToConfirm=holes.get(i);
                break;
            }
        }
    }

    private boolean status;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addBtn:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                String address=null;
                try {
                    List<Address> addresses = geocoder.getFromLocation(currPos.getLat(), currPos.getLng(), 1);
                    address = addresses.get(0).getAddressLine(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                builder.setMessage("Longitude: " + myPosition.getPosition().longitude + '\n' + "Latitude:" + myPosition.getPosition().latitude + "\nAddress: " + address);
                builder.setTitle("Confirm to add a hole!");
                builder.setNegativeButton("cancel", (dialog, id) -> dialog.cancel());
                builder.setPositiveButton("ok", (dialog, which) -> {
                    new Thread(() -> {
                        HTTPSWebUtilDomi utilDomi = new HTTPSWebUtilDomi();
                        Gson gson = new Gson();
                        String json = utilDomi.GETrequest("https://reto1-apps-moviles.firebaseio.com/holes.json");
                        Type type = new TypeToken<HashMap<String,Hole>>(){}.getType();
                        HashMap<String, Hole> holesMap = gson.fromJson(json, type);
                        HashSet<Position> holesPos = new HashSet<>();
                        for(Hole h:holesMap.values()) holesPos.add(h.getLocation());
                        if(!holesPos.contains(currPos)) {
                            Hole hole = new Hole(UUID.randomUUID().toString(), user.getId(), currPos, false);
                            String res = utilDomi.PUTrequest("https://reto1-apps-moviles.firebaseio.com/holes/" + hole.getId() + ".json", gson.toJson(hole));
                            status=true;
                        }
                        if(status) {
                            runOnUiThread(()->Toast.makeText(this, "Hole added successfully!", Toast.LENGTH_SHORT).show());
                            status=false;
                        } else {
                            runOnUiThread(()->Toast.makeText(this, "It was not possible to add a hole. \nMaybe there is already a hole in this location or connection with database failed", Toast.LENGTH_LONG).show());
                        }
                    }).start();
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.confirmButton:
                new Thread(() -> {
                    HTTPSWebUtilDomi utilDomi = new HTTPSWebUtilDomi();
                    Gson gson = new Gson();
                    holeToConfirm.setConfirmed(true);
                    utilDomi.PUTrequest("https://reto1-apps-moviles.firebaseio.com/holes/" + holeToConfirm.getId()+".json", gson.toJson(holeToConfirm));
                }).start();
                confirmButton.setVisibility(View.INVISIBLE);
                break;
        }
    }
}