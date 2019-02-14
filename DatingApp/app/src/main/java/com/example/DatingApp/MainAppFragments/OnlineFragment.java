package com.example.DatingApp.MainAppFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.DatingApp.Services.MyDBService;
import com.example.DatingApp.Adapters.OnlineRecyclerViewAdapterBig;
import com.example.DatingApp.R;
import com.example.DatingApp.Users.User;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class OnlineFragment extends Fragment{
	private static final String TAG = "OnlineActivity";
    public static final String USERS_TOKENS_DB = "users_tokens";
    public static final String USER_SP = "user";
    public static final String FIRESTORE_UID_SP = "firestore_uid_db";
    public static final String LATLNG_DB = "latlng";
    public static final String UID_DB = "uid";
    public static final String NAME_DB = "name";
    public static final String IMG_URL_DB = "img_url";
	public static final String LOCATION_SP = "location";
	private SwipeRefreshLayout swipeRefreshLayout;
	private ProgressBar progressBarOnline;
	private RecyclerView recyclerView;
    private GeoPoint thisLocation;
	private Boolean isBound, shouldContinue;
	private View view;
    private MyDBService myService;
	private MyDBService.MyLocalBinder binder;
	private List<User> nearbyUsers;

	public OnlineFragment(){

	}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
	    if (bundle != null) {
		    binder = (MyDBService.MyLocalBinder) bundle.getBinder("binder");
		    myService = binder.getService();
	    }
	
	    view = inflater.inflate(R.layout.fragment_online, container, false);
	    
        return view;
    }
	
	@Override
	public void onStart() {
		super.onStart();
		recyclerView = view.findViewById(R.id.recyclerv_view);
		progressBarOnline = view.findViewById(R.id.progressBarOnline);
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences(USER_SP,Context.MODE_PRIVATE);
		String locationString = sharedPreferences.getString(LOCATION_SP,null);
		String[] latLng = locationString.split(",");
		thisLocation = new GeoPoint(Double.valueOf(latLng[0]),Double.valueOf(latLng[1]));
		
		//Set Swipe for Refresh
		swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
			@Override
			public void onRefresh(){
//              TODO
				new RefreshTask().execute();
			}
		});
		shouldContinue = false;
		new WaitAndAskAgain().execute();
	}
	
	private void initRecyclerView(List<User> users) {
        Log.d(TAG, "initRecyclerView: init recyclerview.");
        RecyclerView onlineUsersView = getActivity().findViewById(R.id.recyclerv_view);
        RecyclerView newUsersView = getActivity().findViewById(R.id.recycler_view);
        
        

        OnlineRecyclerViewAdapterBig adapterOnline = new OnlineRecyclerViewAdapterBig(getContext(), users);
        onlineUsersView.setAdapter(adapterOnline);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        onlineUsersView.setLayoutManager(gridLayoutManager);

        /*OnlineRecyclerViewAdapter adapterNew = new OnlineRecyclerViewAdapter(getContext(), mNames, mImageUrls, mDistances);
        newUsersView.setAdapter(adapterNew);
        newUsersView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));*/
    }
    
    private class RefreshTask extends AsyncTask<Void,Void,Void>{
	
	    @Override
	    protected Void doInBackground(Void... voids) {
	    	myService.refreshNearbyUsers();
		    return null;
	    }
    }
    
    private class WaitAndAskAgain extends AsyncTask<Void,Void,Void>{

	    @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBarOnline.setVisibility(View.VISIBLE);
	    }

        @Override
	    protected Void doInBackground(Void... voids) {
            while(myService == null){
	            try {
		            Thread.sleep(2000);
	            } catch (InterruptedException e) {
		            e.printStackTrace();
	            }
            }
            nearbyUsers = myService.getNearbyUsers();
            if (nearbyUsers != null)
                shouldContinue = true;
            else{
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new WaitAndAskAgain().doInBackground();
                shouldContinue = false;
            }
            return null;
	    }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBarOnline.setVisibility(View.GONE);
            initRecyclerView(nearbyUsers);
        }
    }
}