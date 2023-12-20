package com.example.majortask.Driver;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.majortask.R;
import com.example.majortask.databinding.FragmentAddRideBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class AddRideFragment extends Fragment {
    private FragmentAddRideBinding binding;
    SharedPreferences sharedPreferences;
    DatabaseReference databaseReference;

    public AddRideFragment() {
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
        binding = FragmentAddRideBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.addRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.addRide.setEnabled(false);
                String pickup, destination, cost, time, day;
                pickup = String.valueOf(binding.pickUp.getText());
                destination = String.valueOf(binding.dropOff.getText());
                cost = String.valueOf(binding.cost.getText());
                time = String.valueOf(binding.time.getText());
                day = String.valueOf(binding.day.getText());

                if (TextUtils.isEmpty(pickup)){
                    Toast.makeText(requireContext(), "Please provide a pick up location", Toast.LENGTH_SHORT).show();
                    binding.addRide.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(destination)){
                    Toast.makeText(requireContext(), "Please provide a destination location", Toast.LENGTH_SHORT).show();
                    binding.addRide.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(cost)){
                    Toast.makeText(requireContext(), "Please provide a cost", Toast.LENGTH_SHORT).show();
                    binding.addRide.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(time)){
                    Toast.makeText(requireContext(), "Please provide a time for the trip", Toast.LENGTH_SHORT).show();
                    binding.addRide.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(day)){
                    Toast.makeText(requireContext(), "Please provide a day for the trip", Toast.LENGTH_SHORT).show();
                    binding.addRide.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                if ( !isValidDateFormat(day, "dd-MM-yyyy")){
                    binding.day.setText("");
                    Toast.makeText(requireContext(), "Please provide a valid day for the trip", Toast.LENGTH_SHORT).show();
                    binding.addRide.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                if ( !isValidTimeFormat(time, "HH:mm")){
                    binding.time.setText("");
                    Toast.makeText(requireContext(), "Please provide a valid time for the trip", Toast.LENGTH_SHORT).show();
                    binding.addRide.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                // add the ride to firstore realtime database
                Log.v("clouddb101", "Collected data from GUI successfully");

                Map data = new HashMap<>();
                data.put("pickup", pickup);
                data.put("dropoff", destination);
                data.put("cost", cost);
                data.put("time", time);
                data.put("day", day);
                data.put("driverId", sharedPreferences.getString("loggedUser",""));
                DatabaseReference ridesRef = databaseReference.child("rides");
                DatabaseReference newRecord = ridesRef.push();
                newRecord.setValue(data)
                        .addOnSuccessListener( aVoid ->{
                            binding.pickUp.setText("");
                            binding.dropOff.setText("");
                            binding.time.setText("");
                            binding.day.setText("");
                            binding.cost.setText("");
                            binding.addRide.setEnabled(true);
                            binding.progressBar.setVisibility(View.GONE);
                            Log.v("clouddb101", "Added Ride Successfully");
                            Toast.makeText(requireContext(), "Ride added successfully!",
                                    Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            binding.addRide.setEnabled(true);
                            binding.progressBar.setVisibility(View.GONE);
                            Log.v("clouddb101", "Failed to add ride");
                            Toast.makeText(requireContext(), "Failed to add ride please check your internet connectivity.",
                                    Toast.LENGTH_SHORT).show();
                        });

            }
        });
    }


    public static boolean isValidDateFormat(String dateStr, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false); // Set lenient to false to ensure strict date parsing
        try {
            sdf.parse(dateStr); // Try parsing the string
            return true; // If parsing succeeds, date format is valid
        } catch (ParseException e) {
            return false; // Parsing failed, invalid date format
        }
    }
    public static boolean isValidTimeFormat(String timeStr, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false); // Set lenient to false to ensure strict time parsing
        try {
            sdf.parse(timeStr); // Try parsing the string
            return true; // If parsing succeeds, time format is valid
        } catch (ParseException e) {
            return false; // Parsing failed, invalid time format
        }
    }

}