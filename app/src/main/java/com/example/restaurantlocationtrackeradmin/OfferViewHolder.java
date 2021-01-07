package com.example.restaurantlocationtrackeradmin;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OfferViewHolder extends RecyclerView.ViewHolder {
    TextView offerTV;
    public OfferViewHolder(@NonNull View itemView) {
        super(itemView);
        offerTV=itemView.findViewById(R.id.offerRowTV);
    }
}
