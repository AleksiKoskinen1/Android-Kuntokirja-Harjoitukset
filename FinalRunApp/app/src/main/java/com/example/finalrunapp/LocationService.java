package com.example.finalrunapp;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

public class LocationService extends Service {
    private LocationManager locationManager;
    private MyLocationListener locationListener;
    private final IBinder binder = new LocationServiceBinder();

    private long startTime = 0;
    private long stopTime = 0;
    private boolean saveState = false;
    final int TIME_INTERVAL = 3;
    final int DIST_INTERVAL = 3; //3 metrii
    private double firstLa = 0.0;
    private double firstLo = 0.0;

    @Override
    public void onCreate() {
        super.onCreate();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        locationListener.recordLocations = false;
        try {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, TIME_INTERVAL, DIST_INTERVAL, locationListener);
        } catch(SecurityException e) {
            Toast myToast = Toast.makeText(this, "Ei GPD oikeuksia annettu", Toast.LENGTH_SHORT);
            myToast.show();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,  flags, startId);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    protected float getDistance() {
        return locationListener.getDistanceOfJourney();
    }

    protected void playJourney() {
        locationListener.newJourney();
        locationListener.recordLocations = true;
        startTime = SystemClock.elapsedRealtime();
        stopTime = 0;
    }

    protected double getDuration() {
        if(startTime == 0) {
            return 0.0;
        }

        long endTime = SystemClock.elapsedRealtime();

        if(stopTime != 0) {
            endTime = stopTime;
        }

        long elapsedMilliSeconds = endTime - startTime;
        return elapsedMilliSeconds / 1000.0;
    }

    protected boolean currentlyTracking() {
        return startTime != 0;
    }
    protected boolean getSaveState() {
        return saveState;
    }

    protected void resetJourney() {

        locationListener.recordLocations = false;
        stopTime = SystemClock.elapsedRealtime();
        startTime = 0;
        saveState = false;
        locationListener.newJourney();
    }

    protected void setSaveStateTrue(){
        saveState = true;
    }
    protected ArrayList<Location> getJourneyInfo() {

        ArrayList<Location> arret = locationListener.getLocations();
        if(arret.size() != 0) {
            locationListener.recordLocations = false;
            stopTime = SystemClock.elapsedRealtime();
        }

        return arret;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        locationListener = null;
        locationManager = null;
    }

    public LatLng getFirstLoc(){

        Location locationCt;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return new LatLng(0,0);
        }
        locationCt = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(locationCt != null) {
            firstLa = locationCt.getLatitude();
            firstLo = locationCt.getLongitude();
            final LatLng loc = new LatLng(LocationService.this.firstLa, LocationService.this.firstLo);
            return loc;
        }
        else{
            return new LatLng(0,0);
        }


    }
    public class LocationServiceBinder extends Binder {

        public float getDistance() {
            return LocationService.this.getDistance();
        }

        public double getDuration() {
            return LocationService.this.getDuration();
        }
        public ArrayList<Location> getCurLocation() {
            return locationListener.getLocations();
        }
        public boolean getNewLocationChanged(){
            return locationListener.getNewLocation();
        }
        public ArrayList<Location> getLocationsToDraw() {
            return locationListener.getLocationsToDraw();
        }
        public LatLng getFirstLocation(){
            return LocationService.this.getFirstLoc();
        }

        public boolean currentlyTracking() {return LocationService.this.currentlyTracking();}

        public boolean getSaveState() {return LocationService.this.getSaveState();}

        public void setSaveStateTrue() { LocationService.this.setSaveStateTrue(); }

        public void playJourney() {
            LocationService.this.playJourney();
        }

        public void resetJourney() {
            LocationService.this.resetJourney();
        }

        public ArrayList<Location> getJourneyInfo() {
            return LocationService.this.getJourneyInfo();
        }

    }


}