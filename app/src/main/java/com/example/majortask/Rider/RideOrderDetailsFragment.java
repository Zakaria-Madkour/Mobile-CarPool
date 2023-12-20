package com.example.majortask.Rider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.majortask.R;
import com.example.majortask.Utils.Ride;
import com.example.majortask.databinding.FragmentRideOrderDetailsBinding;

public class RideOrderDetailsFragment extends Fragment {
    private Ride ride;
    private FragmentRideOrderDetailsBinding binding;

    public RideOrderDetailsFragment(Ride ride) {
        this.ride = ride;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRideOrderDetailsBinding.inflate(inflater,container, false);
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
        binding.addToCartButton.setEnabled(ride.getStatus());
        binding.addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

}