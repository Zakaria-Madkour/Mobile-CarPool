package com.example.majortask.Rider;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import com.example.majortask.databinding.FragmentDetailedCartItemBinding;


public class DetailedCartItemFragment extends Fragment {
    FragmentDetailedCartItemBinding binding;
    private FirebaseHelper firebaseHelper;
    private Request request;
    private Ride ride;

    public DetailedCartItemFragment(Request request, Ride ride) {
        this.request = request;
        this.ride = ride;
    }

    public DetailedCartItemFragment(int contentLayoutId, Request request, Ride ride) {
        super(contentLayoutId);
        this.request = request;
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
        binding = FragmentDetailedCartItemBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (request.getStatus().equals("Awaiting Driver Acceptance")) {
            binding.status.setTextColor(Color.YELLOW);
            binding.paymentButton.setEnabled(false);
        } else if (request.getStatus().equals("Awaiting Payment")) {
            binding.status.setTextColor(Color.GREEN);
            binding.paymentButton.setEnabled(true);
        } else if (request.getStatus().equals("Rejected by Driver")) {
            binding.status.setTextColor(Color.RED);
            binding.paymentButton.setEnabled(false);
        } else if (request.getStatus().equals("Paid")) {
            binding.status.setTextColor(Color.GREEN);
            binding.paymentButton.setEnabled(false);
        }
        binding.paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseHelper.changeRequestState(request.getRequestId(), "Paid", new FirebaseHelper.changeRequestStateCallback() {
                    @Override
                    public void onSuccessfulChange() {
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frame_layout, new CheckoutFragment());
                        fragmentTransaction.commit();
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
                    Toast.makeText(requireContext(), "Network error please check connectivity", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.v("sync101", e.getMessage());
        }
    }
}