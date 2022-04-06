package com.example.finalrunapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.finalrunapp.databinding.ActivitySettingsBinding;

import org.json.JSONArray;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class Settings extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private Integer settingsUserID;
    private String settingsUsername;
    private final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    private String formattedDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Bundle bundle = getIntent().getExtras();
        settingsUserID  = bundle.getInt("userID");
        settingsUsername  = bundle.getString("username");

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getCurrentWeight(new VolleyListener() {
            @Override
            public void JResults(String weightResults) {

                if(!weightResults.isEmpty()){
                    binding.userWeight.setText(weightResults);
                }
            }
            public void jsonResults(JSONArray loginResult) {}
        });

    }

    public void getCurrentWeight(final VolleyListener callBack){

        String url = "https://aleksi-kuntokirja.herokuapp.com/api/getLatestWeight/" + settingsUserID;
        StringRequest sr = new StringRequest(Request.Method.GET,url,
                callBack::JResults,
                error -> {
                    Log.d("MyApp", "error: " + error);
                    callBack.JResults("ERROR");
                });

        MySingleton.getInstance(this).addToRequestQueue(sr);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setUserWeight(double inputweight, final VolleyListener callBack){

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDateTime now = LocalDateTime.now();
        formattedDateTime = now.format(dtf);

        String extraInfo = "Mobiilista laitettu";
        String url = "https://aleksi-kuntokirja.herokuapp.com/api/postWeight/" + settingsUserID + "/" + formattedDateTime + "/" + extraInfo + "/"+ inputweight ;

        StringRequest sr = new StringRequest(Request.Method.POST,url,
                callBack::JResults,
                error -> {
                    Log.d("MyApp", "error: " + error);
                    callBack.JResults("ERROR");
                });

        MySingleton.getInstance(this).addToRequestQueue(sr);
    }

    public void saveSettings(View view){

        String weight = binding.userWeight.getText().toString();
        weight = weight.replace(',', '.'); //Jos syötetty pilkku, korvataan pisteellä
        weight = weight.replaceAll("\\s+",""); //Otetaan mahdolliset välilyönnit pois
        boolean isCorrect = isCorrectWeight(weight); //Katsotaan onko syötetty oikeassa muodossa paino

        if(isCorrect){

            double finalWeight = Double.parseDouble(weight);
            setUserWeight(finalWeight, new VolleyListener() {
                @Override
                public void JResults(String results) {

                    Bundle b = new Bundle();
                    b.putString("username", settingsUsername);
                    b.putInt("userID", settingsUserID);

                    Toast myToast = Toast.makeText(Settings.this, "Asetukset on tallennettu onnistuneesti!", Toast.LENGTH_SHORT);
                    myToast.show();

                    Intent mainact = new Intent(Settings.this, ValikkoActivity.class);
                    mainact.putExtras(b);
                    startActivity(mainact);
                }
                public void jsonResults(JSONArray loginResult) {}
            });
        }
        else{
            Toast myToast = Toast.makeText(this, "Syötä paino oikeassa muodossa! Esimerkiksi 78.50", Toast.LENGTH_LONG);
            myToast.show();
        }
    }

    public boolean isCorrectWeight(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }
}