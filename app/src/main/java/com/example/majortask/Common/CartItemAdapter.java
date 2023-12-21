package com.example.majortask.Common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.majortask.R;
import com.example.majortask.Utils.Request;
import com.example.majortask.Utils.Ride;

import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder> {
    private List<Request> requestRideMap;
    private List<Ride> rideslist;
    private OnCartItemClickListener listener;

    public CartItemAdapter(List<Request> requestRideMap, List<Ride> rideslist, OnCartItemClickListener listener) {
        this.requestRideMap = requestRideMap;
        this.rideslist = rideslist;
        this.listener = listener;
    }


    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        return new CartItemViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        Request request = requestRideMap.get(position);
        Ride ride = rideslist.get(position);

        holder.pickup.setText(ride.getPickup());
        holder.dropOff.setText(ride.getDestination());
        holder.date.setText(ride.getDay());
        holder.time.setText(ride.getTime());
        holder.status.setText(request.getStatus());
        holder.wholeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCartItemClicked(requestRideMap.get(holder.getAdapterPosition()), rideslist.get(holder.getAdapterPosition()));
            }
        });
    }


    @Override
    public int getItemCount() {
        return requestRideMap.size();
    }


    public static class CartItemViewHolder extends RecyclerView.ViewHolder {
        TextView pickup, dropOff, time, date, status;
        LinearLayout wholeItem;

        public CartItemViewHolder(@NonNull View itemView) {
            super(itemView);
            pickup = itemView.findViewById(R.id.pickUp);
            dropOff = itemView.findViewById(R.id.dropOff);
            time = itemView.findViewById(R.id.time);
            date = itemView.findViewById(R.id.date);
            status = itemView.findViewById(R.id.status);
            wholeItem = itemView.findViewById(R.id.cartItem);
        }
    }
}
