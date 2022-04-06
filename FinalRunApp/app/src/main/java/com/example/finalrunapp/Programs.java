package com.example.finalrunapp;

import androidx.annotation.RequiresApi;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.finalrunapp.databinding.ActivityProgramsBinding;
import org.json.JSONArray;
import org.json.JSONException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
public class Programs extends ListActivity {

    private ActivityProgramsBinding binding;
    private Integer puserid;
    private String journeyid;
    private TextView chosenDate;
    private DatePickerDialog.OnDateSetListener dateListener;
    private String formattedDateTime;

    private ProgramsAdapter adapter;
    private ArrayList<oneProgram> dayprogs;


    private class oneProgram {

        private String trainingDuration;
        private String header;
        private String description;
        private String programid;

        public String getTrainingDuration() { return trainingDuration; }
        public void setTrainingDuration(String trainingDuration) { this.trainingDuration = trainingDuration; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getHeader() { return header; }
        public void setHeader(String header) { this.header = header; }
        public String getProgramid() { return programid; }
        public void setProgramid(String programid) { this.programid = programid; }


    }

    private class ProgramsAdapter extends ArrayAdapter<oneProgram> {

        private ArrayList<oneProgram> items;

        public ProgramsAdapter(Context context, int textViewResourceId, ArrayList<oneProgram> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.program_item, null);
            }

