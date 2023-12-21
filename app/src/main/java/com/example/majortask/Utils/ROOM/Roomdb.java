package com.example.majortask.Utils.ROOM;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.majortask.Utils.FirebaseHelper;
import com.example.majortask.Utils.Person;

import java.util.ArrayList;
import java.util.List;

@Database(entities = {Person.class}, version = 2, exportSchema = false)
public abstract class Roomdb extends RoomDatabase {

    private static Roomdb INSTANCE;

    public abstract PersonDao personDao();

    private static Roomdb getDbInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), Roomdb.class, "USERS_DB")
                    .allowMainThreadQueries().build();
        }
        return INSTANCE;
    }

    public static void getUserById(Context context, String userId, final getUserCallbacks callbacks) {
        Roomdb db = getDbInstance(context);
        Person userData = db.personDao().getPersonById(userId);
        if (userData == null) {
            // Data not found in Room, fetch from cloud asynchronously
            syncWithCloud(context, new syncCallbacks() {
                @Override
                public void onSyncComplete() {
                    Person userData = db.personDao().getPersonById(userId);
                    if (userData == null) {
                        callbacks.onUserNotFound("User not found in ether local " +
                                "or cloud database. Check the id you are searching with!");
                    } else {
                        Log.v("room101", "Retrieved user data from ROOM after syncing. UserId:" + userData.getUserId());
                        callbacks.onUserFound(userData);
                    }
                }

                @Override
                public void onNetworkError(String errorMessage) {
                    callbacks.onUserNotFound("User not found in local database " +
                            "and an  error in the connection occurred during syncing local database" +
                            " with cloud check connectivity!\nNetwork Error:" + errorMessage);
                }
            });
        }
        Log.v("room101", "Retrieved user data from ROOM successfully. UserId:" + userData.getUserId());
        callbacks.onUserFound(userData);
    }

    public static void syncWithCloud(Context context, final syncCallbacks callbacks) {
        // Prepare the local image
        Roomdb db = getDbInstance(context);
        List<Person> localSnapshot = db.personDao().getAllUsers(); // Simplified variable name

        FirebaseHelper firebaseHelper = new FirebaseHelper();
        firebaseHelper.retrieveAllRegisterdUsers(new FirebaseHelper.retreiveALlRegisteredUsersCallback() {
            @Override
            public void onRecieveUsers(List<Person> userList) {
                try {
                    // Remove users already present locally
                    userList.removeAll(localSnapshot);

                    for (Person person : userList) {
                        db.personDao().insertPerson(person);
                    }
                    callbacks.onSyncComplete();
                } catch (Exception e) {
                    Log.e("sync101", "Error syncing data: " + e.getMessage());
                    callbacks.onNetworkError("Error syncing data: " + e.getMessage());
                }
            }

            @Override
            public void networkConnectionError(String errorMessage) {
                Log.v("sync101", "Failed to sync due to network error: " + errorMessage);
                callbacks.onNetworkError(errorMessage);
            }
        });
    }

    public interface syncCallbacks {
        void onSyncComplete();

        void onNetworkError(String errorMessage);
    }

    public interface getUserCallbacks {
        void onUserFound(Person person);

        void onUserNotFound(String errorMessage);
    }
}
