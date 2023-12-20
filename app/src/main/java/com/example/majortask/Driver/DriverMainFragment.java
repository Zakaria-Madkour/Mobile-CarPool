package com.example.majortask.Driver;

import static android.service.controls.ControlsProviderService.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.majortask.R;
import com.example.majortask.Rider.RideAdapter;
import com.example.majortask.Utils.Ride;
import com.example.majortask.databinding.FragmentDriverMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class DriverMainFragment extends Fragment {
    private FragmentDriverMainBinding binding;
    SharedPreferences sharedPreferences;
    DatabaseReference databaseReference;
    private RecyclerView homeRecyclerView;
    private RideAdapterDriver rideAdapter;
    private ValueEventListener eventListener;
    private List<Ride> ridesList;


    public DriverMainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        sharedPreferences =  requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDriverMainBinding.inflate(inflater, container, false);

        homeRecyclerView = binding.homeRecyclerViewDriver;
        homeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //---------------------------------- Set the screen to loading---------------------------------------------------------
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();
        ridesList = new ArrayList<>();
        rideAdapter = new RideAdapterDriver(ridesList);
        homeRecyclerView.setAdapter(rideAdapter);
        databaseReference = FirebaseDatabase.getInstance().getReference("rides");
        dialog.show();
        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ridesList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Log.v("demo101", itemSnapshot.getKey().toString());

                    String pickup = itemSnapshot.child("pickup").getValue().toString();
                    String dropoff = itemSnapshot.child("dropoff").getValue().toString();
                    String time = itemSnapshot.child("time").getValue().toString();
                    String cost = itemSnapshot.child("cost").getValue().toString();
                    String date = itemSnapshot.child("day").getValue().toString();
                    String driverId = itemSnapshot.child("driverId").getValue().toString();
                    String rideId = itemSnapshot.getKey().toString();
                    // Check if the driverId is the same of this current user logged in
                    if(sharedPreferences.getString("loggedUser","") .equals(driverId)){
                        Ride myRide = new Ride(pickup,dropoff,time,cost,driverId,rideId,date);
                        ridesList.add(myRide);
                    }
                }
                rideAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialog.dismiss();
                Log.w(TAG, "loadPost:onCancelled", error.toException());
            }
        });


        return binding.getRoot();
    }
}