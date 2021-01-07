package com.example.restaurantlocationtrackeradmin;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MenuViewHolder extends RecyclerView.ViewHolder {
    CircleImageView menuIV;
    TextView menuNameTV;
    TextView portionTV;
    TextView priceTV;


    public MenuViewHolder(@NonNull View itemView) {
        super(itemView);
        menuIV=itemView.findViewById(R.id.menuItemIV);
        menuNameTV=itemView.findViewById(R.id.menuNameTV);
        portionTV=itemView.findViewById(R.id.portionTV);
        priceTV=itemView.findViewById(R.id.priceTV);
    }
}
