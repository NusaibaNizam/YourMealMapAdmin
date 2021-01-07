package com.example.restaurantlocationtrackeradmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference restaurantDatabase;
    DatabaseReference offerDatabase;
    DatabaseReference menuDatabase;
    FirebaseStorage storage;
    StorageReference storageReference;
    EditText searchET;
    RecyclerView recyclerView;
    FirebaseRecyclerOptions<Restaurant> restaurantFirebaseRecyclerOptions;
    FirebaseRecyclerAdapter<Restaurant,RestaurantViewHolder> restaurantViewHolderFirebaseRecyclerAdapter;
    private String searchText;
    Boolean search=false;
    ProgressDialog progressDialog;
    Spinner choiceSP;
    String choice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        restaurantDatabase = database.getReference().child("restaurants");
        menuDatabase = database.getReference().child("menu");
        offerDatabase = database.getReference().child("offers");
        storage = FirebaseStorage.getInstance();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchET = findViewById(R.id.searchET);
        progressDialog = new ProgressDialog(this);
        choiceSP = findViewById(R.id.choiceSP);
        ArrayAdapter<CharSequence> spAdapter = ArrayAdapter.createFromResource(this, R.array.choices, android.R.layout.simple_spinner_item);


        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        choiceSP.setAdapter(spAdapter);
        choiceSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choice = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                choice = parent.getItemAtPosition(0).toString();

            }
        });


        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchET.getText().toString().equals("")) {
                    Query firebaseSearchQuery = restaurantDatabase;
                    setAdapter(firebaseSearchQuery);
                } else if (choice.equals("By Name")) {
                    searchText = searchET.getText().toString().toLowerCase();
                    Query firebaseSearchQuery = restaurantDatabase.orderByChild("restaurantNameLower").startAt(searchText).endAt(searchText + "\uf8ff");
                    setAdapter(firebaseSearchQuery);
                } else {
                    searchText = searchET.getText().toString();
                    Query firebaseSearchQuery = restaurantDatabase.orderByChild("phoneNumber").startAt(searchText).endAt(searchText + "\uf8ff");
                    setAdapter(firebaseSearchQuery);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Query firebaseSearchQuery = restaurantDatabase;
        setAdapter(firebaseSearchQuery);




        /*restaurantFirebaseRecyclerOptions=new FirebaseRecyclerOptions.Builder<Restaurant>().
                setQuery(restaurantDatabase,Restaurant.class).build();
        restaurantViewHolderFirebaseRecyclerAdapter= new FirebaseRecyclerAdapter<Restaurant,
                RestaurantViewHolder>(restaurantFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull RestaurantViewHolder restaurantViewHolder, int i, @NonNull final Restaurant restaurant) {
                restaurantViewHolder.nameTV.setText(restaurant.getRestaurantName());
                restaurantViewHolder.phoneTV.setText(restaurant.getPhoneNumber());
                restaurantViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent =new Intent(MainActivity.this,RestaurantActivity.class);
                        intent.putExtra("res",restaurant);
                        startActivity(intent);
                    }
                });
                restaurantViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                        builder1.setMessage(restaurant.getRestaurantName());
                        builder1.setCancelable(true);

                        builder1.setPositiveButton(
                                "Delete",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        delete(restaurant.getPlaceID());
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
                                Intent intent=new Intent(MainActivity.this,AddRestaurantActivity.class);
                                intent.putExtra("res",restaurant);
                                intent.putExtra("main",true);
                                startActivity(intent);
                                dialog.cancel();
                            }
                        }
                        );
                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                        return true;
                    }
                });
            }

            @NonNull
            @Override
            public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RestaurantViewHolder(LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.restaurant_row,parent,false));
            }
        };
        recyclerView.setAdapter(restaurantViewHolderFirebaseRecyclerAdapter);
*/
    }
    private void delete(final String placeID) {
        progressDialog.setMessage("Deleting...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        restaurantDatabase.child(placeID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                offerDatabase.child(placeID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        menuDatabase.child(placeID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                    final com.example.restaurantlocationtrackeradmin.Menu menu = postSnapshot.getValue(com.example.restaurantlocationtrackeradmin.Menu.class);
                                    if(menu.getPhoto()!=null){
                                        storageReference=storage.getReferenceFromUrl(menu.getPhoto());
                                        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                menuDatabase.child(placeID).child(menu.getMenuID()).removeValue();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(MainActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }else {
                                        menuDatabase.child(placeID).child(menu.getMenuID()).removeValue();
                                    }
                                }
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
            Intent intent=new Intent(MainActivity.this,RegisterActivity.class);
            startActivity(intent);
            finish();
        }
        restaurantViewHolderFirebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        restaurantViewHolderFirebaseRecyclerAdapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.addRestaurantIT){
            Intent intent=new Intent(MainActivity.this,AddRestaurantActivity.class);
            startActivity(intent);
            finish();
        } else {
            mAuth.signOut();
            Intent intent=new Intent(MainActivity.this,RegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setAdapter(Query firebaseSearchQuery) {
        restaurantFirebaseRecyclerOptions=new FirebaseRecyclerOptions.Builder<Restaurant>().
                setQuery(firebaseSearchQuery,Restaurant.class).build();
        restaurantViewHolderFirebaseRecyclerAdapter= new FirebaseRecyclerAdapter<Restaurant,
                RestaurantViewHolder>(restaurantFirebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull RestaurantViewHolder restaurantViewHolder, int i, @NonNull final Restaurant restaurant) {
                restaurantViewHolder.nameTV.setText(restaurant.getRestaurantName());
                restaurantViewHolder.phoneTV.setText(restaurant.getPhoneNumber());
                restaurantViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent =new Intent(MainActivity.this,RestaurantActivity.class);
                        intent.putExtra("res",restaurant);
                        startActivity(intent);
                    }
                });
                restaurantViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                        builder1.setMessage(restaurant.getRestaurantName());
                        builder1.setCancelable(true);

                        builder1.setPositiveButton(
                                "Delete",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        delete(restaurant.getPlaceID());
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
                                        Intent intent=new Intent(MainActivity.this,AddRestaurantActivity.class);
                                        intent.putExtra("res",restaurant);
                                        intent.putExtra("main",true);
                                        startActivity(intent);
                                        dialog.cancel();
                                    }
                                }
                        );
                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                        return true;
                    }
                });
            }

            @NonNull
            @Override
            public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RestaurantViewHolder(LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.restaurant_row,parent,false));
            }
        };
        recyclerView.setAdapter(restaurantViewHolderFirebaseRecyclerAdapter);
        restaurantViewHolderFirebaseRecyclerAdapter.startListening();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        UIUtil.hideKeyboard(MainActivity.this);
        return super.dispatchTouchEvent(ev);
    }
}
