package com.example.restaurantlocationtrackeradmin;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RestaurantViewHolder extends RecyclerView.ViewHolder {
    TextView nameTV;
    TextView phoneTV;
    public RestaurantViewHolder(@NonNull View itemView) {
        super(itemView);
        nameTV=itemView.findViewById(R.id.restaurantRowNameTV);
        phoneTV=itemView.findViewById(R.id.restaurantRowPhoneTV);
    }
}
