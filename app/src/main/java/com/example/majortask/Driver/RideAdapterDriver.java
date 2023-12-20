package com.example.majortask.Driver;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.majortask.R;
import com.example.majortask.Utils.Ride;

import java.util.List;

public class RideAdapterDriver extends RecyclerView.Adapter<RideAdapterDriver.DriverViewHolder> {
    private List<Ride> ridesList;

    public RideAdapterDriver(List<Ride> ridesList) {
        this.ridesList = ridesList;
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_driver, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return ridesList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        Ride ride = ridesList.get(position);
        holder.pickup.setText(ride.getPickup());
        holder.destination.setText(ride.getDestination());
        holder.time.setText(ride.getTime());
        holder.cost.setText(ride.getCost());
        holder.date.setText(ride.getDay());
    }

    public static class DriverViewHolder extends RecyclerView.ViewHolder{
        TextView pickup, destination, cost, time, date;

        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);
            pickup = itemView.findViewById(R.id.pickUp);
            destination = itemView.findViewById(R.id.dropOff);
            cost = itemView.findViewById(R.id.money);
            time = itemView.findViewById(R.id.time);
            date = itemView.findViewById(R.id.date);
        }
    }

}