            oneProgram item = items.get(position);
            if (item != null) {
                TextView itemdate = v.findViewById(R.id.programClock);
                TextView itemdescr = v.findViewById(R.id.programDesc);
                TextView itemSubject = v.findViewById(R.id.programSubject);

                if (itemdate != null) { itemdate.setText(item.getTrainingDuration());  }
                if (itemdescr != null) { itemdescr.setText(item.getDescription());  }
                if (itemSubject != null) { itemSubject.setText(item.getHeader());  }

                Button btnAddP = v.findViewById(R.id.programAddBtn);

                btnAddP.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Integer pid = Integer.parseInt(item.getProgramid());
                        Integer jid = Integer.parseInt(journeyid);

                        linkJourneyToProgram(jid,pid,  new VolleyListener() {
                            @Override
                            public void JResults(String loginResult) {

                                Bundle b = new Bundle();
                                b.putInt("userID", puserid);

                                Intent completede = new Intent(Programs.this, CompletedExercises.class);
                                completede.putExtras(b);
                                startActivity(completede);

                            }
                            public void jsonResults(JSONArray loginResult) {}
                        });
                    }
                });
            }
            return v;
        }
    }

    public void linkJourneyToProgram(Integer jid, Integer pid, final VolleyListener callBack){

        String url = "https://aleksi-kuntokirja.herokuapp.com/api/linkJourneyToProgram/" + pid + "/" + jid + "/" + formattedDateTime;
        StringRequest sr = new StringRequest(Request.Method.POST,url,
                callBack::JResults,
                error -> callBack.JResults("ERROR"));

        MySingleton.getInstance(this).addToRequestQueue(sr);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programs);

        Bundle bundle = getIntent().getExtras();
        puserid  = bundle.getInt("userid");
        journeyid  = bundle.getString("journeyid");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDateTime now = LocalDateTime.now();
        formattedDateTime = now.format(dtf);

        binding = ActivityProgramsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dayprogs = new ArrayList<>();
        adapter = new ProgramsAdapter(this, R.layout.program_item, dayprogs);
        setListAdapter(adapter);

        setUpDateDialogue();

    }


    @Override
    public void onResume() {
        super.onResume();
        String date = chosenDate.getText().toString();
        if(!date.equalsIgnoreCase("valitse päivä")) {
            getChosenPrograms(date);
        }
    }

    private void setUpDateDialogue() {
        chosenDate = findViewById(R.id.dateofprogram);
        chosenDate.setText(formattedDateTime);

        chosenDate.setOnClickListener(view -> {

            int yyyy;
            int mm;
            int dd;

            if(chosenDate.getText().toString().equalsIgnoreCase("valitse päivä")) {
                Calendar calender = Calendar.getInstance();
                yyyy = calender.get(Calendar.YEAR);
                mm = calender.get(Calendar.MONTH);
                dd = calender.get(Calendar.DAY_OF_MONTH);
            } else {
                String[] dateParts = chosenDate.getText().toString().split("\\.");
                yyyy = Integer.parseInt(dateParts[2]);
                mm = Integer.parseInt(dateParts[1]) - 1;
                dd = Integer.parseInt(dateParts[0]);
            }

            DatePickerDialog dialog = new DatePickerDialog(
                    Programs.this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    dateListener,
                    yyyy, mm, dd);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        dateListener = (datePicker, yyyy, mm, dd) -> {

            mm = mm + 1;
            String date;

            if(mm < 10) {
                date = dd + ".0" + mm + "." + yyyy;
            } else {
                date = dd + "." + mm + "." + yyyy;
            }

            if(dd < 10) {
                date = "0" + date;
            }
            formattedDateTime = date;
            chosenDate.setText(date);

            getChosenPrograms(date);
        };
    }

    public void getChosenOnes(String cdate, final VolleyListener callBack){

        String url = "https://aleksi-kuntokirja.herokuapp.com/api/getUserProgramsAndResultsWithDates/" + cdate + "/" + cdate + "/" + puserid;

        JsonArrayRequest jsonArrRequest = new JsonArrayRequest
                (Request.Method.GET, url, null,
                        callBack::jsonResults,
                        error -> Log.d("MyApp", "ERROR " + error));
        MySingleton.getInstance(this).addToRequestQueue(jsonArrRequest);


    }

    public void getChosenPrograms(String chosenDate){

        String[] dateParts = chosenDate.split("\\.");
        chosenDate = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];


        getChosenOnes(chosenDate, new VolleyListener() {
            @Override
            public void jsonResults(JSONArray completedList) {

                dayprogs = new ArrayList<>();
                adapter.notifyDataSetChanged();
                adapter.clear();
                adapter.notifyDataSetChanged();

                if(completedList != null && completedList.length() > 0 ){

                    try {
                        JSONArray arr = completedList.getJSONArray(0);
                        JSONArray arr2 = completedList.getJSONArray(1);


                        for (int a=0;a<arr2.length();a++){

                            if(arr2.getJSONArray(a).length() > 0) {
                                //Ei oteta ohjelmia mukaan, joilla on jo liitetty jokin harjoitus
                                if(arr.getJSONObject(a).getJSONArray("recordedJourneys").length() == 0){
                                    oneProgram i = new oneProgram();

                                    //Lasketaan kellonajat datasta
                                    String endString, startString;
                                    int startT = Integer.parseInt(arr.getJSONObject(a).getString("startTime"));
                                    int halfT = Integer.parseInt(arr.getJSONObject(a).getString("half"));
                                    double durationT = Double.parseDouble(arr.getJSONObject(a).getString("duration"));

                                    double endTime = durationT * 0.5 + startT;
                                    if (halfT == 1) {
                                        endTime += 0.5;
                                        startString = startT + ":30";
                                    } else startString = startT + ":00";

                                    if (endTime % 1 != 0) {
                                        endTime -= 0.5;
                                        endString = (int) endTime + ":30";
                                    } else {
                                        endString = (int) endTime + ":00";
                                    }

                                    String trainingDur = startString + " - " + endString;

                                    i.setProgramid(arr.getJSONObject(a).getString("id"));
                                    i.setDescription(arr.getJSONObject(a).getString("subject"));
                                    i.setHeader(arr.getJSONObject(a).getString("program"));
                                    i.setTrainingDuration(trainingDur);

                                    dayprogs.add(i);
                                }
                            }
                        }

                        if(dayprogs != null && dayprogs.size() > 0) {
                            adapter.notifyDataSetChanged();
                            for(int i = 0; i < dayprogs.size(); i++) {
                                adapter.add(dayprogs.get(i));
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