package com.example.majortask.Common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

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

import com.example.majortask.Driver.DetailedRequestWindowFragment;
import com.example.majortask.Rider.DetailedCartItemFragment;
import com.example.majortask.R;
import com.example.majortask.Utils.FirebaseHelper;
import com.example.majortask.Utils.Request;
import com.example.majortask.Utils.Ride;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {
    private List<Request> requestsRidesList;
    private List<Ride> ridesList;
    private RecyclerView cartRecyclerView;
    private CartItemAdapter cartItemAdapter;
    private FirebaseHelper databaseHelper;
    private String mode;
    SharedPreferences sharedPreferences;

    public CartFragment(String mode) {
        this.mode = mode;
    }

    public CartFragment(int contentLayoutId, String mode) {
        super(contentLayoutId);
        this.mode = mode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new FirebaseHelper();
        sharedPreferences =  requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_cart, container, false);

        cartRecyclerView = rootView.findViewById(R.id.cartRecyclerView);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //-------------------------------------------------------------------------------------------
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setView(R.layout.progress_layout);
        AlertDialog dialog = builder.create();
        dialog.show();
        requestsRidesList = new ArrayList<>();
        ridesList = new ArrayList<>();

        if (mode.equals("RIDER")){
            cartItemAdapter = new CartItemAdapter(requestsRidesList, ridesList, new OnCartItemClickListener() {
                @Override
                public void onCartItemClicked(Request request, Ride ride) {
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frame_layout, new DetailedCartItemFragment(request, ride));
                    fragmentTransaction.commit();
                }
            });
            cartRecyclerView.setAdapter(cartItemAdapter);
            dialog.show();
            String userId = sharedPreferences.getString("loggedUser","");
            databaseHelper.rideCartQuery(userId, new FirebaseHelper.riderCartQueryCallback() {
                @Override
                public void onGetCartItems(List<Request> requestsList, List<Ride> rideList) {
                    requestsRidesList.clear();
                    ridesList.clear();

                    requestsRidesList.addAll(requestsList);
                    ridesList.addAll(rideList);
                    Log.v("clouddb101",requestsRidesList.toString());
                    cartItemAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
                @Override
                public void onEmptyCart() {
                    Log.v("debug101","No Items in cart");
                    cartItemAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
                @Override
                public void networkConnectionError(String errorMessage) {
                    Log.v("debug101","Network Error"+errorMessage);
                }
            });


        } else if (mode.equals("DRIVER")) {

            cartItemAdapter = new CartItemAdapter(requestsRidesList, ridesList, new OnCartItemClickListener() {
                @Override
                public void onCartItemClicked(Request request, Ride ride) {
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frame_layout, new DetailedRequestWindowFragment(ride,request));
                    fragmentTransaction.commit();
                }
            });
            cartRecyclerView.setAdapter(cartItemAdapter);
            dialog.show();
            String userId = sharedPreferences.getString("loggedUser","");

            databaseHelper.driverRequestsQuery(userId, new FirebaseHelper.driverRequestsQueryCallback() {
                @Override
                public void onGetRequests(List<Request> requestsList, List<Ride> rideList) {
                    requestsRidesList.clear();
                    ridesList.clear();

                    requestsRidesList.addAll(requestsList);
                    ridesList.addAll(rideList);
                    Log.v("clouddb101",requestsRidesList.toString());
                    Log.v("clouddb101",ridesList.toString());
                    cartItemAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }

                @Override
                public void onNoRequests() {
                    Log.v("debug101","No requests for this driver");
                    cartItemAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                    Toast.makeText(requireContext(),"You Don't have any requests!",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void networkConnectionError(String errorMessage) {
                    Log.v("debug101","Network Error"+errorMessage);
                    Toast.makeText(requireContext(),"Network error. Check connectivity!",Toast.LENGTH_SHORT).show();
                }
            });
        }
        return rootView;
    }
}