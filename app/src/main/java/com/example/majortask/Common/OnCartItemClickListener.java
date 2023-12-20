package com.example.majortask.Common;

import com.example.majortask.Utils.Request;
import com.example.majortask.Utils.Ride;

import java.util.Map;

public interface OnCartItemClickListener {
    void onCartItemClicked(Request request, Ride ride);
}
