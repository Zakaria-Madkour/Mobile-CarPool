package com.example.majortask.Rider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.majortask.R;
import com.example.majortask.Utils.FirebaseHelper;
import com.example.majortask.Utils.Ride;

import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {
    private List<Ride> rideList;
    private OnRideItemClickListener listener;

    public RideAdapter(List<Ride> rideList, OnRideItemClickListener listener) {
        this.rideList = rideList;
        this.listener = listener;
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
        holder.time.setText(ride.getTime());
        holder.cost.setText(String.valueOf(ride.getCost()));
        holder.date.setText(String.valueOf(ride.getDay()));
        holder.bookRide.setOnClickListener(n -> FirebaseHelper.bookARide(rideList.get(holder.getAdapterPosition())));
        holder.bookRide.setEnabled(ride.getStatus());
        holder.wholeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRideItemClicked(rideList.get(holder.getAdapterPosition()));
            }
        });

    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    public static class RideViewHolder extends RecyclerView.ViewHolder{
        TextView pickUp;
        TextView dropOff;
        TextView cost;
        TextView time, date;
        Button bookRide;
        LinearLayout wholeItem;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            pickUp = itemView.findViewById(R.id.pickUp);
            dropOff = itemView.findViewById(R.id.dropOff);
            cost = itemView.findViewById(R.id.money);
            time = itemView.findViewById(R.id.time);
            date = itemView.findViewById(R.id.date);
            bookRide = itemView.findViewById(R.id.addToCartButton);
            wholeItem = itemView.findViewById(R.id.rideItem);
        }
    }
}
