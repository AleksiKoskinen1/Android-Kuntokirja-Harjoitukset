package com.example.finalrunapp;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import android.app.AlertDialog;

import com.android.volley.toolbox.StringRequest;
import com.example.finalrunapp.databinding.ActivityCompletedExersicesBinding;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.InputStream;
import java.util.ArrayList;

public class CompletedExercises extends ListActivity {

    private ActivityCompletedExersicesBinding binding;

    private CompletedAdapter adapter;
    private ArrayList<completedItem> journeyNames;
    private Integer cuserid;
    private Integer claji = 1;


    private class completedItem {

        private String date;
        private String matka;
        private String aika;
        private String aikaavg;
        private String jcoords;
        private String imUri;
        private String imUri2;
        private String jid;
        private Integer hasProgram;
        private String pinfoText;
        private String calories;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getMatka() { return matka; }
        public void setMatka(String matka) { this.matka = matka; }
        public String getAika() { return aika; }
        public void setAika(String aika) { this.aika = aika; }
        public String getAikaavg() { return aikaavg; }
        public void setAikaavg(String aikaavg) { this.aikaavg = aikaavg; }
        public String getJcoords() { return jcoords; }
        public void setJcoords(String jcoords) { this.jcoords = jcoords; }
        public String getImUri() {
            return imUri;
        }
        public String getImUri2() {
            return imUri2;
        }
        public void setJid(String jid) {
            this.jid = jid;
        }
        public String getJid() {
            return jid;
        }
        public void setHasProgram(Integer hasProgram) {
            this.hasProgram = hasProgram;
        }
        public Integer getHasProgram() {
            return hasProgram;
        }
        public void setPinfoText(String pinfoText) {
            this.pinfoText = pinfoText;
        }
        public String getPinfoText() {
            return pinfoText;
        }
        public void setCalories(String calories) {
            this.calories = calories;
        }
        public String getCalories() {
            return calories;
        }


    }

    private class CompletedAdapter extends ArrayAdapter<completedItem> {

        private ArrayList<completedItem> items;

        public CompletedAdapter(Context context, int textViewResourceId, ArrayList<completedItem> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @SuppressLint({"UseCompatLoadingForDrawables", "InflateParams"})
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.completed_list_item, null);
            }

