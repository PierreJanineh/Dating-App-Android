package com.example.DatingApp.MainAppFragments;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.DatingApp.Services.MyDBService;
import com.example.DatingApp.Adapters.OnlineRecyclerViewAdapterBig;
import com.example.DatingApp.R;
import com.example.DatingApp.Users.User;

import java.util.List;

public class FavouritesFragment extends Fragment {
    private User user;
    private MyDBService myService;
    private RecyclerView recyclerView;
    private TextView txtView;
    private ProgressBar progressBar;
    private Boolean carryOn;
    private List<String> favs;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @NonNull ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        carryOn = false;
        new GetCurrentUser().execute();
        View view = inflater.inflate(R.layout.fragment_favourites, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        txtView = view.findViewById(R.id.noFavsLbl);
        progressBar = view.findViewById(R.id.progress_bar);
        
        Bundle bundle = this.getArguments();
        MyDBService.MyLocalBinder binder = (MyDBService.MyLocalBinder) bundle.getBinder("binder");
        myService = binder.getService();
        new GetUsersFromDB().execute();
        return view;
    }

    private void initRecyclerView(List<User> users) {
        RecyclerView onlineUsersView = getActivity().findViewById(R.id.recycler_view);

        OnlineRecyclerViewAdapterBig adapterOnline = new OnlineRecyclerViewAdapterBig(getContext(), users);
        onlineUsersView.setAdapter(adapterOnline);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),3);
        onlineUsersView.setLayoutManager(gridLayoutManager);
    }
    
    public class GetUsersFromDB extends AsyncTask<Void, Void, List<User>>{
    
        @Override
        protected List<User> doInBackground(Void... voids) {
            while (!carryOn){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (user.getFavs() != null) {
                favs = user.getFavs();
                if (favs.isEmpty()) {
                    return null;
                }
            }else{
                return null;
            }
            
            List<User> users = myService.getUsersByFIRE(favs);
            return users;
        }
    
        @Override
        protected void onPostExecute(List<User> users) {
            if (users == null){
                txtView.setVisibility(View.VISIBLE);
            }else{
                initRecyclerView(users);
                recyclerView.setVisibility(View.VISIBLE);
            }
            progressBar.setVisibility(View.GONE);
            
        }
    }
    
    public class GetCurrentUser extends AsyncTask<Void, Void, Void>{
    
        @Override
        protected Void doInBackground(Void... voids) {
            while (myService == null){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            user = myService.getCurrentUser();
            carryOn = true;
            return null;
        }
    }
}
