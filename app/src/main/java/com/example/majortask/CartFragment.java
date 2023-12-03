package com.example.majortask;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CartFragment extends Fragment {
    private List<Ride> ridesList;
    private RecyclerView homeRecyclerView;
    private RideAdapter rideAdapter;
    private Button payButton;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CartFragment newInstance(String param1, String param2) {
        CartFragment fragment = new CartFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_cart, container, false);
        ridesList = generateRides();

        payButton = rootView.findViewById(R.id.checkoutButton);
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("demo101","Button Clicked");
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frame_layout, new CheckoutFragment());
                fragmentTransaction.commit();
            }
        });

        homeRecyclerView = rootView.findViewById(R.id.cartRecyclerView);
        homeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        rideAdapter = new RideAdapter(ridesList);
        homeRecyclerView.setAdapter(rideAdapter);
        return rootView;
    }


    private List<Ride> generateRides(){
        List<Ride> offeredRidesList = new ArrayList<>();
        Car car1 = new Car("rt4q","Ahmed","black","Toyota","corolla");
        offeredRidesList.add(new Ride("Gate3", "Abassia","4:30",22.5,car1));

        Car car2 = new Car("4sr","Ahmed","white","Toyota","corolla");
        offeredRidesList.add(new Ride("Gate4", "Abassia","4:30",25.5,car2));

        Car car3 = new Car("r3q","Mahmoud","black","Toyota","corolla");
        offeredRidesList.add(new Ride("Abbasia", "Gate3","4:30",24.95,car3));

        Car car4 = new Car("r234q","Mohamed","grey","Toyota","corolla");
        offeredRidesList.add(new Ride("Abassia", "Gate4","4:30",22.885,car4));

        return offeredRidesList;
    }
}