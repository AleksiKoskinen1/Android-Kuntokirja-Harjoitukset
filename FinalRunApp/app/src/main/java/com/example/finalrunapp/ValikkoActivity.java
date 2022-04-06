package com.example.finalrunapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.finalrunapp.databinding.ActivityValikkoBinding;

public class ValikkoActivity extends AppCompatActivity {

    private ActivityValikkoBinding binding;
    private Integer userID = 0;
    private String username = "";
    private static final int PERMISSION_GPS_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityValikkoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        handlePermissions();

        Bundle bundle = getIntent().getExtras();

        userID  = bundle.getInt("userID");
        username  = bundle.getString("username");

        binding.valmiitHarjoitukset.setOnClickListener(view -> {
            Bundle b = new Bundle();
            b.putString("username", username);
            b.putInt("userID", userID);
            Intent newTilastot = new Intent(ValikkoActivity.this, CompletedExercises.class);
            newTilastot.putExtras(b);
            startActivity(newTilastot);
        });

        binding.uusiHarjoitus.setOnClickListener(view -> {

            Bundle b = new Bundle();
            b.putString("username", username);
            b.putInt("userID", userID);

            Intent newTraining = new Intent(ValikkoActivity.this, MapsActivity.class);
            newTraining.putExtras(b);
            startActivity(newTraining);

        });

        binding.asetukset.setOnClickListener(view -> {

            Bundle b = new Bundle();
            b.putString("username", username);
            b.putInt("userID", userID);
            Intent newAsetuket = new Intent(ValikkoActivity.this, Settings.class);
            newAsetuket.putExtras(b);
            startActivity(newAsetuket);

        });
    }

    private void handlePermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_GPS_CODE);
        }
    }

}