package com.example.majortask.Utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public  class FirebaseHelper {
    private  FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private DatabaseReference databaseReference;

    public FirebaseHelper() {
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }
    public static void bookARide(Ride r, String userId){
        Log.v("clouddb101", "Booking a ride for the user"+r.getPickup());

    }

    public static void bookARide(Ride r){
        Log.v("clouddb101", "Booking a ride for the user"+r.getPickup());
    }
    public void checkIfRiderOrDriver(String userId, final RiderOrDriverCallback callback){
        db.collection("USERS")
                .document("DRIVER")
                .collection("root")
                .document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                callback.isDriver(true);
                            }else
                            {
                                callback.isDriver(false);
                            }
                        }else {
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
                        if (task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                callback.isRider(true);
                            }else
                            {
                                callback.isRider(false);
                            }
                        }else {
                            callback.isRiderOrDriverFetchError(task.getException().getMessage());
                        }
                    }
                });

    }
    public interface RiderOrDriverCallback{
        void isRider(boolean rider);
        void isDriver(boolean driver);
        void isRiderOrDriverFetchError(String errorMessage);
    }
    public interface bookARideCallback{
        void booked(boolean bookedSuccessfully);
        void networkConnectionError(String errorMessage);
    }
}
