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
import com.example.majortask.Utils.FirebaseHelper;
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
//    DatabaseReference databaseReference;
    private RecyclerView homeRecyclerView;
    private RideAdapterDriver rideAdapter;
    private List<Ride> ridesList;
    private FirebaseHelper databaseHelper;


    public DriverMainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new FirebaseHelper();
//        databaseReference = FirebaseDatabase.getInstance().getReference();
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
        dialog.show();
        String userId = sharedPreferences.getString("loggedUser","");

        databaseHelper.retrieveDriverRides(userId, new FirebaseHelper.retrieveDriverRidesCallback() {
            @Override
            public void onGetRides(List<Ride> rideList) {
                ridesList.clear();
                ridesList.addAll(rideList);
                Log.v("clouddb101",rideList.toString());
                rideAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
            @Override
            public void onNoRides() {
                Log.v("debug101","No available Rides by driver");
                rideAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
            @Override
            public void networkConnectionError(String errorMessage) {
                Log.v("debug101","Network Error"+errorMessage);
            }
        });
        return binding.getRoot();
    }
}