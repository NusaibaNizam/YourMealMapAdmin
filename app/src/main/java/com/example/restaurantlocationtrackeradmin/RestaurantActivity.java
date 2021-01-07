package com.example.restaurantlocationtrackeradmin;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RestaurantActivity extends AppCompatActivity {

    private static final int CALL_REQUEST_CODE = 676;
    private Restaurant restaurant;
    ImageView imageView;
    TextView nameTV;
    TextView phoneTV;
    LinearLayout offerLL;
    RecyclerView offerRV;
    RecyclerView menuRV;
    private FirebaseDatabase database;
    private DatabaseReference offerDatabase;
    private DatabaseReference menuDatabase;
    FirebaseStorage storage;
    ArrayList<Offer> offers;
    ArrayList<Menu> menus;
    FirebaseRecyclerOptions<Offer> offerFirebaseRecyclerOptions;
    FirebaseRecyclerOptions<Menu> menuFirebaseRecyclerOptions;
    FirebaseRecyclerAdapter<Offer,OfferViewHolder> offerViewHolderFirebaseRecyclerAdapter;
    FirebaseRecyclerAdapter<Menu,MenuViewHolder> menuViewHolderFirebaseRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);
        Intent intent=getIntent();
        restaurant= (Restaurant) intent.getSerializableExtra("res");
        imageView=findViewById(R.id.resImageIV);
        nameTV=findViewById(R.id.resNameTV);
        phoneTV=findViewById(R.id.resPhoneTV);
        offerLL=findViewById(R.id.orderLayout);
        offerRV=findViewById(R.id.offersIV);
        menuRV=findViewById(R.id.menuIV);
        menus=new ArrayList<>();
        offers=new ArrayList<>();
        Glide.with(this /* context */)
                .load(restaurant.getPhoto())
                .optionalCenterCrop()
                .placeholder(R.drawable.cover)
                .into(imageView);
        nameTV.setText(restaurant.getRestaurantName());
        phoneTV.setText(restaurant.getPhoneNumber());
        database=FirebaseDatabase.getInstance();
        offerDatabase=database.getReference().child("offers");
        menuDatabase=database.getReference().child("menu").child(restaurant.getPlaceID());



        offerDatabase.child(restaurant.getPlaceID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    offerLL.setVisibility(View.VISIBLE);
                } else {
                    offerLL.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        offerRV.setHasFixedSize(true);
        offerRV.setLayoutManager(new LinearLayoutManager(RestaurantActivity.this));
        offerFirebaseRecyclerOptions=new FirebaseRecyclerOptions.Builder<Offer>().
                setQuery(offerDatabase.child(restaurant.getPlaceID()),Offer.class).build();
        offerViewHolderFirebaseRecyclerAdapter =new FirebaseRecyclerAdapter<Offer,
                OfferViewHolder>(offerFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull OfferViewHolder offerViewHolder, int i, @NonNull final Offer offer) {
                offerViewHolder.offerTV.setText(offer.getOfferText());
                offerViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(RestaurantActivity.this);
                        builder1.setMessage(restaurant.getRestaurantName());
                        builder1.setCancelable(true);

                        builder1.setPositiveButton(
                                "Delete",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        deleteOffer(offer.getOfferId());
                                        dialog.cancel();
                                    }
                                });

                        builder1.setNegativeButton(
                                "Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        builder1.setNeutralButton(
                                "Edit Details",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent=new Intent(RestaurantActivity.this,MenuOfferActivity.class);
                                        intent.putExtra("off",offer);
                                        intent.putExtra("placeID",restaurant.getPlaceID());
                                        startActivity(intent);
                                        dialog.cancel();
                                    }
                                }
                        );
                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                    }
                });
            }

            @NonNull
            @Override
            public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new OfferViewHolder(LayoutInflater.from(RestaurantActivity.this)
                        .inflate(R.layout.offer_row,parent,false));
            }
        };
        offerRV.setAdapter(offerViewHolderFirebaseRecyclerAdapter);

        menuRV.setHasFixedSize(true);
        menuRV.setLayoutManager(new LinearLayoutManager(this));
        menuFirebaseRecyclerOptions=new FirebaseRecyclerOptions.Builder<Menu>().
                setQuery(menuDatabase,Menu.class).build();
        menuViewHolderFirebaseRecyclerAdapter =new FirebaseRecyclerAdapter<Menu, MenuViewHolder>(menuFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder menuViewHolder, int i, @NonNull final Menu menu) {
                Glide.with(RestaurantActivity.this /* context */)
                        .load(menu.getPhoto())
                        .optionalCenterCrop()
                        .placeholder(R.drawable.cover)
                        .into(menuViewHolder.menuIV);
                menuViewHolder.menuNameTV.setText(menu.getName());
                menuViewHolder.portionTV.setText(menu.getPortion());
                menuViewHolder.priceTV.setText(menu.getPrice());
                menuViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(RestaurantActivity.this);
                        builder1.setMessage(restaurant.getRestaurantName());
                        builder1.setCancelable(true);

                        builder1.setPositiveButton(
                                "Delete",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        deleteMenu(menu);
                                        dialog.cancel();
                                    }
                                });

                        builder1.setNegativeButton(
                                "Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        builder1.setNeutralButton(
                                "Edit Details",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent=new Intent(RestaurantActivity.this,MenuOfferActivity.class);
                                        intent.putExtra("menu",menu);
                                        intent.putExtra("placeID",restaurant.getPlaceID());
                                        startActivity(intent);
                                        dialog.cancel();
                                    }
                                }
                        );
                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                    }
                });
            }

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new MenuViewHolder(LayoutInflater.from(RestaurantActivity.this)
                        .inflate(R.layout.menu_row,parent,false));
            }
        };

        menuRV.setAdapter(menuViewHolderFirebaseRecyclerAdapter);

    }

    private void deleteMenu(final Menu menu) {
        storage=FirebaseStorage.getInstance();
        if(menu.getPhoto()!=null){
            StorageReference reference=storage.getReferenceFromUrl(menu.getPhoto());
            reference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    menuDatabase.child(menu.getMenuID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(RestaurantActivity.this,"Delete",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }else {
            menuDatabase.child(menu.getMenuID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(RestaurantActivity.this,"Delete",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteOffer(String offerId) {
        offerDatabase.child(restaurant.getPlaceID()).child(offerId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(RestaurantActivity.this,"Deleted",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void call(View view) {
        if(ContextCompat.checkSelfPermission(RestaurantActivity.this,
                Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(RestaurantActivity.this,
                    new String[] {Manifest.permission.CALL_PHONE},CALL_REQUEST_CODE);
        }else {
            String dial="tel:"+restaurant.getPhoneNumber();
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==CALL_REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                String dial="tel:"+restaurant.getPhoneNumber();
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            } else {
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        offerViewHolderFirebaseRecyclerAdapter.startListening();
        menuViewHolderFirebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        offerViewHolderFirebaseRecyclerAdapter.startListening();
        menuViewHolderFirebaseRecyclerAdapter.stopListening();
    }
}
