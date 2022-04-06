package com.example.finalrunapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.example.finalrunapp.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LocationService.LocationServiceBinder locationService;
    private boolean firstPos = false;
    private Integer userID;
    private String username;
    private String locationString = "";
    private double userCurrentWeight = 0;

    private static final int PERMISSION_GPS_CODE = 1;

    private Handler postBack = new Handler();

    private ServiceConnection lsc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            handlePermissions();
            locationService = (LocationService.LocationServiceBinder) iBinder;

            setButtons();
            LatLng  lngfirst = locationService.getFirstLocation();
            moveCameraToCurPosition(lngfirst);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (locationService != null) {

                        float d = (float) locationService.getDuration();
                        long duration = (long) d;
                        float distance = locationService.getDistance();

                        long hours = duration / 3600;
                        long minutes = (duration % 3600) / 60;
                        long seconds = duration % 60;

                        float avgSpeed = 0;
                        if(d != 0) {
                            avgSpeed = distance / (d / 3600);
                        }

                        final String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                        final String dist = String.format("%.2f KM", distance);
                        final String avgs = String.format("%.2f KM/H", avgSpeed);

                        String spt = binding.exerciseSpinner.getSelectedItem().toString();
                        Integer jlaji = 1;
                        if(spt.equals("Pyöräily")) jlaji = 2;
                        if(spt.equals("Hiihto")) jlaji = 3;

                        final String currentCalories = countSpentCalories(time, avgs, userCurrentWeight, jlaji);

                        postBack.post(new Runnable() {
                            @Override
                            public void run() {

                                binding.travelTimeText.setText(time);
                                binding.averageSpeedText.setText(avgs);
                                binding.travelAmoutText.setText(dist);
                                binding.wastedCaloriesText.setText(currentCalories);
                                for(Location location : locationService.getCurLocation()) {

                                    if(!firstPos){
                                        firstPos = true;
                                        final LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                                        moveCameraToCurPosition(loc);
                                    }

                                }
                                if(locationService.getNewLocationChanged()){
                                    mMap.clear();
                                    PolylineOptions line = new PolylineOptions().clickable(false);
                                    for(Location location : locationService.getLocationsToDraw()) {

                                        LatLng loc = new LatLng(location.getLatitude(),location.getLongitude());
                                        line.add(loc);

                                        mMap.addPolyline(line);

                                    }
                                }
                            }
                        });

                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            locationService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handlePermissions();

        Bundle bundle = getIntent().getExtras();

        userID  = bundle.getInt("userID");
        username  = bundle.getString("username");

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.saveExercise.setVisibility( View.GONE );

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapResults);
        mapFragment.getMapAsync(this);

        getCurrentWeight(new VolleyListener() {
            @Override
            public void JResults(String weightResults) {

                if(!weightResults.isEmpty()){
                    userCurrentWeight = Double.parseDouble(weightResults);
                    binding.wastedCaloriesText.setVisibility(View.VISIBLE);
                    binding.wastedCalories.setVisibility(View.VISIBLE);
                }
                else{
                    binding.wastedCaloriesText.setVisibility(View.GONE);
                    binding.wastedCalories.setVisibility(View.GONE);
                }
            }
            public void jsonResults(JSONArray loginResult) {}
        });


        Spinner spinner = (Spinner) findViewById(R.id.exercise_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.harj_lajit, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        startService(new Intent(this, LocationService.class));
        bindService(
                new Intent(this, LocationService.class), lsc, Context.BIND_AUTO_CREATE);
    }

    private void setButtons() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            binding.startExercise.setEnabled(false);
            binding.stopExercise.setEnabled(false);
            return;
        }

        if(locationService != null && locationService.currentlyTracking()) {
            binding.startExercise.setEnabled(false);
            binding.stopExercise.setEnabled(true);
            binding.exerciseSpinner.setEnabled(false);
        } else {
            binding.startExercise.setEnabled(true);
            binding.exerciseSpinner.setEnabled(true);
            binding.stopExercise.setEnabled(false);
        }
        if(locationService.getSaveState()){
            locationService.resetJourney();
            binding.startExercise.setEnabled(true);
            binding.stopExercise.setEnabled(false);
            binding.exerciseSpinner.setEnabled(true);
        }
    }

    public void moveCameraToCurPosition(LatLng loc){

        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));

    }

    private String removeLastChar(String s){
        return s.substring(0, s.length() - 1);
    }

    public void getCurrentWeight(final VolleyListener callBack){

        String url = "https://aleksi-kuntokirja.herokuapp.com/api/getLatestWeight/" + userID;

        StringRequest sr = new StringRequest(Request.Method.GET,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callBack.JResults(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callBack.JResults("ERROR");
            }
        });

        MySingleton.getInstance(this).addToRequestQueue(sr);
    }

    public String countSpentCalories(String spentDuration, String spentAverage, double cWeight, Integer usedLaji){

        String[] avgSplited = spentAverage.split(" ");
        String [] dursplit = spentDuration.split(":", 5);
        String finalCalories;
        if( (dursplit[0].equals("00") && dursplit[1].equals("00")) || cWeight == 0 ){
            finalCalories = "0 kcal.";
        }
        else{
            Integer mins = 0, hours = 0;
            if(!dursplit[0].equals("00")){
                Integer h = Integer.parseInt(dursplit[0]);
                mins = h * 60;
            }

            mins = mins + Integer.parseInt(dursplit[1]);

            double calorieMultiple = 0;
            String kmh = avgSplited[0].replace(',','.');
            double f = Double.parseDouble(kmh);
            //Katsotaan nopeudesta, mikä kerroin annetaan kalorikulutukselle
            if(usedLaji == 1) { //Juoksu
                if (f <= 1) calorieMultiple = 0.01;
                else if (f > 1 && f <= 4) calorieMultiple = 0.05;
                else if (f > 4 && f <= 6) calorieMultiple = 0.07;
                else if (f > 6 && f <= 10) calorieMultiple = 0.11;
                else if (f > 10 && f <= 13) calorieMultiple = 0.16;
                else if (f > 13 && f <= 16) calorieMultiple = 0.22;
                else if (f > 16) calorieMultiple = 0.29;
            }
            else if(usedLaji == 2){ //Pyöräily
                if (f <= 1) calorieMultiple = 0.01;
                else if (f > 1 && f <= 4) calorieMultiple = 0.02;
                else if (f > 4 && f <= 14) calorieMultiple = 0.05;
                else if (f > 14 && f <= 19) calorieMultiple = 0.07;
                else if (f > 19 && f <= 24) calorieMultiple = 0.11;
                else if (f > 24) calorieMultiple = 0.18;
            }
            else if(usedLaji == 3){ //Hiihto
                if (f <= 1) calorieMultiple = 0.01;
                else if (f > 1 && f <= 4) calorieMultiple = 0.04;
                else if (f > 4 && f <= 6) calorieMultiple = 0.06;
                else if (f > 6 && f <= 10) calorieMultiple = 0.10;
                else if (f > 10 && f <= 13) calorieMultiple = 0.14;
                else if (f > 13 && f <= 18) calorieMultiple = 0.18;
                else if (f > 18) calorieMultiple = 0.20;
            }

            double temp = calorieMultiple * cWeight * mins; //Kalorilasku
            int finalTemp = (int) Math.round(temp); //Otetaan tasaluku
            finalCalories = String.valueOf(finalTemp) + " kcal.";

       //     Log.d("MyApp", "!finalCalories F: " + finalCalories);

        }

        return finalCalories;

    }
    public void saveCurrentJourney(double currentWeight, final VolleyListener callBack){

        String jdistance = (String) binding.travelAmoutText.getText();
        String jduration = (String) binding.travelTimeText.getText();
        String javg = (String) binding.averageSpeedText.getText();

        String spt = binding.exerciseSpinner.getSelectedItem().toString();
        Integer jlaji = 1;
        if(spt.equals("Pyöräily")) jlaji = 2;
        if(spt.equals("Hiihto")) jlaji = 3;

        String calories = countSpentCalories(jduration,javg,currentWeight,jlaji);

        if(locationString.length() != 0){
            locationString=removeLastChar(locationString);
        }
        else{
            Toast myToast = Toast.makeText(MapsActivity.this, "Et ole vielä liikkunut yhtään!", Toast.LENGTH_SHORT);
            myToast.show();
            callBack.JResults("");
            return;
        }

        javg = javg.replace("/","-");

        String url = "https://aleksi-kuntokirja.herokuapp.com/api/addNewJourney/" + javg + "/" + jduration + "/" + locationString + "/" + jdistance + "/" + jlaji + "/" + userID +"/" + calories;

        StringRequest sr = new StringRequest(Request.Method.POST,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callBack.JResults(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("MyApp", "error: " + error);
                callBack.JResults("ERROR");
            }
        });

        MySingleton.getInstance(this).addToRequestQueue(sr);
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
     //       mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show();
        }

        binding.saveExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveCurrentJourney(userCurrentWeight, new VolleyListener() {
                    @Override
                    public void JResults(String loginResult) {

                        Bundle b = new Bundle();
                        b.putString("username", username);
                        b.putInt("userID", userID);

                        locationService.resetJourney();

                        Toast myToast = Toast.makeText(MapsActivity.this, "Harjoitus on tallennettu onnistuneesti!", Toast.LENGTH_SHORT);
                        myToast.show();

                        Intent mainact = new Intent(MapsActivity.this, ValikkoActivity.class);
                        mainact.putExtras(b);
                        startActivity(mainact);

                    }
                    public void jsonResults(JSONArray loginResult) {}
                });
            }
        });

        binding.startExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                locationService.playJourney();
                binding.startExercise.setEnabled(false);
                binding.stopExercise.setEnabled(true);
                binding.exerciseSpinner.setEnabled(false);

            }
        });

        binding.stopExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ArrayList<Location> locainfo = locationService.getJourneyInfo();

                if(locainfo.size() == 0){
                    Toast myToast = Toast.makeText(MapsActivity.this, "Et ole vielä liikkunut yhtään!", Toast.LENGTH_SHORT);
                    myToast.show();
                    return;
                }
                binding.startExercise.setVisibility( View.GONE );
                binding.stopExercise.setVisibility( View.GONE );
                binding.saveExercise.setVisibility( View.VISIBLE );
                locationService.setSaveStateTrue();


                mMap.setMyLocationEnabled(false);
                mMap.clear();

                PolylineOptions line = new PolylineOptions().clickable(false);
                LatLng firstLoc = null;
                LatLng lastLoc = null;
                boolean firstLC = true;
                locationString = "";
                for(Location location : locainfo) {

                    locationString = locationString + location.getLatitude() + "-" + location.getLongitude() + ":";
                    LatLng loc = new LatLng(location.getLatitude(),location.getLongitude());
                    if(firstLC) {

                        firstLoc = loc;
                        firstLC = false;
                    }
                    lastLoc = loc;
                    line.add(loc);
                }

                float zoom = 15.0f;
                if(lastLoc != null && firstLoc != null) {
                    mMap.addMarker(new MarkerOptions().position(firstLoc).title("Aloitus"));
                    mMap.addMarker(new MarkerOptions().position(lastLoc).title("Lopetus"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLoc, zoom));
                }
                mMap.addPolyline(line);


            }
        });

    }

    private void handlePermissions() {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_GPS_CODE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(lsc != null) {
            unbindService(lsc);
            lsc = null;
        }
    }


}