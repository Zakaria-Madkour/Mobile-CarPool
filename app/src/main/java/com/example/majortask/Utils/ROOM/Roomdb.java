package com.example.majortask.Utils.ROOM;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.majortask.Utils.FirebaseHelper;
import com.example.majortask.Utils.Person;

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


    /**
     * Retrieves user data either from the local Room database or the cloud based on the provided user ID.
     * If the user data is not found in the local Room database, it attempts to sync data with the cloud asynchronously.
     *
     * @param context   The context used to access the local Room database and handle SharedPreferences.
     * @param userId    The unique identifier of the user to fetch.
     * @param callbacks Callbacks to handle user data retrieval outcomes:
     *
     * <p> The method initially checks the local Room database for the user data using the provided user ID.
     * - If the data is found locally:
     *    - The method logs the successful retrieval and triggers the  onUserFound callback.
     * - If the data is not found locally:
     *    - The method initiates an asynchronous sync with the cloud using the provided context.
     *    - Upon completion of the cloud sync, it attempts to retrieve the user data again from the local Room database.
     *    - If the data is found after the sync:
     *       - The method logs the successful retrieval after syncing and triggers the onUserFound callback.
     *    - If the data is still not found after syncing:
     *       - The method triggers the  onUserNotFound callback, indicating the absence of user data
     *         in both the local and cloud databases. It advises verifying the provided user ID.
     * - If there's a network error during cloud syncing:
     *    - The method triggers the  onUserNotFound callback, informing the failure to retrieve
     *      user data due to a network issue during the cloud sync. It prompts the user to check network connectivity.</p>
     *
     *<p> This method provides callbacks to handle various scenarios of user data retrieval,
     * allowing developers to respond accordingly based on the success or failure of data retrieval.</p>
     *
     *<p> you need to implement getUserCallbacks when invoking this method</p>
     */
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
        else {
            Log.v("room101", "Retrieved user data from ROOM successfully. UserId:" + userData.getUserId());
            callbacks.onUserFound(userData);
        }
    }


    /**
     * Synchronizes local Room database data with the cloud by fetching and updating user information.
     *
     * @param context   The context used to access the local Room database and handle Firebase operations.
     * @param callbacks Callbacks to handle synchronization outcomes:
     *
     *<p> The method initiates a sync process between the local Room database and the cloud database
     * by retrieving all registered users' data from the cloud.
     * - It retrieves a local snapshot of the users' data from the Room database.
     * - Subsequently, it uses FirebaseHelper to fetch all registered users' data from the cloud asynchronously.
     * - Upon receiving the cloud data, it compares and updates the local database with the new data.
     *   - It removes users already present in the local snapshot to prevent duplicates.
     *   - For each new user in the cloud data, it inserts them into the local Room database.
     * - After updating the local database, the method triggers the onSyncComplete callback to indicate successful synchronization.</p>
     *
     *<p> If any errors occur during the synchronization process:
     * - If there's an error while inserting data into the local database,
     * - If a network error occurs during the cloud data retrieval process,
     * it logs the error and triggers the  onNetworkError callback
     * to inform about the network-related issue during sync, providing an error message.</p>
     *
     *<p> you need to implement the syncCallbacks when invoking this method</p>
     */
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
