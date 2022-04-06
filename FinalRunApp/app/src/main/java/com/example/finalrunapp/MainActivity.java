package com.example.finalrunapp;

import android.os.Bundle;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import com.example.finalrunapp.databinding.ActivityMainBinding;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;

public class MainActivity extends AppCompatActivity  {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }

    public void getLogin(String username, String password, final VolleyListener callBack){

        username = username.trim();
        password = password.trim();

        username = "demo";
        password = "pass";
        if(username.isEmpty() || password.isEmpty()){
            callBack.jsonResults(new JSONArray());
        }
        else {
            String url = "https://aleksi-kuntokirja.herokuapp.com/api/getUser/" + username + "/" + password;

            JsonArrayRequest jsonArrRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, response -> {
                    callBack.jsonResults(response);
                },
                error -> Log.d("MyApp", "ERROR " + error));
            MySingleton.getInstance(this).addToRequestQueue(jsonArrRequest);
        }


    }


}