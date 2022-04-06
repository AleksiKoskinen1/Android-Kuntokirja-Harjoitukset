package com.example.finalrunapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.finalrunapp.databinding.FragmentLoginBinding;

import org.json.JSONArray;
import org.json.JSONException;

public class Login extends Fragment {

    private FragmentLoginBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String username = binding.usernameField.getText().toString();
                String password = binding.passwordField.getText().toString();

                ((MainActivity) getActivity()).getLogin(username, password, new VolleyListener() {
                    @Override
                    public void jsonResults(JSONArray loginResult) {

                        if(loginResult != null && loginResult.length() > 0 ){
                            try {
                                String user = loginResult.getJSONObject(0).getString("name");
                                String userID = loginResult.getJSONObject(0).getString("id");

                                Bundle b = new Bundle();
                                b.putString("username", user);
                                b.putInt("userID", Integer.parseInt(userID));
                                Intent mainact = new Intent(getActivity(), ValikkoActivity.class);

                                mainact.putExtras(b);
                                startActivity(mainact);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            Toast myToast = Toast.makeText(getActivity(), "Väärä käyttäjätunnus ja/tai salasana", Toast.LENGTH_SHORT);
                            myToast.show();
                        }
                    }
                    public void JResults(String res) {}
                });
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}