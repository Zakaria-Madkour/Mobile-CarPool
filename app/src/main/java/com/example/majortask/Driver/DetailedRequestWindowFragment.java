package com.example.majortask.Driver;

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
import com.example.majortask.Utils.Request;
import com.example.majortask.Utils.Ride;
import com.example.majortask.databinding.FragmentDetailedRequestWindowBinding;


public class DetailedRequestWindowFragment extends Fragment {
    FragmentDetailedRequestWindowBinding binding;
    FirebaseHelper firebaseHelper;
    Ride ride;
    Request request;

    public DetailedRequestWindowFragment(Ride ride, Request request) {
        this.ride = ride;
        this.request = request;
    }

    public DetailedRequestWindowFragment(int contentLayoutId, Ride ride, Request request) {
        super(contentLayoutId);
        this.ride = ride;
        this.request = request;
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
        binding = FragmentDetailedRequestWindowBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (request.getStatus().equals("Awaiting Driver Acceptance")) {
            binding.rejectButton.setEnabled(true);
            binding.acceptButton.setEnabled(true);
        } else if (request.getStatus().equals("Awaiting Payment")) {
            binding.rejectButton.setEnabled(true);
            binding.acceptButton.setEnabled(false);
        } else {
            binding.rejectButton.setEnabled(false);
            binding.acceptButton.setEnabled(false);
        }
        binding.rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseHelper.changeRequestState(request.getRequestId(), "Rejected by Driver", new FirebaseHelper.changeRequestStateCallback() {
                    @Override
                    public void onSuccessfulChange() {
                        binding.rejectButton.setEnabled(false);
                        binding.acceptButton.setEnabled(false);
                    }

                    @Override
                    public void onFailedChange(String errorMessage) {
                        Toast.makeText(requireContext(), "Failed to reject request!" + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        binding.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseHelper.changeRequestState(request.getRequestId(), "Awaiting Payment", new FirebaseHelper.changeRequestStateCallback() {
                    @Override
                    public void onSuccessfulChange() {
                        binding.rejectButton.setEnabled(false);
                        binding.acceptButton.setEnabled(false);
                    }

                    @Override
                    public void onFailedChange(String errorMessage) {
                        Toast.makeText(requireContext(), "Failed to reject request!" + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        binding.pickUp.setText(ride.getPickup());
        binding.dropOff.setText(ride.getDestination());
        binding.rideTime.setText(ride.getTime());
        binding.rideDay.setText(ride.getDay());
        binding.rideCost.setText(ride.getCost());
        binding.status.setText(request.getStatus());


        try {
            Roomdb.getUserById(this.getContext(), request.getRiderId(), new Roomdb.getUserCallbacks() {
                @Override
                public void onUserFound(Person person) {
                    binding.driverFirstName.setText(person.getFirstName());
                    binding.driverLastName.setText(person.getLastName());
                    binding.driverEmail.setText(person.getEmail());
                    binding.driverPhone.setText(person.getPhone());
                }

                @Override
                public void onUserNotFound(String errorMessage) {
                    Toast.makeText(requireContext(), "Connectivity Error! Make sure you have stable internet!" + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.v("sync101", e.getMessage());
        }

    }
}