            completedItem item = items.get(position);
            if (item != null) {
                TextView itemdate = v.findViewById(R.id.itemDate);
                TextView itemdistance = v.findViewById(R.id.itemDistance);
                TextView itemduration = v.findViewById(R.id.itemDuration);
                TextView itemavg = v.findViewById(R.id.itemAvgSpeed);
                TextView pinfot = v.findViewById(R.id.programInfoText);
                TextView itemCalories = v.findViewById(R.id.itemCalories);
                ImageView img = v.findViewById(R.id.exerciseImage);
                ImageView trash = v.findViewById(R.id.trashJourney);

                if (itemdate != null) { itemdate.setText(item.getDate());  }
                if (itemdistance != null) { itemdistance.setText(item.getMatka());  }
                if (itemduration != null) { itemduration.setText(item.getAika());  }
                if (itemavg != null) { itemavg.setText(item.getAikaavg());  }
                if (pinfot != null) { pinfot.setText(item.getPinfoText());  }
                if (itemCalories != null) { itemCalories.setText(item.getCalories());  }
                if(img != null) {
                    String imUri = item.getImUri();
                    if(imUri != null) {
                        try {
                            final Uri imageUri = Uri.parse(imUri);
                            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            img.setImageBitmap(selectedImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        if(claji == 1) img.setImageDrawable(getResources().getDrawable(R.drawable.runner3));
                        else if(claji == 3) img.setImageDrawable(getResources().getDrawable(R.drawable.skier));
                        else if(claji == 2) img.setImageDrawable(getResources().getDrawable(R.drawable.cycler));
                    }
                }
                if(trash != null) {
                    String imUri2 = item.getImUri2();
                    if(imUri2 != null) {
                        try {
                            final Uri imageUri2 = Uri.parse(imUri2);
                            final InputStream imageStream2 = getContentResolver().openInputStream(imageUri2);
                            final Bitmap selectedImage2 = BitmapFactory.decodeStream(imageStream2);
                            trash.setImageBitmap(selectedImage2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        if(claji == 1) trash.setImageDrawable(getResources().getDrawable(R.drawable.trashcan));
                        else if(claji == 3) trash.setImageDrawable(getResources().getDrawable(R.drawable.trashcan));
                        else if(claji == 2) trash.setImageDrawable(getResources().getDrawable(R.drawable.trashcan));
                    }
                }

                Button btnMap = (Button) v.findViewById(R.id.showMapBtn);
                Button btnAdd = (Button) v.findViewById(R.id.addToProgramBtn);

                if(item.getHasProgram() == 1 && btnAdd != null){
                    btnAdd.setText("Poista");
                }

                if(trash != null) {
                    trash.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            AlertDialog.Builder trashPrompt = new AlertDialog.Builder(getContext());
                            trashPrompt.setMessage("Haluatko varmasti poistaa harjoituksen?");
                            trashPrompt.setCancelable(true);

                            trashPrompt.setPositiveButton(
                                    "Kyllä",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            removeJourney(Integer.parseInt(item.getJid()), new VolleyListener() {
                                                @Override
                                                public void JResults(String removeResults) {

                                                    adapter.notifyDataSetChanged();
                                                    adapter.clear();
                                                    adapter.notifyDataSetChanged();
                                                    setChosenExersices();
                                                    dialog.cancel();
                                                }

                                                public void jsonResults(JSONArray loginResult) {
                                                }
                                            });
                                        }
                                    });

                            trashPrompt.setNegativeButton(
                                    "Ei",
                                    (dialog, id) -> dialog.cancel());

                            AlertDialog trashWindow = trashPrompt.create();
                            trashWindow.show();
                        }
                    });
                }
                btnMap.setOnClickListener(view -> {

                    Bundle b = new Bundle();
                    b.putString("coords", item.getJcoords());
                    Intent newCMap = new Intent(CompletedExercises.this, MapsResultsActivity.class);
                    newCMap.putExtras(b);
                    startActivity(newCMap);
                });

                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(item.getHasProgram() == 1){  //Oli ohjelma, poistetaan se liitos juoksuun

                            AlertDialog.Builder prompti = new AlertDialog.Builder(getContext());
                            prompti.setMessage("Haluatko varmasti poistaa juoksun liitoksen ohjelmasta?");
                            prompti.setCancelable(true);

                            prompti.setPositiveButton(
                                    "Kyllä",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            removeJourneyFromProgram(Integer.parseInt(item.getJid()), new VolleyListener() {
                                                @Override
                                                public void JResults(String removeResults) {

                                                    adapter.notifyDataSetChanged();
                                                    adapter.clear();
                                                    adapter.notifyDataSetChanged();
                                                    setChosenExersices();
                                                    dialog.cancel();

                                                }
                                                public void jsonResults(JSONArray loginResult) {}
                                            });


                                        }
                                    });

                            prompti.setNegativeButton(
                                    "Ei",
                                    (dialog, id) -> dialog.cancel());

                            AlertDialog alertWindow = prompti.create();
                            alertWindow.show();

                        }
                        else { //Otetaan ohjelma lista käyttäjälle
                            Bundle b = new Bundle();
                            b.putInt("userid", cuserid);
                            b.putString("journeyid", item.getJid());

                            Intent programs = new Intent(CompletedExercises.this, Programs.class);
                            programs.putExtras(b);
                            startActivity(programs);
                        }
                    }
                });
            }
            return v;
        }
    }

    public void removeJourney(Integer jid, final VolleyListener callBack){

        String url = "https://aleksi-kuntokirja.herokuapp.com/api/delJourney/" + jid ;

        StringRequest sr = new StringRequest(Request.Method.DELETE,url,
                callBack::JResults,
                error -> {
                    Log.d("MyApp", "error: " + error);
                    callBack.JResults("ERROR");
                });

        MySingleton.getInstance(this).addToRequestQueue(sr);
    }

    public void removeJourneyFromProgram(Integer jid, final VolleyListener callBack){

        String url = "https://aleksi-kuntokirja.herokuapp.com/api/removeJourneyFromProgram/" + jid ;

        StringRequest sr = new StringRequest(Request.Method.POST,url,
                callBack::JResults,
                error -> {
                        Log.d("MyApp", "error: " + error);
                        callBack.JResults("ERROR");
                    }
                );

        MySingleton.getInstance(this).addToRequestQueue(sr);
    }

    public void getChosenOnes(final VolleyListener callBack){

        String url = "https://aleksi-kuntokirja.herokuapp.com/api/getUserCompletedByLaji/" + cuserid + "/" + claji;

        JsonArrayRequest jsonArrRequest = new JsonArrayRequest
                (Request.Method.GET, url, null,
                        callBack::jsonResults,
                        error -> Log.d("MyApp", "ERROR " + error)
                );
        MySingleton.getInstance(this).addToRequestQueue(jsonArrRequest);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        cuserid  = bundle.getInt("userID");

        binding = ActivityCompletedExersicesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        journeyNames = new ArrayList<>();
        adapter = new CompletedAdapter(this, R.layout.completed_list_item, journeyNames);
        setListAdapter(adapter);
        setChosenExersices();

        binding.ejuoksu.setOnClickListener(view -> {
            claji = 1;
            binding.epyora.setBackground(null);
            binding.ehiihto.setBackground(null);
            binding.ejuoksu.setBackgroundResource(R.drawable.roundcorners);
            setChosenExersices();
        });

        binding.epyora.setOnClickListener(view -> {
            claji = 2;
            binding.ejuoksu.setBackground(null);
            binding.ehiihto.setBackground(null);
            binding.epyora.setBackgroundResource(R.drawable.roundcorners);
            setChosenExersices();
        });

        binding.ehiihto.setOnClickListener(view -> {
            claji = 3;
            binding.ejuoksu.setBackground(null);
            binding.epyora.setBackground(null);
            binding.ehiihto.setBackgroundResource(R.drawable.roundcorners);
            setChosenExersices();
        });
    }


    public void setChosenExersices(){

        getChosenOnes(new VolleyListener() {
            @Override
            public void jsonResults(JSONArray completedList) {

                journeyNames = new ArrayList<>();
                adapter.notifyDataSetChanged();
                adapter.clear();
                adapter.notifyDataSetChanged();
                if(completedList != null && completedList.length() > 0 ){
                    try {
                        for (int a = 0; a<completedList.length(); a++){

                            int hasProgram = 0;
                            String programInfoText = "Liitä ohjelmaan";

                            String savedDate = completedList.getJSONObject(a).getString("journeydate");
                            String[] dateSplitted = savedDate.split("-", 5);
                            String formattedNewDate = dateSplitted[2] + "."+dateSplitted[1]+"."+dateSplitted[0];

                            completedItem i = new completedItem();
                            i.setMatka(completedList.getJSONObject(a).getString("journeylength"));

                            i.setDate(formattedNewDate);
                            i.setJcoords(completedList.getJSONObject(a).getString("journeycoords"));
                            i.setAika(completedList.getJSONObject(a).getString("duration"));
                            i.setAikaavg(completedList.getJSONObject(a).getString("averagespeed"));
                            i.setJid(completedList.getJSONObject(a).getString("id"));
                            if(!completedList.getJSONObject(a).getString("calories").equals("0 kcal.") && !completedList.getJSONObject(a).getString("calories").equals("null")) {
                                i.setCalories(completedList.getJSONObject(a).getString("calories"));
                            }

                            if(!completedList.getJSONObject(a).getString("jprogram").equals("null")){
                                hasProgram = 1;
                                programInfoText = "Poista liitos ohjelmaan";
                            }

                            i.setHasProgram(hasProgram);
                            i.setPinfoText(programInfoText);

                            journeyNames.add(i);
                        }

                        if(journeyNames != null && journeyNames.size() > 0) {
                            adapter.notifyDataSetChanged();
                            for(int i = 0; i < journeyNames.size(); i++) {
                                adapter.add(journeyNames.get(i));
                            }
                        }

                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
            public void JResults(String jr2) {}
        });
    }
}