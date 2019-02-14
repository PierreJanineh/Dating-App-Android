package com.example.DatingApp.Services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.DatingApp.Chat.Message;
import com.example.DatingApp.Chat.Room;
import com.example.DatingApp.Users.User;
import com.example.DatingApp.Users.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MyDBService extends Service {

    public static final String USERS_TOKENS_DB = "users_tokens";
    public static final String USER_SP = "user";
    public static final String FIRESTORE_UID_SP = "firestore_uid_db";
    public static final String ROOMS_DB = "rooms";
    public static final String MESSAGES_DB = "messages";
    public static final String LATLNG_DB = "latlng";

    public static final String KILOMETERS = "K";
    public static final String NAUTICAL_MILES = "N";
    public static final String MILES = "M";
    public static final String UID_DB = "uid";
    public static final int NEW_DEVICE = 1;
    public static final int NORMAL_STATE = 10;
    public static final int NEW_USER = 2;
    public static final String TAG = "GeoQuery";
    public static final String INFO_DB = "info";
    private static final String ACTION_UPDATE_USER_LOCATION = "com.example.myapplication.action.UPDATE_USER_LOCATION";
    public static final String FAVS_DB = "favs";
    private SharedPreferences sp;
    private int returnVal, index;
    private User currentUser, otherUser;
    private FirebaseUser currentFirebaseUser;
    private Message message;
    private List<Message> messages;
    private Room room;
    private List<Room> rooms;
    private String uid, firestore, roomUID, otherFirestore;
    private List<User> nearbyUsers;
    private Boolean shouldContinue, isComplete;
    private Boolean gotCurrentUser = false, gotNearbyUsers = false, gotAllRooms = false, gotRoom = false;
    private int userState;
    private IBinder myBinder = new MyLocalBinder();
    private Object tsk;
    private List<User> favUsersList;

    public MyDBService() {
    }

    private static int distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit == KILOMETERS) {
                dist = dist * 1.609344;
            } else if (unit == NAUTICAL_MILES) {
                dist = dist * 0.8684;
            } else if (unit == MILES) {
                //return distance
            }

            return (int) dist;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sp = getSharedPreferences(USER_SP, MODE_PRIVATE);
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        getFirestoreDocumentID();

        return super.onStartCommand(intent, flags, startId);
    }

    private void getFirestoreDocumentID() {
        firestore = sp.getString(FIRESTORE_UID_SP, null);
        if (firestore == null) {
            FirebaseFirestore.getInstance()
                    .collection(USERS_TOKENS_DB)
                    .whereEqualTo(UID_DB, currentFirebaseUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        firestore = task.getResult().getDocuments().get(0).getId();
                        getCurrentUser(true);
                    }
                }
            });
            userState = NEW_DEVICE;
        } else {
            getCurrentUser(true);
        }
    }

    public List<Room> getAllRooms() {
        gotAllRooms = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                getAllRoomsAndMessages(getCurrentUser());
            }
        }).start();
        while(!gotAllRooms){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return rooms;
    }

    public Room getRoom(String roomUID){
        gotRoom = false;
        tsk = FirebaseFirestore.getInstance()
                .collection(ROOMS_DB)
                .document(roomUID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            room = task.getResult().toObject(Room.class);
                            String roomID = task.getResult().getId();
                            room.setRoomUID(roomID);
                            gotRoom = true;
                        } else {
                            //TODO
                        }
                    }
                });
        while (!gotRoom){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return room;
    }
    
    public List<User> getUsersFromRooms(List<Room> roomsList){

        int length = roomsList.toArray().length;
        List<String> uidsList = new ArrayList<>();
        List<User> usersList = new ArrayList<>();
        if (length > 1) {
            Log.d(TAG, "getUsersFromRooms: before first for");
            for (Room rm : roomsList) {

                Log.d(TAG, "getUsersFromRooms: in first for");
                if (!rm.getTofrom().get(0).equals(uid)){
                    uidsList.add(rm.getTofrom().get(0));
                }else{
                    uidsList.add(rm.getTofrom().get(1));
                }
            }
            Log.d(TAG, "getUsersFromRooms: before second for");
            for (int i = 0; i < uidsList.size(); i++) {

                Log.d(TAG, "getUsersFromRooms: in first for");
                usersList.add(getUserByUID(uidsList.get(i)));
            }
        }else{
            Log.d(TAG, "else if");
            if (!roomsList.get(0).getTofrom().get(0).equals(uid)){
                uidsList.add(roomsList.get(0).getTofrom().get(0));
            }else{
                uidsList.add(roomsList.get(0).getTofrom().get(1));
            }

            usersList.add(getUserByUID(uidsList.get(0)));
            Log.d(TAG, "getUsersFromRooms: "+ usersList.toArray().toString());
        }

        return usersList;
    }

    public String getFirestoreDocumentID(String uId) {
        otherFirestore = null;
        FirebaseFirestore.getInstance()
                .collection(USERS_TOKENS_DB)
                .whereEqualTo(UID_DB, uId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                            otherFirestore = task.getResult().getDocuments().get(0).getId();
                            getCurrentUser(true);
                        }
                    }
                });
        while (otherFirestore == null) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return otherFirestore;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public String getFirestore() {
        return firestore;
    }

    public void updateLocationFieldInUsersTokens(final String field, final Object value) {
        getFirestoreDocumentID();


        FirebaseFirestore.getInstance()
                .collection(USERS_TOKENS_DB)
                .whereEqualTo(UID_DB, uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                            firestore = task.getResult().getDocuments().get(0).getId();
                            sp.edit().putString(FIRESTORE_UID_SP, firestore).commit();
                            FirebaseFirestore.getInstance()
                                    .collection(USERS_TOKENS_DB)
                                    .document(firestore)
                                    .update(field, value);
                        }
                    }
                });
    }

    public void updateInfoFieldInUsersTokens(UserInfo userInfo) {
        FirebaseFirestore.getInstance()
                .collection(USERS_TOKENS_DB)
                .document(firestore)
                .update(INFO_DB, userInfo);
    }

    public void updateFieldInUsersTokens(final Map<String, Object> map) {
        getFirestoreDocumentID();

        uid = (String) map.get(UID_DB);
        FirebaseFirestore.getInstance()
                .collection(USERS_TOKENS_DB)
                .whereEqualTo(UID_DB, uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                            firestore = task.getResult().getDocuments().get(0).getId();
                            sp.edit().putString(FIRESTORE_UID_SP, firestore).commit();
                            FirebaseFirestore.getInstance()
                                    .collection(USERS_TOKENS_DB)
                                    .document(firestore)
                                    .update(map);
                        } else {
                            FirebaseFirestore.getInstance()
                                    .collection(USERS_TOKENS_DB)
                                    .add(map)
                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            firestore = task.getResult().getId();
                                            sp.edit().putString(FIRESTORE_UID_SP, firestore).commit();
                                        }
                                    });
                        }
                    }
                });
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public List<User> getNearbyUsers() {
        return nearbyUsers;
    }

    public User getUserByUID(String uid) {
        otherUser = null;
        FirebaseFirestore.getInstance()
                .collection(USERS_TOKENS_DB)
                .whereEqualTo(UID_DB, uid)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult() != null) {
                    otherUser = task.getResult().getDocuments().get(0).toObject(User.class);
                    otherUser.setDocID(task.getResult().getDocuments().get(0).getId());
                }
            }
        });
        while (otherUser == null) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return otherUser;
    }

    public List<User> refreshNearbyUsers() {
        //TODO
        shouldContinue = false;
        getNearbyUsers(51, true);
        while (!shouldContinue) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return nearbyUsers;
    }

    public Room getChatList(String roomID) {
        Room room = null;
        if (rooms.contains(roomID)) {
            room = rooms.get(rooms.indexOf(roomID));
        }
        return room;
    }

    public void addUser(User user) {
        currentUser = user;
        FirebaseFirestore.getInstance()
                .collection(USERS_TOKENS_DB)
                .add(user);

    }

    public String checkIfRoomExists(String otherUserUID) {
        roomUID = null;
        FirebaseFirestore.getInstance()
                .collection(ROOMS_DB)
                .whereArrayContains("tofrom", otherUserUID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        roomUID = task.getResult().getDocuments().get(0).getId();
                    }
                });
        while (roomUID == null) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return roomUID;
    }

    public void addMessagetoARoom(String roomUID, Message... messages) {
        room = null;
        if (rooms != null) {
            for (Room r : rooms) {
                if (r.getRoomUID().equals(roomUID)) {
                    room = r;
                }
            }
        }

        for (Message msg : messages) {
            room.addMessage(msg);
        }
        List<Message> roomMsgs = room.getMessages();
        FirebaseFirestore.getInstance()
                .collection(ROOMS_DB)
                .document(roomUID)
                .update(MESSAGES_DB, roomMsgs);
    }

    public String addNewRoomAndMessage(final String firestoreUID, String otherUserUID, Message msg) {
        room = new Room();
        room.setTofrom(Arrays.asList(otherUserUID, uid));
        room.addMessage(msg);

        Task<DocumentReference> task = FirebaseFirestore.getInstance()
                .collection(ROOMS_DB)
                .add(room).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                room.setRoomUID(task.getResult().getId());
                FirebaseFirestore.getInstance()
                        .collection(USERS_TOKENS_DB)
                        .document(firestoreUID)
                        .update(MESSAGES_DB, Arrays.asList(room.getRoomUID()));
                FirebaseFirestore.getInstance()
                        .collection(USERS_TOKENS_DB)
                        .document(firestore)
                        .update(MESSAGES_DB, Arrays.asList(room.getRoomUID()));
            }
        });

        while (!task.isComplete()){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        currentUser.addRoom(room.getRoomUID());
        return room.getRoomUID();
    }
    
    public void addFavourites(User favUser){
        List<String> favs = currentUser.getFavs();
        if (favs == null){
            favs = new ArrayList<>();
        }
        favs.add(favUser.getDocID());
        FirebaseFirestore.getInstance()
                .collection(USERS_TOKENS_DB)
                .document(firestore)
                .update(FAVS_DB, favs);
    }
    
    public List<User> getUsersByFIRE(List<String> firestoreIDs){
        getUsers(firestoreIDs);
        while (!isComplete){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return favUsersList;
    }
    
    private void getUsers(final List<String> firestoreIDs){
        isComplete = false;
        favUsersList = new ArrayList<>();
        for (int i = 0; i < firestoreIDs.size() ; i++){
            index = i;
            FirebaseFirestore.getInstance()
                    .collection(USERS_TOKENS_DB)
                    .document(firestoreIDs.get(i))
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    favUsersList.add(task.getResult().toObject(User.class));
                    if (index == firestoreIDs.size()-1){
                        isComplete = true;
                    }
                }
            });
        }
    }

    private void getNearbyUsers(int limit, final Boolean refresh) {
        Task<QuerySnapshot> task = FirebaseFirestore.getInstance()
                .collection(USERS_TOKENS_DB)
                .limit(limit)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                User usr;
                nearbyUsers = new ArrayList<>();
                for (DocumentSnapshot o : task.getResult().getDocuments()) {
                    usr = o.toObject(User.class);
                    usr.setDocID(o.getId());
                    nearbyUsers.add(usr);
                }
                if (refresh) {
                    shouldContinue = true;
                }
                for (User user : nearbyUsers) {
                    user.setDistance(
                            String.valueOf(distance(currentUser.getLatlng().getLatitude(),
                                    currentUser.getLatlng().getLongitude(),
                                    user.getLatlng().getLatitude(),
                                    user.getLatlng().getLongitude(),
                                    KILOMETERS
                            ))
                    );
                }
                Collections.sort(nearbyUsers);
                nearbyUsers.remove(currentUser);
                gotNearbyUsers = true;
            }
        });
        while (!task.isComplete()){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void getCurrentUser(Boolean isCurrentUser) {

        Task<DocumentSnapshot> firebaseTask = FirebaseFirestore.getInstance()
                .collection(USERS_TOKENS_DB)
                .document(firestore)
                .get();

        if (isCurrentUser) {
            firebaseTask.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        currentUser = task.getResult().toObject(User.class);
                        currentUser.setDocID(task.getResult().getId());
                        gotCurrentUser = true;
                        getNearbyUsers(50, false);
                        getAllRoomsAndMessages(currentUser);

                    } else {
//TODO do something when not successful
                    }
                }
            });
        } else {
            firebaseTask.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        otherUser = task.getResult().toObject(User.class);
                        getAllRoomsAndMessages(currentUser);
                        getNearbyUsers(50, false);
                    } else {
//TODO do something when not successful
                    }
                }
            });
        }

    }

    private void updateFieldInRooms(String field, Object value, String collection, String roomUID) {
        FirebaseFirestore.getInstance()
                .collection(collection)
                .document(roomUID)
                .update(field, value);
    }

    private Task<QuerySnapshot> getAllRoomsAndMessages(User user) {
        gotAllRooms = false;
        gotRoom = false;
        rooms = new ArrayList<>();
        final List<String> messagesUIDs = user.getMessages();
        if (messagesUIDs.size() > 0) {
            for (int i = 0; i < messagesUIDs.size(); i++) {
                index = i;
                if (messagesUIDs.get(i) != null) {
                    tsk = FirebaseFirestore.getInstance()
                            .collection(ROOMS_DB)
                            .document(messagesUIDs.get(i))
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                room = task.getResult().toObject(Room.class);
                                String roomID = task.getResult().getId();
                                room.setRoomUID(roomID);
                                rooms.add(room);
                                if (index == messagesUIDs.size()-1){
                                    gotAllRooms = true;
                                }
                            } else {
                                //TODO
                            }
                        }
                    });
                }
            }
        }
        return (Task<QuerySnapshot>)tsk;

    }

    public class MyLocalBinder extends Binder {
        public MyDBService getService() {
            return MyDBService.this;
        }
    }
}
