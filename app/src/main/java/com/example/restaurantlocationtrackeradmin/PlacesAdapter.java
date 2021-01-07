package com.example.restaurantlocationtrackeradmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by saran on 1/2/2018.
 */

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder>{
    private Context MyContext;
    private List<NearByPlacesResponse.Result> places;
    ClickListener listener;
    FirebaseDatabase database;
    DatabaseReference restaurantDatabase;
    DatabaseReference offerDatabase;

    public PlacesAdapter(Context myContext, List<NearByPlacesResponse.Result> places, ClickListener listener) {
        MyContext = myContext;
        this.places=places;
        this.listener=listener;

        database=FirebaseDatabase.getInstance();
        restaurantDatabase=database.getReference().child("restaurants");
        offerDatabase=database.getReference().child("offers");
    }


    @Override
    public PlacesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(MyContext).inflate(R.layout.places_single_row,parent,false);
        return new PlacesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlacesViewHolder holder, int position) {

        NearByPlacesResponse.Result place=places.get(position);
        String placeName=(position+1)+"."+place.getName();

        restaurantDatabase.child(place.getPlaceId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.placeNameTV.setText(placeName);
                holder.checkIV.setVisibility(View.GONE);
                holder.offerTV.setVisibility(View.GONE);

                try {
                    if (place.getOpeningHours().getOpenNow())
                        holder.openTV.setText("Open");
                    else holder.openTV.setText("Closed");
                }catch (NullPointerException e){}
                if(dataSnapshot.getValue()!=null){
                    holder.checkIV.setVisibility(View.VISIBLE);
                    offerDatabase.child(place.getPlaceId()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue()!=null){
                                holder.offerTV.setVisibility(View.VISIBLE);
                            }else {
                                holder.offerTV.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            holder.offerTV.setVisibility(View.GONE);


                        }
                    });
                }else {
                    holder.checkIV.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return places.size();
    }


    public class PlacesViewHolder extends RecyclerView.ViewHolder {
        TextView placeNameTV;
        TextView openTV;
        ImageView checkIV;
        TextView offerTV;
        public PlacesViewHolder(View itemView) {
            super(itemView);
            placeNameTV=itemView.findViewById(R.id.place_nameTV);
            openTV=itemView.findViewById(R.id.openTV);
            checkIV=itemView.findViewById(R.id.checkedIV);
            offerTV=itemView.findViewById(R.id.offerTV);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(places.get(getAdapterPosition()));
                }
            });
        }
    }

    public void Update(List<NearByPlacesResponse.Result> places){
        this.places=places;
        notifyDataSetChanged();
    }
    public interface ClickListener{
        void onClick(NearByPlacesResponse.Result place);
    }

}
