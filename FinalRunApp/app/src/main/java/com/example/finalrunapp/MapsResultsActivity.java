package com.example.finalrunapp;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.finalrunapp.databinding.ActivityMapsResultsBinding;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsResultsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsResultsBinding binding;
    private static final int PERMISSION_GPS_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handlePermissions();
        binding = ActivityMapsResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapResults);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(this, "Permission error!!", Toast.LENGTH_LONG).show();
        }

        Bundle bundle = getIntent().getExtras();

        String coords = bundle.getString("coords");
        String[] holeCords = coords.split(":");

        mMap.clear();

        PolylineOptions line = new PolylineOptions().clickable(false);
        LatLng firstLoc = null;
        LatLng lastLoc = null;
        boolean firstLC = true;

        for (String a : holeCords) {

            String[] singleCords = a.split("-");
            LatLng loc = new LatLng(Double.parseDouble(singleCords[0]),Double.parseDouble(singleCords[1]));
            if(firstLC) {

                firstLoc = loc;
                firstLC = false;
            }
            lastLoc = loc;
            line.add(loc);
        }

        float zoom = 15.0f;
        if(lastLoc != null && firstLoc != null) {


            Marker startM = mMap.addMarker(new MarkerOptions().position(firstLoc).title("Aloitus"));
            mMap.addMarker(new MarkerOptions().position(lastLoc).title("Lopetus"));
            startM.showInfoWindow(); //aloitustitteli defaulttina näkymään
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLoc, zoom));
        }
        mMap.addPolyline(line);

    }

    private void handlePermissions() {

           if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_GPS_CODE);
            }
    }

}