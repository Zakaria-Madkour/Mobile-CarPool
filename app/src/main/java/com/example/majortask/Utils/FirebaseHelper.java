package com.example.majortask.Utils;

import static android.service.controls.ControlsProviderService.TAG;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.type.DateTime;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    public void addARide(Ride ride, final addRideCallback callback) {
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
                .addOnSuccessListener(aVoid -> {
                    callback.rideAddedSuccessfully(newRecord.getKey());
                })
                .addOnFailureListener(e -> {
                    callback.networkConnectionError(e.getMessage());
                });
    }


    public void driverRequestsQuery(String userId, final driverRequestsQueryCallback callback) {
        List<Ride> rideList = new ArrayList<>();
        List<Request> allRequests = new ArrayList<>();

        DatabaseReference requestRef = databaseReference.child("requests");
        requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String snapshotRiderId = snapshot.child("riderId").getValue(String.class);
                    String snapshotRideId = snapshot.child("rideId").getValue(String.class);
                    String snapshotStatus = snapshot.child("status").getValue(String.class);
                    String snapshotrequestId = snapshot.getKey();
                    Request snapshotRequest = new Request(snapshotStatus, snapshotRideId, snapshotRiderId, snapshotrequestId);
                    allRequests.add(snapshotRequest);
                    Log.v("clouddb101", "Retrieved request:" + snapshotrequestId);
                }
                //Now we have fetched all requests
                for (Request request : allRequests) {
                    //For each request we fetch its ride and check the driverId
                    DatabaseReference rideReq = FirebaseDatabase.getInstance().getReference().child("rides");
                    Query rideQuery = rideReq.child(request.getRideId());
                    rideQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String pickup = snapshot.child("pickup").getValue(String.class);
                            String dropoff = snapshot.child("dropoff").getValue(String.class);
                            String date = snapshot.child("day").getValue(String.class);
                            String time = snapshot.child("time").getValue(String.class);
                            String cost = snapshot.child("cost").getValue(String.class);
                            String driverId = snapshot.child("driverId").getValue(String.class);
                            String rideId = snapshot.getKey();
                            Boolean status = true;
                            Ride ride = new Ride(pickup, dropoff, time, cost, status, driverId, rideId, date);
                            rideList.add(ride);

                            if (rideList.size() == allRequests.size()) {
                                // we are ready to filter the results and return the fitered data
                                List<Ride> filteredRideList = new ArrayList<>();
                                List<Request> filteredRequests = new ArrayList<>();
                                for (int i = 0; i < rideList.size(); i++) {
                                    if (rideList.get(i).getDriverId().equals(userId)) {
//                                            && allRequests.get(i).getStatus().equals("Awaiting Driver Acceptance")){
//                                            could be added later after developing a dedicated view for other states
                                        filteredRideList.add(rideList.get(i));
                                        filteredRequests.add(allRequests.get(i));
                                    }
                                }
                                callback.onGetRequests(filteredRequests, filteredRideList);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            callback.networkConnectionError(error.getMessage());
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.networkConnectionError(databaseError.getMessage());
            }
        });
    }


    public void rideCartQuery(String userId, final riderCartQueryCallback callback) {
        List<Request> requestList = new ArrayList<>();
        List<Ride> rideList = new ArrayList<>();

        //Step 1 fetch all requests that have riderId = userId
        DatabaseReference ridesRef = databaseReference.child("requests");
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
                    Request snapshotRequest = new Request(snapshotStatus, snapshotRideId, userId, snapshotrequestId);
                    if (true) {
//                    if(!snapshotStatus.equals("Paid")){
//                    retrive all the cart items for now (Use this place to view both cart and history)
//                    TODO: dedicated history view and filter here after doing it.
                        requestList.add(snapshotRequest);
                    }
                    Log.v("clouddb101", "Retrieved cart ride:" + snapshotrequestId);
                }
                // Step 2 check the length of Requests
                if (requestList.isEmpty()) {
                    callback.onEmptyCart();
                    Log.v("clouddb101", "There is no registered rides in cart");
                }
                // Step 3 retrieve all the rides corresponding to the requests
                FirebaseHelper helperSubsidary = new FirebaseHelper();
                for (Request request : requestList) {
                    helperSubsidary.retrieveRideById(request.getRideId(), new retrieveRideCallback() {
                        @Override
                        public void retrieveRideData(Ride ride) {
                            Map<Request, Ride> map1 = new HashMap<>();
                            rideList.add(ride);

                            if (requestList.size() == rideList.size()) {
                                // All the requests finished could call your callback
                                // Finished retrieving Rides return them
                                callback.onGetCartItems(requestList, rideList);
                                Log.v("clouddb101", rideList.toString());

                            }
                        }

                        @Override
                        public void keyDoesntExist() {
                            //Skip
                            // Database corruption need to handle rules
                            Log.v("clouddb101", "Ride not found yet key is used in request. Database corruption need to handle rules!");
                        }

                        @Override
                        public void networkConnectionError(String errorMessage) {
                            Log.v("clouddb101", "Connection Error" + errorMessage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.networkConnectionError(databaseError.getMessage());
            }
        });
    }

    public void retrieveRideById(String rideId, final retrieveRideCallback callback) {
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
                    callback.retrieveRideData(new Ride(pickup, dropoff, time, cost, true, driverId, rideId, day));
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

    public void retrievePersonById(String userId, final retrievePersonCallback callback) {
        db.collection("USERS")
                .document("DRIVER")
                .collection("root")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Document exists, retrieve its data
                        String firstName = documentSnapshot.getString("FirstName");
                        String lastName = documentSnapshot.getString("LastName");
                        String email = documentSnapshot.getString("Email");
                        String phone = documentSnapshot.getString("Phone");
                        callback.retrievedPersonData(new Person(firstName, lastName, email, phone, "DRIVER"));
                    } else {
                        db.collection("USERS")
                                .document("RIDER")
                                .collection("root")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(documentSnapshot2 -> {
                                    if (documentSnapshot2.exists()) {
                                        // Document exists, retrieve its data
                                        String firstName = documentSnapshot2.getString("FirstName");
                                        String lastName = documentSnapshot2.getString("LastName");
                                        String email = documentSnapshot2.getString("Email");
                                        String phone = documentSnapshot2.getString("Phone");
                                        callback.retrievedPersonData(new Person(firstName, lastName, email, phone, "DRIVER"));

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
                if (!requestAlreadyExists) {
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


    public void retrieveDriverRides(String userId, final retrieveDriverRidesCallback callback) {
        List<Ride> ridesList = new ArrayList<>();
        //Step 1 fetch all rides that have driverId = userId
        DatabaseReference ridesRef = FirebaseDatabase.getInstance().getReference().child("rides");
        // First search if the request already exists
        Query query = ridesRef.orderByChild("driverId").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Get the user with the specified rideId
                    String pickUp = snapshot.child("pickup").getValue(String.class);
                    String dropOff = snapshot.child("dropoff").getValue(String.class);
                    String day = snapshot.child("day").getValue(String.class);
                    String time = snapshot.child("time").getValue(String.class);
                    String cost = snapshot.child("cost").getValue(String.class);
                    String rideId = snapshot.getKey();

                    //Set the status of the ride to true in all cases to list all rides
                    Boolean status = true;
                    Ride ride = new Ride(pickUp, dropOff, time, cost, status, userId, rideId, day);
                    if (status) {
                        // Add on all rides
                        ridesList.add(ride);
                        Log.v("clouddb101", "Retrieved cart ride:" + rideId);
                    }
                }
                if (ridesList.size() == 0) {
                    callback.onNoRides();
                }
                callback.onGetRides(ridesList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("database101", "Connection Error");
                callback.networkConnectionError(databaseError.getMessage());
            }
        });
    }

    public void changeRequestState(String requestId, String newState, final changeRequestStateCallback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference keyRef = databaseReference.child("requests").child(requestId).child("status");
        keyRef.setValue(newState)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // The value has been updated successfully
                        callback.onSuccessfulChange();
                        Log.d("Firebase", "Value updated successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors that occurred during the update
                        callback.onFailedChange(e.getMessage());
                        Log.e("Firebase", "Failed to update value: " + e.getMessage());
                    }
                });
    }

    public void fetchAvailableRides(Boolean includePast, final fetchAvailableRidesCallback callback) {
        List<Ride> ridesList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("rides");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    String pickup = itemSnapshot.child("pickup").getValue().toString();
                    String dropoff = itemSnapshot.child("dropoff").getValue().toString();
                    String time = itemSnapshot.child("time").getValue().toString();
                    String cost = itemSnapshot.child("cost").getValue().toString();
                    String date = itemSnapshot.child("day").getValue().toString();
                    String driverId = itemSnapshot.child("driverId").getValue().toString();
                    String rideId = itemSnapshot.getKey().toString();
                    Boolean state = !DateNTime.isPast(date, time); // state=false -> (ride in past)

                    // Condition check on the timing constraints
                    Double timeRemainingForRide = DateNTime.timeToRide(date, time);
                    // Filter based on the remaining time for the ride [USE CASE -> Document Rquirment]
                    if (time.equals("7:30") && timeRemainingForRide < 9.5) {
                        continue;
                    }
                    if (time.equals("5:30") && timeRemainingForRide < 4.5) {
                        continue;
                    }

                    // Add ride to the returned list based on the request
                    if (state || includePast) {
                        // Ride yet to come (Future)
                        Ride myRide = new Ride(pickup, dropoff, time, cost, state, driverId, rideId, date);
                        ridesList.add(myRide);
                    }
                }
                if (ridesList.size() == 0) {
                    callback.onNoRides();
                    return;
                }
                // Finished fetching rides then return them
                callback.onGetRides(ridesList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.networkConnectionError(error.getMessage());
                Log.v("Firebase", "Failed to fetch rides" + error.getMessage());

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

    public interface retrievePersonCallback {
        void retrievedPersonData(Person person);

        void networkConnectionError(String errorMessage);

    }

    public interface addRideCallback {
        void rideAddedSuccessfully(String rideId);

        void networkConnectionError(String errorMessage);
    }

    public interface retrieveRideCallback {
        void retrieveRideData(Ride ride);

        void keyDoesntExist();

        void networkConnectionError(String errorMessage);

    }

    public interface riderCartQueryCallback {
        void onGetCartItems(List<Request> requestsList, List<Ride> rideList);

        void onEmptyCart();

        void networkConnectionError(String errorMessage);
    }

    public interface driverRequestsQueryCallback {
        void onGetRequests(List<Request> requestsList, List<Ride> rideList);

        void onNoRequests();

        void networkConnectionError(String errorMessage);
    }

    public interface retrieveDriverRidesCallback {
        void onGetRides(List<Ride> rideList);

        void onNoRides();

        void networkConnectionError(String errorMessage);
    }

    public interface changeRequestStateCallback {
        void onSuccessfulChange();

        void onFailedChange(String errorMessage);
    }

    public interface fetchAvailableRidesCallback {
        void onGetRides(List<Ride> rideList);

        void onNoRides();

        void networkConnectionError(String errorMessage);
    }
}
