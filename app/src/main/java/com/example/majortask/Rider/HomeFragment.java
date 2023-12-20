package com.example.majortask.Rider;

import static android.service.controls.ControlsProviderService.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.majortask.R;
import com.example.majortask.Utils.Ride;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class HomeFragment extends Fragment {

    private List<Ride> ridesList;
    private RecyclerView homeRecyclerView;
    private RideAdapter rideAdapter;
    private DatabaseReference databaseReference;
    private ValueEventListener eventListener;



    public HomeFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        homeRecyclerView = rootView.findViewById(R.id.homeRecyclerView);
        homeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        //-------------------------------------------------------------------------------------------
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();
        ridesList = new ArrayList<>();
        rideAdapter = new RideAdapter(ridesList, new OnRideItemClickListener() {
            @Override//specifying that we change screen to detailed rideitem when it is clicked
            public void onRideItemClicked(Ride ride) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frame_layout, new RideOrderDetailsFragment(ride));
                fragmentTransaction.commit();
            }
        });
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
                    Boolean state = true;

                    // Condition check on the timing constraints
                    Boolean condition = false;
                    LocalDateTime currentDateTime = LocalDateTime.now();
                    String dateTimeString = date+"T"+time;

                    // Define multiple date-time format patterns
                    String[] patterns = {"dd-MM-yyyy'T'HH:mm", "dd-MM-yyyy'T'H:mm","dd-MM-yyyy'T'HH:m","dd-MM-yyyy'T'H:m",
                                         "d-MM-yyyy'T'HH:mm", "d-MM-yyyy'T'H:mm","d-MM-yyyy'T'HH:m","d-MM-yyyy'T'H:m",
                                         "dd-M-yyyy'T'HH:mm", "dd-M-yyyy'T'H:mm","dd-M-yyyy'T'HH:m","dd-M-yyyy'T'H:m",
                                        "d-M-yyyy'T'HH:mm", "d-M-yyyy'T'H:mm","d-M-yyyy'T'HH:m","d-M-yyyy'T'H:m",};

                    // Create a DateTimeFormatterBuilder and add the patterns
                    DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
                    for (String pattern : patterns) {
                        builder.appendOptional(DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT));
                    }

                    // Create the DateTimeFormatter with the combined patterns
                    DateTimeFormatter formatter = builder.toFormatter(Locale.ENGLISH);
                    // Parse the string into a LocalDateTime object using the formatter
                    try {
                        LocalDateTime parsedDateTime = LocalDateTime.parse(dateTimeString, formatter);
                        // Calculate the difference using Duration.between
                        Duration duration = Duration.between(currentDateTime, parsedDateTime);
                        // Get the difference in hours and minutes
                        double hoursDifference = duration.toHours();
                        long minutesDifference = duration.toMinutes() % 60;
                        hoursDifference += minutesDifference/60.0;
                        if(time.equals("7:30") && hoursDifference < 9.5){
                            condition = true;
                        }
                        if (time.equals("5:30") && hoursDifference < 4.5){
                            condition = true;
                        }
                        if(condition){
                            state = false;
                        }

                        Ride myRide = new Ride(pickup,dropoff,time,cost,state,driverId,rideId,date);
                        // Add only upcoming rides ( Ignore rides of the past )
                        if(currentDateTime.isBefore(parsedDateTime)){
                            ridesList.add(myRide);
                        }
                    }
                    catch ( DateTimeParseException e){
                        Log.e(TAG, "Error: Invalid date-time format or value"+e.toString());
                        Toast.makeText(requireContext(), "Error: Invalid date-time format or value"+e.toString(),
                                Toast.LENGTH_SHORT).show();
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
        return rootView;
    }

}