package com.example.majortask;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {
    private List<Ride> rideList;
    public RideAdapter(List<Ride> rideList) {
        this.rideList = rideList;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride, parent, false);
        return new RideViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        Ride ride = rideList.get(position);
        holder.pickUp.setText(ride.getPickup());
        holder.dropOff.setText(ride.getDestination());
        holder.time.setText(ride.getTime().toString());
        holder.cost.setText(String.valueOf(ride.getCost()));

        String licencePlate = ride.getCar().getLicencePlate();
        String make = ride.getCar().getMake();
        String model = ride.getCar().getModel();
        String color = ride.getCar().getColor();
        String driver = ride.getCar().getDriver();
        holder.carInfo.setText(licencePlate+"/"+make+" "+model+"/"+color+"=>"+driver);
    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    public static class RideViewHolder extends RecyclerView.ViewHolder{
        TextView pickUp;
        TextView dropOff;
        TextView cost;
        TextView time;
        TextView carInfo;
        Button bookRide;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            pickUp = itemView.findViewById(R.id.pickUp);
            dropOff = itemView.findViewById(R.id.dropOff);
            cost = itemView.findViewById(R.id.money);
            time = itemView.findViewById(R.id.time);
            carInfo = itemView.findViewById(R.id.car);
            bookRide = itemView.findViewById(R.id.addToCartButton);
        }
    }
}
