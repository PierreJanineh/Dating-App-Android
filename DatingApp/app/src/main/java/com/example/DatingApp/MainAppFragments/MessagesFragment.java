package com.example.DatingApp.MainAppFragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.DatingApp.Chat.Room;
import com.example.DatingApp.ChatActivity;
import com.example.DatingApp.Services.MyDBService;
import com.example.DatingApp.R;
import com.example.DatingApp.Users.MyUser;
import com.example.DatingApp.Users.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagesFragment extends Fragment {
	private static final String TAG = "MessagesActivity";
	
	private ProgressBar progressBar;
    private RecyclerView recyclerView;
    
    private MyDBService.MyLocalBinder binder;
    private MyDBService myService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreate: started.");
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        progressBar = view.findViewById(R.id.progress);
	
	    Bundle bundle = this.getArguments();
	    if (bundle != null) {
		    binder = (MyDBService.MyLocalBinder) bundle.getBinder("binder");
		    myService = binder.getService();
	    }
		new GetChatThreads().execute();
        return view;
    }

    private void initRecyclerView(List<Room> rooms, List<User> users) {
        Log.d(TAG, "initRecyclerView: init recyclerview.");
        View view = progressBar.getRootView();
        RecyclerView recyclerView = view.findViewById(R.id.recyclerv_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(rooms, users);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
    }

    private class GetChatThreads extends AsyncTask<Void, Void, Map<Object, Object>> {
        @Override
        protected Map<Object, Object> doInBackground(Void... voids) {
	        while(myService == null){
		        try {
			        Thread.sleep(2000);
		        } catch (InterruptedException e) {
			        e.printStackTrace();
		        }
	        }
	        Map<Object, Object> map = new HashMap<>();
	        List<Room> rooms = myService.getAllRooms();
	        List<User> users = myService.getUsersFromRooms(rooms);
	        map.put(rooms, users);
            return map;
        }
	
	    @Override
        protected void onPostExecute(Map<Object, Object> map) {
        	List<Room> roomsList = (List<Room>) map.keySet().toArray()[0];
        	List<User> usersList = (List<User>) map.values().toArray()[0];
        	progressBar.setVisibility(View.GONE);
	        recyclerView = getActivity().findViewById(R.id.recyclerv_view);
	        TextView textView = getActivity().findViewById(R.id.noChatLbl);
	        if (roomsList == null || roomsList.isEmpty()) {
		        recyclerView.setVisibility(View.GONE);
		        textView.setVisibility(View.VISIBLE);
	        } else {
		        textView.setVisibility(View.GONE);
		        initRecyclerView(roomsList, usersList);
		        recyclerView.setVisibility(View.VISIBLE);
	        }
        }
	
	    @Override
	    protected void onPreExecute() {
		    progressBar.setVisibility(View.VISIBLE);
	    }
    }
	
	public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
		
		private static final String TAG = "RecyclerViewAdapter";
		private List<Room> rooms;
		private List<User> users;
		
		public RecyclerViewAdapter(List<Room> rooms, List<User> users) {
			this.rooms = rooms;
			this.users = users;
		}
		
		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
			ViewHolder holder = new ViewHolder(view);
			return holder;
		}
		
		@Override
		public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
			Log.d(TAG, "onBindViewHolder: called.");
			
			holder.nameLbl.setText(users.get(position).getName());
			holder.image.setImageResource(R.drawable.ic_launcher_background);
			holder.txtMessages.setText(rooms.get(position).getMessages().get(0).getContent());
			
			holder.parentLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Log.d(TAG, "onClick: clicked on: " + users.get(position).getName());
					
					Intent intent = new Intent(getContext(), ChatActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("other_user", new MyUser(users.get(position)));
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
		}
		
		@Override
		public int getItemCount() {
			return rooms.size();
		}
		
		
		public class ViewHolder extends RecyclerView.ViewHolder{
			
			ImageView image;
			TextView nameLbl, txtMessages;
			ConstraintLayout parentLayout;
			
			public ViewHolder(View itemView) {
				super(itemView);
				image = itemView.findViewById(R.id.image);
				nameLbl = itemView.findViewById(R.id.image_name);
				parentLayout = itemView.findViewById(R.id.parent_layout);
				txtMessages = itemView.findViewById(R.id.txtMessage);
			}
		}
	}
}