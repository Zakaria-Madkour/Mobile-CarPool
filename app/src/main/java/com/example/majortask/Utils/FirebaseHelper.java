package com.example.majortask.Utils;


import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {
    private FirebaseFirestore db;
    private DatabaseReference databaseReference;


    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }


    /**
     * Adds a new ride to the database based on the provided ride details.
     *
     * @param ride     The 'Ride' object containing information about the ride to be added.
     *                 Contains details such as pickup location, drop-off destination, cost, time, day, and driver ID.
     * @param callback The callback interface to handle the result of the ride addition.
     *                 Must implement 'addRideCallback'.
     *
     * <p>This method adds a new ride entry to the 'rides' node in the database using the provided 'Ride' object.
     * The relevant details of the ride, including pickup location, drop-off destination, cost, time, day, and driver ID,
     * are extracted from the 'ride' object and stored in a 'data' map.
     *
     * <p>The ride details in the 'data' map are then pushed to the 'rides' reference in the database.
     * Upon successful addition of the ride entry, the 'rideAddedSuccessfully' method of the 'addRideCallback'
     * is invoked, passing the unique key of the newly added ride.
     *
     * <p>If there is a failure during the ride addition process (e.g., network issues),
     * the 'networkConnectionError' method of the 'addRideCallback' is invoked, passing an error message.
     *
     * <p>The 'addRideCallback' interface needs to be implemented to handle the success or failure
     * of adding the ride and obtain the newly added ride's key.
     */
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


    /**
     * Retrieves requests and associated ride details for a specific driver from the database.
     *
     * @param userId   The unique identifier of the driver for whom the requests are being retrieved.
     * @param callback The callback interface to handle the received requests and related ride details.
     *                 Must implement 'driverRequestsQueryCallback'.
     *
     * <p>This method retrieves all requests from the 'requests' node in the database and stores them
     * in the 'allRequests' list. Each request contains details such as rider ID, ride ID, status, and request ID.
     *
     * <p>For each request fetched, the method queries the associated ride details based on the ride ID
     * from the 'rides' node in the database. Once the ride details are retrieved, they're added to the 'rideList'.
     *
     * <p>Once all ride details are fetched and added to the 'rideList', the method filters these rides
     * based on whether their driver ID matches the provided 'userId'. Filtered rides and associated requests
     * are added to 'filteredRideList' and 'filteredRequests', respectively.
     *
     * <p>The 'onGetRequests' method of the 'driverRequestsQueryCallback' is then invoked, passing the
     * filtered ride and request lists. This method should handle the received filtered data.
     *
     * <p>The 'driverRequestsQueryCallback' interface needs to be implemented to manage the received
     * filtered ride and request details from this method.
     */
    public void driverRequestsQuery(String userId, final driverRequestsQueryCallback callback) {
        List<Ride> rideList = new ArrayList<>();
        List<Request> allRequests = new ArrayList<>();

        DatabaseReference requestRef = databaseReference.child("requests");
        requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.networkConnectionError(databaseError.getMessage());
            }
        });
    }


    /**
     * Retrieves cart information related to a specific user identified by their ID from the database.
     *
     * @param userId   The unique identifier of the user for whom the cart information is being retrieved.
     * @param callback The callback interface to handle the received cart items and related ride details.
     *                 Must implement 'riderCartQueryCallback'.
     *
     * <p>This method fetches all requests associated with a specified user ID (rider) from the 'requests'
     * node in the database. Each request contains details such as ride ID, status, and request ID.
     *
     * <p>The method populates a 'requestList' with retrieved requests having the provided user ID.
     * If a request exists and meets certain conditions (as indicated in the 'if' condition), it's added
     * to the 'requestList'.
     *
     * <p>If no requests are found for the user, the 'onEmptyCart' method of the 'riderCartQueryCallback'
     * is invoked to handle the case.
     *
     * <p>After assembling the 'requestList', the method iterates through each request and retrieves
     * corresponding ride details using the 'retrieveRideById' method of the 'FirebaseHelper' class.
     * For each retrieved ride, the 'retrieveRideData' method of the 'retrieveRideCallback' interface is
     * called, adding the ride to the 'rideList'.
     *
     * <p>Once all ride details corresponding to the requests are retrieved, the 'onGetCartItems' method
     * of the 'riderCartQueryCallback' is invoked, passing both the 'requestList' and 'rideList' as parameters.
     *
     * <p>Special handling is provided through the 'keyDoesntExist' and 'networkConnectionError' methods
     * of the 'retrieveRideCallback' for scenarios where a ride is not found or a network error occurs
     * during ride retrieval.
     *
     * <p>Implement the 'riderCartQueryCallback' interface to handle cart information and related ride details
     * received from this method.
     */
    public void rideCartQuery(String userId, final riderCartQueryCallback callback) {
        List<Request> requestList = new ArrayList<>();
        List<Ride> rideList = new ArrayList<>();

        //Step 1 fetch all requests that have riderId = userId
        DatabaseReference ridesRef = databaseReference.child("requests");
        // First search if the request already exists
        Query query = ridesRef.orderByChild("riderId").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.networkConnectionError(databaseError.getMessage());
            }
        });
    }


    /**
     * Retrieves information about a specific ride from the Firebase Realtime Database based on the provided ride ID.
     *
     * @param rideId   The unique identifier of the ride whose information needs to be retrieved.
     * @param callback The callback interface to handle the reception of ride data.
     *                 Must implement 'retrieveRideCallback'.
     *
     * <p>This method retrieves information about a specific ride from the Firebase Realtime Database
     * based on the provided ride ID. It accesses the 'rides' node in the database and searches for
     * the ride ID to retrieve its details.
     *
     * <p>If the ride with the specified ride ID exists in the database, the method extracts data
     * such as pickup location, drop-off location, day, time, cost, and driver ID associated with
     * the ride.
     *
     * <p>The retrieved data is encapsulated in a 'Ride' object and passed to the 'retrieveRideData'
     * method of the 'retrieveRideCallback' for further handling.
     *
     * <p>If the specified ride ID doesn't exist in the database, the 'keyDoesntExist' method of
     * the 'retrieveRideCallback' is invoked to handle this scenario.
     *
     * <p>Any potential database errors during the retrieval process trigger the
     * 'networkConnectionError' method of the 'retrieveRideCallback', providing an error message.
     *
     * <p>Implement the 'retrieveRideCallback' interface to handle the received ride data or errors
     * when using this method.
     */
    public void retrieveRideById(String rideId, final retrieveRideCallback callback) {
        DatabaseReference ridesRef = databaseReference.child("rides");
        ridesRef.child(rideId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors
                callback.networkConnectionError(databaseError.getMessage());
            }
        });
    }


    /**
     * Retrieves information about a specific person (driver or rider) from the Firestore database
     * based on the provided user ID.
     *
     * @param userId   The unique identifier of the person whose information needs to be retrieved.
     * @param callback The callback interface to handle the reception of person data.
     *                 Must implement 'retrievePersonCallback'.
     *
     * <p>This method retrieves information about a specific person from the Firestore database,
     * searching for the user ID in the 'DRIVER' and 'RIDER' collections under the 'root' document
     * within the 'USERS' collection. The user ID specifies the document to be retrieved.
     *
     * <p>If the user is identified as a DRIVER, the method retrieves their details (First Name,
     * Last Name, Email, Phone) from the 'DRIVER' collection. If the user is identified as a RIDER,
     * it fetches their information from the 'RIDER' collection.
     *
     * <p>The retrieved data is then encapsulated in a 'Person' object and passed to the
     * 'retrievedPersonData' method of the 'retrievePersonCallback' for further handling.
     *
     * <p>Network connection errors during data retrieval trigger the 'networkConnectionError' method
     * of the 'retrievePersonCallback', providing an error message.
     *
     * <p>Implement the 'retrievePersonCallback' interface to handle the received person data or
     * network connection errors when using this method.
     */
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
                        callback.retrievedPersonData(new Person(firstName, lastName, email, phone, "DRIVER", userId));
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
                                        callback.retrievedPersonData(new Person(firstName, lastName, email, phone, "RIDER", userId));

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


    /**
     * Retrieves and compiles a list of all registered users from the Firestore database.
     *
     * @param callback The callback interface to handle the reception of user data.
     *                 Must implement 'retreiveALlRegisteredUsersCallback'.
     *
     * <p>This method retrieves and compiles a list of all registered users from the Firestore
     * database under the 'USERS' collection for both drivers and riders. It queries the
     * 'DRIVER' and 'RIDER' sub-collections under the 'root' document, retrieving user data
     * such as First Name, Last Name, Email, Phone, and User Type (either DRIVER or RIDER).
     *
     * <p>Upon successfully fetching user data, it assembles a list of 'Person' objects
     * representing each user, categorized by their role (DRIVER or RIDER). This list is then
     * provided to the 'onRecieveUsers' method of the 'retreiveALlRegisteredUsersCallback'.
     *
     * <p>Network connection errors encountered during data retrieval trigger the
     * 'networkConnectionError' method of the 'retreiveALlRegisteredUsersCallback', providing
     * an error message.
     *
     * <p>Implement the 'retreiveALlRegisteredUsersCallback' interface to handle the
     * received user data or network connection errors when using this method.
     */
    public void retrieveAllRegisterdUsers(final retreiveALlRegisteredUsersCallback callback) {
        List<Person> usersList = new ArrayList<>();
        db.collection("USERS")
                .document("DRIVER")
                .collection("root")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.v("sync101", "Number of DRIVER documents: " + queryDocumentSnapshots.size());
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Access document data using document.getData() method
                        String userId = document.getId();
                        String firstName = document.getString("FirstName");
                        String lastName = document.getString("LastName");
                        String email = document.getString("Email");
                        String phone = document.getString("Phone");
                        Person newPerson = new Person(firstName, lastName, email, phone, "DRIVER", userId);
                        Log.v("sync101", firstName);
                        usersList.add(newPerson);
                    }
                    db.collection("USERS")
                            .document("RIDER")
                            .collection("root")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots1 -> {
                                Log.v("sync101", "Number of RIDER documents: " + queryDocumentSnapshots1.size());
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots1) {
                                    // Access document data using document.getData() method
                                    String userId = document.getId();
                                    String firstName = document.getString("FirstName");
                                    String lastName = document.getString("LastName");
                                    String email = document.getString("Email");
                                    String phone = document.getString("Phone");
                                    Person newPerson = new Person(firstName, lastName, email, phone, "RIDER", userId);
                                    Log.v("sync101", firstName);
                                    usersList.add(newPerson);
                                }
                                callback.onRecieveUsers(usersList);
                            })
                            .addOnFailureListener(e -> {
                                callback.networkConnectionError(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    callback.networkConnectionError(e.getMessage());
                });
    }


    /**
     * Books a ride for the current user and sends a booking request to the database.
     *
     * @param ride     The Ride object containing details of the ride to be booked.
     * @param callback The callback interface to handle the booking operation result.
     *                 Must implement 'bookARideCallback'.
     *
     * <p>This method is responsible for booking a ride for the current user by creating a
     * booking request in the database. It checks if a similar booking request for the
     * specified rideId and riderId already exists. If the request exists, it triggers
     * the 'requestAlreadyExists' method of the 'bookARideCallback'. If no similar request
     * is found, a new booking request is added to the database under the 'requests' node,
     * with state awaiting driver acceptance.
     *
     * <p>The 'bookedSuccessfully' method of the 'bookARideCallback' is called upon
     * successful addition of the new request, providing the key of the added record.
     * In case of a failure during the request addition (e.g., network issues), the
     * 'networkConnectionError' method of the 'bookARideCallback' is triggered,
     * providing an error message.
     *
     * <p>Implement the 'bookARideCallback' interface to handle the booking operation's
     * success, failure, or existing request scenarios when using this method.
     */
    public void bookARide(Ride ride, String riderId, final bookARideCallback callback) {
        Log.v("clouddb101", "Booking a ride for the user" + ride.getPickup());
        String rideId = ride.getRideId();

        Map data = new HashMap<>();
        data.put("status", "Awaiting Driver Acceptance");
        data.put("rideId", rideId);
        data.put("riderId", riderId);
        DatabaseReference requestsRef = databaseReference.child("requests");
        // First search if the request already exists
        Query query = requestsRef.orderByChild("rideId").equalTo(ride.getRideId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean requestAlreadyExists = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Get the user with the specified rideId
                    String snapshotRiderId = snapshot.child("riderId").getValue(String.class);
                    if (riderId.equals(snapshotRiderId)) {
                        Log.v("database101", "Request already exists");
                        callback.requestAlreadyExists();
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
                            })
                            .addOnFailureListener(e -> {
                                callback.networkConnectionError(e.getMessage());
                                Log.v("database101", "Failed to add ride");
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


    /**
     * Checks whether a user identified by 'userId' is a rider or a driver by querying
     * the Firestore database.
     *
     * @param userId   The unique identifier of the user to check (rider or driver).
     * @param callback The callback interface to handle the result of the check.
     *                 Must implement 'RiderOrDriverCallback'.
     *
     * <p>This method queries the Firestore database to determine if the user with the provided
     * 'userId' exists as a driver or a rider.
     *
     * <p>It checks for the presence of the 'userId' in the 'DRIVER' and 'RIDER' collections
     * under the 'USERS' document. If the user exists in either collection, it invokes the
     * corresponding method in the 'RiderOrDriverCallback' interface: 'isDriver' or 'isRider'
     * with a boolean value indicating the user's existence as a driver or a rider, respectively.
     *
     * <p>If any error occurs during the database query, such as network issues or data retrieval
     * failures, the 'isRiderOrDriverFetchError' method of the callback is triggered,
     * providing the error message.
     *
     * <p>Implement the 'RiderOrDriverCallback' interface to handle the retrieved results or
     * errors appropriately when using this method.
     */
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
                            callback.isDriver(document.exists());
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
                            callback.isRider(document.exists());
                        } else {
                            callback.isRiderOrDriverFetchError(task.getException().getMessage());
                        }
                    }
                });
    }


    /**
     * Retrieves rides associated with a specific driver from the Firebase Realtime Database.
     *
     * @param userId   The unique identifier of the driver to retrieve rides for.
     * @param callback The callback interface to handle the result of the retrieval operation.
     *                 Must implement 'retrieveDriverRidesCallback'.
     *
     * <p><b>Method Behavior:</b>
     * This method fetches all rides associated with the specified driver, identified by the
     * provided 'userId', from the Firebase Realtime Database. It queries the database for rides
     * with a matching 'driverId'.
     *
     * <p><b>Callback Interface:</b>
     * -Upon successful retrieval of rides, the 'onGetRides' method of the provided callback is
     * invoked, passing the list of retrieved rides. If no rides are found for the specified driver,
     * the 'onNoRides' method of the callback is triggered.
     * -If any error occurs during the retrieval process, the 'networkConnectionError' method
     * of the callback is invoked, providing the error message.
     *
     * <p>It's essential to implement the 'retrieveDriverRidesCallback' interface to handle these
     * callbacks appropriately when using this method.
     */
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


    /**
     * Changes the state of a request in the Firebase Realtime Database.
     *
     * @param requestId The unique identifier of the request to be updated.
     * @param newState  The new state to be set for the request.
     * @param callback  The callback interface to handle the result of the state change operation.
     *                  Must implement 'changeRequestStateCallback'.
     *
     * <p><b>Method Behavior:</b>
     * This method updates the status/state of a specific request identified by the given request ID
     * in the Firebase Realtime Database. It sets the new state specified by the 'newState' parameter.
     * Upon successful update, the 'onSuccessfulChange' method of the provided callback is invoked.
     * If any error occurs during the update process, the 'onFailedChange' method of the callback
     * is triggered with an error message.
     *
     * <p><b>Callback Interface:</b>
     * -The 'onSuccessfulChange' callback indicates a successful update of the request status.
     * -The 'onFailedChange' callback notifies about any failure during the update process.
     * It's essential to implement the 'changeRequestStateCallback' interface to handle these callbacks
     * appropriately when using this method.
     */
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


    /**
     * Fetches available rides from the Firebase database based on specified constraints.
     *
     * @param includePast Indicates whether to include past rides in the retrieved list.
     *                    Set as 'true' to include past rides; 'false' excludes past rides.
     * @param callback    A callback interface to handle the fetched rides or potential errors.
     *                    This callback should not be null and is essential for retrieving data
     *                    asynchronously and handling the results or errors appropriately.
     *                    Implement the 'fetchAvailableRidesCallback' interface to handle
     *                    retrieved rides or errors.
     *
     * <p><b>Method Behavior:</b>
     * The method retrieves ride data from the Firebase database, considering the specified
     * constraints like past or future rides. It iterates through the database snapshots,
     * filters rides based on conditions, and adds eligible rides to the 'ridesList'.
     * Once all rides are processed, it calls the appropriate callback method defined
     * in the 'fetchAvailableRidesCallback' interface to handle the results or errors.
     * If no rides are found or if there's an error during retrieval, the corresponding
     * callbacks are invoked to handle these scenarios.
     *
     * <p><b>Callback Interface:</b>
     * The 'fetchAvailableRidesCallback' provides methods to handle the results
     * or errors from the asynchronous data retrieval process:
     * - 'onGetRides(List<Ride> rides)': Invoked upon successfully fetching available rides.
     *                                   Provides the retrieved rides in a list.
     * - 'onNoRides()': Invoked if no rides are found based on the specified constraints.
     * - 'networkConnectionError(String errorMessage)': Invoked when encountering errors
     *                                                 related to network connections or Firebase.
     *
     * @see fetchAvailableRidesCallback The callback interface for handling retrieved rides
     *                                  or network connection errors.
     */
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
                    String rideId = itemSnapshot.getKey();
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


    public interface retreiveALlRegisteredUsersCallback {
        void onRecieveUsers(List<Person> userList);
        void networkConnectionError(String errorMessage);
    }
}
