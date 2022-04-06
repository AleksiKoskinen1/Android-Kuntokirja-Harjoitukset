package com.example.finalrunapp;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class MyLocationListener implements LocationListener {
    ArrayList<Location> locations;
    boolean recordLocations;
    boolean newLocationAchieved;

    public MyLocationListener() {
        newJourney();
        recordLocations = false;
    }

    public void newJourney() {
        locations = new ArrayList<Location>();
        newLocationAchieved = false;
    }

    public float getDistanceOfJourney() {
        if(locations.size() <= 1) {
            return 0;
        }

        return locations.get(0).distanceTo(locations.get(locations.size() - 1)) / 1000;
    }

    public ArrayList<Location> getLocations() {
        return locations;
    }
    public ArrayList<Location> getLocationsToDraw() {
        newLocationAchieved = false;
        return locations;
    }
    public boolean getNewLocation() {
        return newLocationAchieved;
    }

    @Override
    public void onLocationChanged(Location location) {
        newLocationAchieved = true;

        if(recordLocations) {
            locations.add(location);
        }
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
      //  Log.d("MyApp", "onStatusChanged: " + provider + " " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        // the user enabled (for example) the GPS
       // Log.d("MyApp", "onProviderEnabled: " + provider);
    }
    @Override
    public void onProviderDisabled(String provider) {
        // the user disabled (for example) the GPS
     //   Log.d("MyApp", "onProviderDisabled: " + provider);
    }
}