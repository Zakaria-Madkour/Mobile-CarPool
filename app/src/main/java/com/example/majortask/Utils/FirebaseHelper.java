package com.example.majortask.Utils;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private DatabaseReference databaseReference;

    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void addARide(Ride ride, final addRideCallback callback){
        Map data = new HashMap<>();
        data.put("pickup", ride.getPickup());
        data.put("dropoff", ride.getDestination());
        data.put("cost", ride.getCost());
        data.put("time", ride.getTime());
        data.put("day", ride.getDay());
        data.put("driverId", ride.getDriverId());
        DatabaseReference ridesRef = databaseReference.child("rides");
        DatabaseReference newRecord = ridesRef.push();
        newRecord.setValue(data)
                .addOnSuccessListener( aVoid ->{
                    callback.rideAddedSuccessfully(newRecord.getKey());
                })
                .addOnFailureListener(e -> {
                    callback.networkConnectionError(e.getMessage());
                });
    }

    public void rideCartQuery(String userId, final riderCartQueryCallback callback){
        List<Request> ridesRequestsList = new ArrayList<>();
        List<Map<Request,Ride>> rideRequestsMap = new ArrayList<>();

        //Step 1 fetch all requests that have riderId = userId
        DatabaseReference ridesRef = databaseReference.child("rides");
        // First search if the request already exists
        Query query = ridesRef.orderByChild("riderId").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Get the user with the specified rideId
                    String snapshotRideId = snapshot.child("rideId").getValue(String.class);
                    String snapshotStatus = snapshot.child("status").getValue(String.class);
                    String snapshotrequestId = snapshot.getKey();
                    Request snapshotRequest = new Request(snapshotStatus,snapshotRideId,userId,snapshotrequestId);
                    ridesRequestsList.add(snapshotRequest);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.networkConnectionError(databaseError.getMessage());
            }
        });
        // Step 2 check the length of Requests
        if (ridesRequestsList.isEmpty()){
            callback.onEmptyCart();
        }
        // Step 3 retrieve all the rides corresponding to the requests
        FirebaseHelper helperSubsidary = new FirebaseHelper();
        for (Request request:ridesRequestsList){
            helperSubsidary.retrieveRideById(request.getRideId(), new retrieveRideCallback() {
                @Override
                public void retrieveRideData(Ride ride) {
                    Map<Request, Ride> map1 = new HashMap<>();
                    map1.put(request, ride);
                    rideRequestsMap.add(map1);
                }

                @Override
                public void keyDoesntExist() {
                    //Skip
                    // Database corruption need to handle rules
                    Log.v("clouddb101","Ride not found yet key is used in request. Database corruption need to handle rules!");
                }
                @Override
                public void networkConnectionError(String errorMessage) {
                    Log.v("clouddb101","Connection Error"+errorMessage);
                }
            });
        }
        // Finished retrieving Rides return them
        callback.onGetCartItems(rideRequestsMap);
    }

    public void retrieveRideById(String rideId, final retrieveRideCallback callback){
        DatabaseReference ridesRef = databaseReference.child("rides");
        ridesRef.child(rideId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String pickup = dataSnapshot.child("pickup").getValue(String.class);
                    String dropoff = dataSnapshot.child("dropoff").getValue(String.class);
                    String day = dataSnapshot.child("day").getValue(String.class);
                    String time = dataSnapshot.child("time").getValue(String.class);
                    String cost = dataSnapshot.child("cost").getValue(String.class);
                    String driverId = dataSnapshot.child("driverId").getValue(String.class);
                    callback.retrieveRideData(new Ride(pickup,dropoff,time,cost,true,driverId, rideId,day));
                } else {
                    // Handle case where the key doesn't exist
                    callback.keyDoesntExist();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle potential errors
                callback.networkConnectionError(databaseError.getMessage());
            }
        });
    }

    public void retrievePersonById(String userId, final retrievePersonCallback callback){
        db.collection("USERS")
                .document("DRIVER")
                .collection("root")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Document exists, retrieve its data
                        String firstName = documentSnapshot.getString("FirstName");
                        String lastName = documentSnapshot.getString("LastName");
                        String email = documentSnapshot.getString("Email");
                        String phone = documentSnapshot.getString("Phone");
                        callback.retrievedPersonData(new Person(firstName,lastName,email,phone,"DRIVER"));
                    }
                    else {
                        db.collection("USERS")
                                .document("RIDER")
                                .collection("root")
                                .document(mAuth.getCurrentUser().getUid())
                                .get()
                                .addOnSuccessListener(documentSnapshot2 -> {
                                    if (documentSnapshot2.exists()) {
                                        // Document exists, retrieve its data
                                        String firstName = documentSnapshot2.getString("FirstName");
                                        String lastName = documentSnapshot2.getString("LastName");
                                        String email = documentSnapshot2.getString("Email");
                                        String phone = documentSnapshot2.getString("Phone");
                                        callback.retrievedPersonData(new Person(firstName,lastName,email,phone,"DRIVER"));

                                    }
                                })
                                .addOnFailureListener(ee -> {
                                    // Handle any errors that may occur while fetching data
                                    callback.networkConnectionError(ee.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                   callback.networkConnectionError(e.getMessage());
                });

    }

    public void bookARide(Ride r, final bookARideCallback callback) {
        Log.v("clouddb101", "Booking a ride for the user" + r.getPickup());
        String rideId = r.getRideId();
        String riderId = mAuth.getCurrentUser().getUid();

        Map data = new HashMap<>();
        data.put("status", "Awaiting Driver Acceptance");
        data.put("rideId", rideId);
        data.put("riderId", riderId);
        DatabaseReference requestsRef = databaseReference.child("requests");
        // First search if the request already exists
        Query query = requestsRef.orderByChild("rideId").equalTo(r.getRideId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean requestAlreadyExists = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Get the user with the specified rideId
                    String snapshotRiderId = snapshot.child("riderId").getValue(String.class);
                    if (riderId.equals(snapshotRiderId)) {
                        Log.v("database101", "Request already exists");
                        callback.requestAlreadyExists();
                        requestAlreadyExists = true;
                        return;
                    }
                }
                if (!requestAlreadyExists){
                    // Request doesn't exist add it
                    DatabaseReference newRecord = requestsRef.push();
                    newRecord.setValue(data)
                            .addOnSuccessListener(aVoid -> {
                                callback.bookedSuccessfully(newRecord.getKey());
                                Log.v("database101", "Added Ride Successfully");
//                    Toast.makeText(requireContext(), "Ride added successfully!",
//                            Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                callback.networkConnectionError(e.getMessage());
                                Log.v("database101", "Failed to add ride");
//                    Toast.makeText(requireContext(), "Failed to add ride please check your internet connectivity.",
//                            Toast.LENGTH_SHORT).show();
                            });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("database101", "Connection Error");
                callback.networkConnectionError(databaseError.getMessage());
            }
        });
    }

    public void checkIfRiderOrDriver(String userId, final RiderOrDriverCallback callback) {
        db.collection("USERS")
                .document("DRIVER")
                .collection("root")
                .document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                callback.isDriver(true);
                            } else {
                                callback.isDriver(false);
                            }
                        } else {
                            callback.isRiderOrDriverFetchError(task.getException().getMessage());
                        }
                    }
                });
        db.collection("USERS")
                .document("RIDER")
                .collection("root")
                .document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                callback.isRider(true);
                            } else {
                                callback.isRider(false);
                            }
                        } else {
                            callback.isRiderOrDriverFetchError(task.getException().getMessage());
                        }
                    }
                });

    }

    public interface RiderOrDriverCallback {
        void isRider(boolean rider);

        void isDriver(boolean driver);

        void isRiderOrDriverFetchError(String errorMessage);
    }

    public interface bookARideCallback {
        void bookedSuccessfully(String requestId);

        void requestAlreadyExists();

        void networkConnectionError(String errorMessage);
    }
    public interface retrievePersonCallback{
        void retrievedPersonData(Person person);
        void networkConnectionError(String errorMessage);

    }
    public interface addRideCallback{
        void rideAddedSuccessfully(String rideId);
        void networkConnectionError(String errorMessage);
    }
    public interface retrieveRideCallback{
        void retrieveRideData(Ride ride);
        void keyDoesntExist();
        void networkConnectionError(String errorMessage);

    }

    public interface riderCartQueryCallback{
        void onGetCartItems(List<Map<Request,Ride>> requestRideMap);
        void onEmptyCart();
        void networkConnectionError(String errorMessage);

    }
}
