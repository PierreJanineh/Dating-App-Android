package com.example.DatingApp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.DatingApp.MainAppFragments.FavouritesFragment;
import com.example.DatingApp.MainAppFragments.MessagesFragment;
import com.example.DatingApp.MainAppFragments.OnlineFragment;
import com.example.DatingApp.Services.MyDBService;
import com.example.DatingApp.Users.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.GeoPoint;
import java.util.List;

public class MainAppActivity extends AppCompatActivity {
	
	public static final String TAG = "MainApp";
	public static final int REQUEST_CODE_LAST_LOCATION = 123;
	
	private Fragment selectedFragment;
	private Activity mainActivity = this;
	private Location thisLocation;
	private LocationRequest locationRequest;
	private LocationCallback locationCallback;
	private boolean shouldStartLocationUpdates = false;
	private FusedLocationProviderClient fusedLocationProviderClient;
	private MyDBService myService;
	private MyDBService.MyLocalBinder binder;
	private Boolean isBound;
	private ServiceConnection serviceConnection;
	private ProgressBar progressBar;
	private TextView progressLbl;
	private List<User> nearbyUsers;
	private User currentUser;

	
	private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FrameLayout mainFrame = findViewById(R.id.mainFrame);
	        Bundle bundle = new Bundle();
            switch (item.getItemId()) {
                case R.id.nav_home:
                    mainActivity.setTitle(R.string.online);
                    selectedFragment = new OnlineFragment();
                    bundle.putBinder("binder", binder);
                    if(!shouldStartLocationUpdates){
                    	startLocationUpdates();
                    }
                    break;
                case R.id.nav_chat:
                    mainActivity.setTitle(R.string.messages);
                    selectedFragment = new MessagesFragment();
	                bundle.putBinder("binder", binder);
                    if(shouldStartLocationUpdates){
                    	stopLocationUpdates();
                    }
                    break;
                case R.id.nav_favs:
                    mainActivity.setTitle(R.string.favourites);
                    selectedFragment = new FavouritesFragment();
	                bundle.putBinder("binder", binder);
	                if(shouldStartLocationUpdates){
		                stopLocationUpdates();
	                }
                    break;
            }
            selectedFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, selectedFragment).commit();
            return true;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app);
        
	    serviceConnection = new ServiceConnection() {
		    @Override
		    public void onServiceConnected(ComponentName name, IBinder service) {
			    binder = (MyDBService.MyLocalBinder) service;
			    myService = binder.getService();
			    isBound = true;
		    }
		
		    @Override
		    public void onServiceDisconnected(ComponentName name) {
			    isBound = false;
		    }
	    };
	    
	    Intent intent = new Intent(MainAppActivity.this, MyDBService.class);
	    bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	    
        locationCallback = new LocationCallback(){
		
		    @Override
		    public void onLocationResult(LocationResult locationResult) {
			    List<Location> locationList = locationResult.getLocations();
			    if (locationList.size() > 0) {
				    //The last location in the list is the newest
				    Location location = locationList.get(locationList.size() - 1);
				    Log.i(TAG, "Location: " + location.getLatitude() + " " + location.getLongitude());
				    thisLocation = location;
				    SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
				    String id = sharedPreferences.getString("firestore_uid_db", null);
				    sharedPreferences.edit().putString("location", thisLocation.getLatitude() + "," + thisLocation.getLongitude()).commit();

			    }
			    if (selectedFragment == null) {
				    selectedFragment = new OnlineFragment();
				    Bundle bundle = new Bundle();
				    bundle.putBinder("binder", binder);
				    selectedFragment.setArguments(bundle);
				    getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, selectedFragment).commit();
			    }
		    }
	    };

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        progressBar = findViewById(R.id.progressBarFragments);
        progressLbl = findViewById(R.id.progressLabel);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

	    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
	    getLocation();



    }

	private void getLocation() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			
			ActivityCompat.requestPermissions(this,
					new String[]{
							Manifest.permission.ACCESS_COARSE_LOCATION,
							Manifest.permission.ACCESS_FINE_LOCATION
					},
					REQUEST_CODE_LAST_LOCATION);
			
		}else {
			createLocationRequest();
			startLocationUpdates();
			new UpdateDBField().execute(thisLocation);
		}
	}
	private void createLocationRequest() {
		if (locationRequest == null)
			locationRequest = LocationRequest.create();
		locationRequest.setInterval(5000);
		locationRequest.setFastestInterval(5000);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}
	@SuppressLint("MissingPermission")
	private void startLocationUpdates() {
		shouldStartLocationUpdates = true;
		fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
	}
	
	private void stopLocationUpdates() {
		shouldStartLocationUpdates = false;
		fusedLocationProviderClient.removeLocationUpdates(locationCallback);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		stopLocationUpdates();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		stopLocationUpdates();
		// Unbind from the service
		if (isBound) {
			unbindService(serviceConnection);
			isBound = false;
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopLocationUpdates();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (shouldStartLocationUpdates)
			startLocationUpdates();
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_CODE_LAST_LOCATION) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				getLocation();
			}
		}
	}

	public class UpdateDBField extends AsyncTask<Location, Integer, Void>{

		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
			progressLbl.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onProgressUpdate(Integer... ints) {
			switch (ints[0]){
				case 1:
					progressLbl.setText("Getting location updates...");
					break;
				case 2:
					progressLbl.setText("Preparing users' info...");
					break;
				case 3:
					progressLbl.setText("Preparing nearby users...");
					break;
			}
		}

		@Override
		protected Void doInBackground(Location... locations) {
			publishProgress(1);
			Log.d(TAG, "doInBackground: published 1");
			updateLocationInDB(locations[0]);

			publishProgress(2);
			Log.d(TAG, "doInBackground: published 2");
			defineCurrentUser();

			publishProgress(3);
			Log.d(TAG, "doInBackground: published 3");
			defineNearbyUsers();
			return null;
		}

		private void updateLocationInDB(Location location1) {
			if(location1 == null){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				updateLocationInDB(thisLocation);
			}else{
				myService.updateLocationFieldInUsersTokens("latlng", new GeoPoint(location1.getLatitude(), location1.getLongitude()));
			}
		}

		private void defineCurrentUser() {
			currentUser = myService.getCurrentUser();
			if(currentUser == null){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				defineCurrentUser();
			}
		}

		private void defineNearbyUsers() {
			nearbyUsers = myService.getNearbyUsers();
			if(nearbyUsers == null){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				defineNearbyUsers();
			}
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			progressBar.setVisibility(View.GONE);
			progressLbl.setVisibility(View.GONE);
		}
	}

	
}
