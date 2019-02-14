package com.example.DatingApp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.example.DatingApp.Services.MyDBService;
import com.example.DatingApp.Users.UserInfo;

import java.util.Map;

public class ProfileEditorActivity extends AppCompatActivity{
    private String user;
    private int image, height, weight;
    private String about, birthDate, ethnicity, relationship, religion, orientation, reference, stdS, role, firestore;
    private EditText txtAbout;
    private NumberPicker pkrDay, pkrMonth, pkrYear, pkrHeight, pkrWeight;
    private Spinner spnrEthnicity, spnrReligion, spnrRelationship, spnrOrientation, spnrReference, spnrSTDs, spnrRole;
    private ArrayAdapter<CharSequence> arrayAdapter;
    private SharedPreferences sharedPreferences;
    private String userId;
    private ProgressBar progressBar;

    private MyDBService myService;
    private Boolean isBound;
    private ServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_editor);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                isBound = true;
                MyDBService.MyLocalBinder binder = (MyDBService.MyLocalBinder) service;
                myService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBound = false;
            }
        };
        Intent intent = new Intent(ProfileEditorActivity.this, MyDBService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        txtAbout = findViewById(R.id.about);
        pkrDay = findViewById(R.id.day);
        pkrMonth = findViewById(R.id.month);
        pkrYear = findViewById(R.id.year);
        pkrHeight = findViewById(R.id.height);
        pkrWeight = findViewById(R.id.weight);
        spnrEthnicity = findViewById(R.id.ethnicity);
        spnrOrientation = findViewById(R.id.orientation);
        spnrRelationship = findViewById(R.id.relationship);
        spnrReligion = findViewById(R.id.religion);
        spnrReference = findViewById(R.id.reference);
        spnrSTDs = findViewById(R.id.stds);
        spnrRole = findViewById(R.id.role);
        progressBar = findViewById(R.id.progressBarInfo);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        new SetValuesTask().execute();

        pkrDay.setMinValue(1);
        pkrDay.setMaxValue(31);
        pkrMonth.setMinValue(1);
        pkrMonth.setMaxValue(12);
        pkrYear.setMinValue(1930);
        pkrYear.setMaxValue(2001);
        pkrYear.setValue(1995);
        pkrHeight.setMinValue(100);
        pkrHeight.setMaxValue(230);
        pkrWeight.setMinValue(40);
        pkrWeight.setMaxValue(200);

        //Ethnicity Spinner
        arrayAdapter = ArrayAdapter.createFromResource(this, R.array.ethnicity, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrEthnicity.setAdapter(arrayAdapter);
	    
        //Reference Spinner
        arrayAdapter = ArrayAdapter.createFromResource(this, R.array.reference, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrReference.setAdapter(arrayAdapter);
	
	    //Religion Spinner
        arrayAdapter = ArrayAdapter.createFromResource(this, R.array.religion, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrReligion.setAdapter(arrayAdapter);
	
	    //Relationship Spinner
        arrayAdapter = ArrayAdapter.createFromResource(this, R.array.relationship, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrRelationship.setAdapter(arrayAdapter);
	
	    //Orientation Spinner
        arrayAdapter = ArrayAdapter.createFromResource(this, R.array.orientation, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrOrientation.setAdapter(arrayAdapter);
	
	    //STDs Spinner
        arrayAdapter = ArrayAdapter.createFromResource(this, R.array.stds, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrSTDs.setAdapter(arrayAdapter);

        //Role Spinner
        arrayAdapter = ArrayAdapter.createFromResource(this, R.array.role, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrRole.setAdapter(arrayAdapter);
    }


    public void editProfile(View view) {
        about = txtAbout.getText().toString();
        birthDate = pkrDay.getValue() + "/" + pkrMonth.getValue() + "/" + pkrYear.getValue();
        height = pkrHeight.getValue();
        weight = pkrWeight.getValue();
        ethnicity = String.valueOf(spnrEthnicity.getSelectedItemPosition());
        relationship = String.valueOf(spnrRelationship.getSelectedItemPosition());
        religion = String.valueOf(spnrReligion.getSelectedItemPosition());
        reference = String.valueOf(spnrReference.getSelectedItemPosition());
        orientation = String.valueOf(spnrOrientation.getSelectedItemPosition());
        stdS = String.valueOf(spnrSTDs.getSelectedItemPosition());
        role = String.valueOf(spnrRole.getSelectedItemPosition());

        UserInfo userInfo = new UserInfo(
		        userId,
                about,
                birthDate,
                height,
                weight,
                relationship,
                religion,
                orientation,
                role,
                ethnicity,
                reference,
                stdS);
        new EditProfileTask().execute(userInfo);
    }

    private class EditProfileTask extends AsyncTask<UserInfo, Void, Boolean> {

        @Override
        protected Boolean doInBackground(UserInfo... userInfos) {
            if(userInfos == null || userInfos.length != 1)
                return null;
            
            while(myService == null){
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            myService.updateInfoFieldInUsersTokens(userInfos[0]);

	        return null;
        }
    }

    private class SetValuesTask extends AsyncTask<Void, Void, Map<Object, Object>>{

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Map<Object, Object> doInBackground(Void... voids) {
            while(myService == null){
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Map<Object, Object> map = null;
            if( myService.getCurrentUser().getInfo() != null){
                map =  myService.getCurrentUser().getInfo().mapOfUserInfo();
            }
            return map;
        }

        @Override
        protected void onPostExecute(Map<Object, Object> map) {

            if(map != null) {
                String[] split = ((String) map.get("birthdate")).split("/");

                txtAbout.setText((String) map.get("about"));
                pkrHeight.setValue((int) map.get("height"));
                pkrWeight.setValue((int) map.get("weight"));

                pkrDay.setValue(Integer.valueOf(split[0]));
                pkrMonth.setValue(Integer.valueOf(split[1]));
                pkrYear.setValue(Integer.valueOf(split[2]));

                spnrEthnicity.setSelection(Integer.valueOf((String) map.get("ethnicity")));
                spnrRelationship.setSelection(Integer.valueOf((String) map.get("relationship")));
                spnrReference.setSelection(Integer.valueOf((String) map.get("reference")));
                spnrReligion.setSelection(Integer.valueOf((String) map.get("religion")));
                spnrOrientation.setSelection(Integer.valueOf((String) map.get("orientation")));
                spnrSTDs.setSelection(Integer.valueOf((String) map.get("STDs")));
                spnrRole.setSelection(Integer.valueOf((String) map.get("role")));
            }
            progressBar.setVisibility(View.GONE);
        }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(serviceConnection);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
        }
    }
}
