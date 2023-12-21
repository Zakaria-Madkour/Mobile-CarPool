package com.example.majortask.Rider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.majortask.R;
import com.example.majortask.Utils.FirebaseHelper;
import com.example.majortask.Utils.Person;
import com.example.majortask.Utils.ROOM.Roomdb;
import com.example.majortask.Utils.Ride;
import com.example.majortask.databinding.FragmentRideOrderDetailsBinding;

public class RideOrderDetailsFragment extends Fragment {
    private Ride ride;
    private FirebaseHelper firebaseHelper;
    private FragmentRideOrderDetailsBinding binding;

    public RideOrderDetailsFragment(Ride ride) {
        this.ride = ride;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        firebaseHelper = new FirebaseHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRideOrderDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.pickUp.setText(ride.getPickup());
        binding.dropOff.setText(ride.getDestination());
        binding.rideCost.setText(String.valueOf(ride.getCost()));
        binding.rideTime.setText(ride.getTime());
        binding.rideDay.setText(ride.getDay());

        try {
            Roomdb.getUserById(this.getContext(), ride.getDriverId(), new Roomdb.getUserCallbacks() {
                @Override
                public void onUserFound(Person person) {
                    binding.driverFirstName.setText(person.getFirstName());
                    binding.driverLastName.setText(person.getLastName());
                    binding.driverEmail.setText(person.getEmail());
                    binding.driverPhone.setText(person.getPhone());
                }

                @Override
                public void onUserNotFound(String errorMessage) {
                    Log.v("clouddb101", "Problem retrieving driver data" + errorMessage);
                    Toast.makeText(requireContext(), "Couldn't get driver data from cloud",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.v("sync101", e.getMessage());
        }

        binding.addToCartButton.setEnabled(ride.getStatus());
        binding.addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseHelper.bookARide(ride, new FirebaseHelper.bookARideCallback() {
                    @Override
                    public void bookedSuccessfully(String requestId) {
                        Log.v("clouddb101", requestId);
                    }

                    @Override
                    public void requestAlreadyExists() {
                        Toast.makeText(requireContext(), "Request already exists!",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void networkConnectionError(String errorMessage) {
                        Toast.makeText(requireContext(), "Error while requesting ride!!!" + errorMessage,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

}