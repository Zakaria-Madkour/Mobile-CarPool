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
import com.example.majortask.Utils.FirebaseHelper;
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
    private FirebaseHelper firebaseHelper;


    public HomeFragment() {
        // Required empty public constructor
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
        dialog.show();


        firebaseHelper.fetchAvailableRides(false, new FirebaseHelper.fetchAvailableRidesCallback() {
            @Override
            public void onGetRides(List<Ride> rideList) {
                ridesList.clear();
                ridesList.addAll(rideList);
                rideAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onNoRides() {
                Log.v("debug101", "No rides in database to fetch");
                rideAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void networkConnectionError(String errorMessage) {
                Log.v("debug101", "Network Error" + errorMessage);
            }
        });
        return rootView;
    }
}