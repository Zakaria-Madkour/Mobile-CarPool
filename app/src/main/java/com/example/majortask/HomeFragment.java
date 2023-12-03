package com.example.majortask;

import static android.service.controls.ControlsProviderService.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private List<Ride> ridesList;
    private RecyclerView homeRecyclerView;
    private RideAdapter rideAdapter;
    private DatabaseReference databaseReference;
    private ValueEventListener eventListener;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

//        ridesList = generateRides();

        homeRecyclerView = rootView.findViewById(R.id.homeRecyclerView);
        homeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        //-------------------------------------------------------------------------------------------
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();
        ridesList = new ArrayList<>();
        rideAdapter = new RideAdapter(ridesList);
        homeRecyclerView.setAdapter(rideAdapter);
        databaseReference = FirebaseDatabase.getInstance().getReference("rides");
        dialog.show();

        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ridesList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Log.v("demo101", itemSnapshot.child("car").child("make").getValue().toString());

                    String pickup = itemSnapshot.child("pickup").getValue().toString();
                    String dropoff = itemSnapshot.child("dropoff").getValue().toString();
                    String time = itemSnapshot.child("time").getValue().toString();
                    Double cost = Double.valueOf(itemSnapshot.child("cost").getValue().toString());

                    String licencePlate = itemSnapshot.child("car").child("licencePlate").getValue().toString();
                    String make = itemSnapshot.child("car").child("make").getValue().toString();
                    String model = itemSnapshot.child("car").child("model").getValue().toString();
                    String color = itemSnapshot.child("car").child("color").getValue().toString();
                    String driver = itemSnapshot.child("car").child("driver").getValue().toString();

                    Ride myRide = new Ride(pickup,dropoff,time,cost,new Car(licencePlate,driver,color,make,model));
                    ridesList.add(myRide);
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
        return rootView;
    }


    private List<Ride> generateRides() {
        List<Ride> offeredRidesList = new ArrayList<>();
//        Car car1 = new Car("rt4q","Ahmed","black","Toyota","corolla");
//        offeredRidesList.add(new Ride("Gate3", "Abassia","4:30",22.5,car1));
//
//        Car car2 = new Car("4sr","Ahmed","white","Toyota","corolla");
//        offeredRidesList.add(new Ride("Gate4", "Abassia","4:30",25.5,car2));
//
//        Car car3 = new Car("r3q","Mahmoud","black","Toyota","corolla");
//        offeredRidesList.add(new Ride("Abbasia", "Gate3","4:30",24.95,car3));
//
//        Car car4 = new Car("r234q","Mohamed","grey","Toyota","corolla");
//        offeredRidesList.add(new Ride("Abassia", "Gate4","7:00",22.885,car4));
        databaseReference = FirebaseDatabase.getInstance().getReference("rides");

        return offeredRidesList;
    }
}