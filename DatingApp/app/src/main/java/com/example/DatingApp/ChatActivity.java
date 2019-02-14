package com.example.DatingApp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.DatingApp.Adapters.ChatThreadsRecyclerViewAdapter;
import com.example.DatingApp.Chat.Message;
import com.example.DatingApp.Chat.Room;
import com.example.DatingApp.Services.MyDBService;
import com.example.DatingApp.Users.MyUser;
import com.example.DatingApp.Users.User;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.example.DatingApp.Connection.HttpRequest.httpPostFCM;

public class ChatActivity extends AppCompatActivity {

    public static final String TAG = "ChatActivity";
    public static final String ROOMS_DB = "rooms";
	public static final String MESSAGES_DB = "messages";
	public static final String USERS_TOKENS_DB = "users_tokens";
	public static final String USER_SP = "user";
	public static final String USER_UID_SP = "user_uid";
	
	private Button sendBtn;
	private ProgressBar progressBar;
	private EditText msg;
	private User userQueried;
	private MyUser otherUser;
    private FirebaseFirestore mDatabase;
    private ChatThreadsRecyclerViewAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView listOfMessages;
    private String room, uid, newChatUserUID;
    private String message;

    private MyDBService myService;
    private ServiceConnection serviceConnection;
    private Boolean isBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				MyDBService.MyLocalBinder binder = (MyDBService.MyLocalBinder) service;
				myService = binder.getService();
				isBound = true;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				isBound = false;
			}
		};

        Intent intent = new Intent(ChatActivity.this, MyDBService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        
        if(savedInstanceState == null) {
		        Bundle extras = getIntent().getExtras();
	        if (extras != null) {
		        otherUser = (MyUser) extras.getSerializable("other_user");
		        room = extras.getString("room");
	        }
        }
        setContentView(R.layout.activity_chat);

	    uid = getSharedPreferences(USER_SP, MODE_PRIVATE).getString(USER_UID_SP, null);
        View view = findViewById(R.id.lyout);
        msg = findViewById(R.id.inptMessage);
        listOfMessages = findViewById(R.id.list_of_messages);
        progressBar = findViewById(R.id.progressBarSendMsg);
        sendBtn = findViewById(R.id.sendBtn);
        
        linearLayoutManager = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL,
                false);
        listOfMessages.setLayoutManager(linearLayoutManager);

        new CheckRoomsValidity().execute();

        if(otherUser != null){
	        new GetUserInfo().execute(otherUser.getUid());
        }
    }

    private class GetChatList extends AsyncTask<Void, Void, Void> {
	
	    @Override
		protected Void doInBackground(Void... voids) {
			while (myService == null) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

			Room rm = myService.getChatList(room);
			List<Message> msgs = rm.getMessages();
            setIsItMeByToFrom(msgs);
            adapter = new ChatThreadsRecyclerViewAdapter(rm.getMessages());
			adapter.notifyDataSetChanged();
			listOfMessages.setAdapter(adapter);

			return null;
		}

	}

	private class CheckRoomsValidity extends AsyncTask<Void, Void, List<Message>> {

	    @Override
		protected List<Message> doInBackground(Void... voids) {
			while (myService == null) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(room == null){
                List<String> rooms = otherUser.getMessages();
                List<String> myRooms = myService.getCurrentUser().getMessages();
                for (int i = 0; i < rooms.size(); i++) {
                    for (int j = 0; j < myRooms.size(); j++) {
                        if (rooms.get(i).equals(myRooms.get(j))){
                            room = rooms.get(i);
                        }
                    }
                }
            }
            
            while (room == null){
	            try {
		            Thread.sleep(2000);
	            } catch (InterruptedException e) {
		            e.printStackTrace();
	            }
            }
            
		    List<Message> msgs = myService.getRoom(room).getMessages();
		    setIsItMeByToFrom(msgs);
			return msgs;
		}
		
		@Override
		protected void onPostExecute(List<Message> msgs) {
			adapter = new ChatThreadsRecyclerViewAdapter(msgs);
			adapter.notifyDataSetChanged();
			listOfMessages.setAdapter(adapter);
		}
	}

    public void sendMsg(View view) throws JSONException {
        Log.d(TAG, "sendMsg: entered");

        // Read the input field and push a new instance
        // of ChatMessage to the Firebase database
        message = msg.getText().toString().trim();

//        String targetedToken = "fMOApYyHu2g:APA91bGkGyDHcKBWgQNWt_tbPdYimb1fEJAvMDlDBv0TrrW1VoRt79q9Necxv4VuAaU37bpM0OI01yvnXx5j9eaGVjDqC8fm6DZcvnnGyeLylkqB4S-ggKc1hyvlgb7EcHim6ON-LSBp";
	    String targetedToken = otherUser.getToken();
        String targetedName = "New message";
        JSONObject jsonObj = new JSONObject();
        JSONObject jsonData = new JSONObject();
        jsonData.put("title", targetedName);
        jsonData.put("body", message);
        jsonObj.put("data", jsonData);
        jsonObj.put("to", targetedToken);
        new SendMessageTask().execute(jsonObj.toString());


        Log.d(TAG, "sendMsg: after firebase action");
        // Clear the input
        msg.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        adapter.stopListening();
    }
    @Override
    protected void onStart() {
        super.onStart();
//        adapter.startListening();
    }

    private class SendMessageTask extends AsyncTask<String, Void, List<Message>> {
	    @Override
	    protected void onPreExecute() {
		    super.onPreExecute();
		    progressBar.setVisibility(View.VISIBLE);
		    sendBtn.setVisibility(View.GONE);
	    }
	
	    @Override
        protected List<Message> doInBackground(String... strings) {
	        //Send a notification
            String url = "https://fcm.googleapis.com/fcm/send";
            try {
                httpPostFCM(url,strings[0]);
            } catch (IOException e) {
                Log.d("MainActivity","error on catch doInBackground"+e);
            }
            //loop until service is available
            while (myService == null){
	            try {
		            Thread.sleep(2000);
	            } catch (InterruptedException e) {
		            e.printStackTrace();
	            }
            }
            Message message1 = new Message(message,
                    Timestamp.now(),
                    Arrays.asList(otherUser.getUid(), uid));
            String roomID = null;
		    if(room != null
                    && otherUser.getToken() != null
                    && otherUser.getUid() != null){
		        myService.addMessagetoARoom(room, message1);
		        roomID = room;

		    }else if(otherUser != null) {
			    roomID = myService.addNewRoomAndMessage(
					    otherUser.getDocID(),
					    otherUser.getUid(),
					    message1);
		    }
		    List<Message> msgs = myService.getRoom(roomID).getMessages();
            setIsItMeByToFrom(msgs);

            return msgs;
        }

        @Override
        protected void onPostExecute(List<Message> msgs) {
            super.onPostExecute(msgs);
            sendBtn.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            adapter = new ChatThreadsRecyclerViewAdapter(msgs);
            adapter.notifyDataSetChanged();
            listOfMessages.setAdapter(adapter);
        }
    }

    private void setIsItMeByToFrom(List<Message> msgs) {
        for (Message msg : msgs) {
            if (msg.getTofrom().get(0).equals(uid)) {
                msg.setIsItMe(false);
            } else {
                msg.setIsItMe(true);
            }
        }
    }

    private class GetUserInfo extends AsyncTask<String, Void, User> {
        @Override
        protected User doInBackground(String... strings) {
        	while(myService == null){
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
            return myService.getUserByUID(strings[0]);
        }
    }
	
	@Override
	protected void onStop() {
		super.onStop();
		if (isBound) {
			unbindService(serviceConnection);
		}
	}
}
