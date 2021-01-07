package com.example.restaurantlocationtrackeradmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AddRestaurantActivity extends AppCompatActivity {
    private static final int AUTOCOMPLTE_REQUEST = 56676;
    EditText restaurantET;
    EditText phoneNumberET;
    TextView errorTV;
    Button resBT;
    Button menuBT;
    Button photoBT;
    ProgressBar progressBar;
    private PlacesClient placesClient;
    private Place place;
    private String placeID;
    private String restaurantName;
    private String phoneNumber;
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference restaurantDatabase;
    private int GALLERY_INTENT=358;
    private String image;
    FirebaseStorage storage;
    StorageReference imageStorage;
    Restaurant res;
    private boolean main;
    private View selectBT;
    private boolean map;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_restaurant);
        restaurantET=findViewById(R.id.restaurantNameET);
        main=false;
        map=false;
        phoneNumberET=findViewById(R.id.phoneET);
        errorTV=findViewById(R.id.errorTV);
        resBT=findViewById(R.id.resBT);
        selectBT=findViewById(R.id.selectBT);
        photoBT=findViewById(R.id.photoBT);
        menuBT=findViewById(R.id.menuBT);
        progressBar=findViewById(R.id.progressBar);
        restaurantET.setKeyListener(null);
        mAuth = FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        restaurantDatabase=database.getReference().child("restaurants");
        storage=FirebaseStorage.getInstance();
        imageStorage=storage.getReference();
        resBT.setEnabled(false);
        progressDialog=new ProgressDialog(this);
        selectBT.setVisibility(View.VISIBLE);
        Intent intent=getIntent();
        photoBT.setEnabled(true);
        main=intent.getBooleanExtra("main",false);
        map=intent.getBooleanExtra("map",false);
        if(main){
            res= (Restaurant) intent.getSerializableExtra("res");
            image=res.getPhoto();
            placeID=res.getPlaceID();
            phoneNumberET.setText(res.getPhoneNumber());
            restaurantName=res.getRestaurantName();
            restaurantET.setText(res.getRestaurantName());
            restaurantET.setEnabled(false);
            phoneNumber=res.getPhoneNumber();
            resBT.setText("Update Restaurant");
            photoBT.setEnabled(true);
            resBT.setEnabled(true);
            menuBT.setVisibility(View.VISIBLE);
            menuBT.setEnabled(true);
            selectBT.setVisibility(View.GONE);
        }
        else if(map){
            placeID=intent.getStringExtra("placeID");
            restaurantName=intent.getStringExtra("name");
            restaurantET.setText(restaurantName);

        }

        Places.initialize(this,getString(R.string.google_near_by_places_api));
        placesClient = Places.createClient(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_restaurant_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.listIT){
            Intent intent=new Intent(AddRestaurantActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            mAuth.signOut();
            Intent intent=new Intent(AddRestaurantActivity.this,RegisterActivity.class);
            startActivity(intent);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void selectRestaurant(View view) {
        try {

            List<Place.Field> fields = Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID,
                    com.google.android.libraries.places.api.model.Place.Field.NAME,com.google.android.libraries.places.api.model.Place.Field.LAT_LNG,
                    com.google.android.libraries.places.api.model.Place.Field.ADDRESS);

            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields)
                    .build(AddRestaurantActivity.this);
            startActivityForResult(intent, AUTOCOMPLTE_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLTE_REQUEST) {
            if (resultCode == RESULT_OK) {
                place = Autocomplete.getPlaceFromIntent(data);
                restaurantName =place.getName();
                placeID=place.getId();
                restaurantET.setText(restaurantName);
                photoBT.setEnabled(true);
            }else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                // TODO: Handle the error.
                Log.i("",status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        if(requestCode==GALLERY_INTENT && resultCode== RESULT_OK){
            progressDialog.setMessage("Uploading..");
            progressDialog.show();
            progressDialog.setCancelable(false);
            photoBT.setEnabled(false);
            final Uri uri=data.getData();
            Date date=new Date();
            Timestamp timestamp=new Timestamp(date.getTime());
            final StorageReference filePath=imageStorage.child("Profile Photos").child(placeID);
            filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUrl = uri;
                            //Do what you want with the url
                            image= String.valueOf(downloadUrl);
                            progressDialog.dismiss();
                            photoBT.setEnabled(true);
                            resBT.setEnabled(true);
                            Toast.makeText(AddRestaurantActivity.this,"Uploaded",Toast.LENGTH_SHORT).show();

                        }

                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    errorTV.setText(e.getLocalizedMessage());
                    errorTV.setVisibility(View.VISIBLE);
                    progressDialog.dismiss();
                }
            });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    public void addRestaurant(View view) {
        UIUtil.hideKeyboard(AddRestaurantActivity.this);
        if(TextUtils.isEmpty(restaurantET.getText())||TextUtils.isEmpty(phoneNumberET.getText())){
            errorTV.setText("Enter Fields");
            errorTV.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.VISIBLE);
            errorTV.setVisibility(View.GONE);
            phoneNumber=phoneNumberET.getText().toString();
            Restaurant restaurant=new Restaurant(placeID,restaurantET.getText().toString(),phoneNumber,image);
            restaurantET.setEnabled(false);
            phoneNumberET.setEnabled(false);
            resBT.setEnabled(false);
            restaurantDatabase.child(placeID).setValue(restaurant).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressBar.setVisibility(View.GONE);
                    resBT.setEnabled(true);
                    if(task.isSuccessful()){
                        resBT.setVisibility(View.GONE);
                        photoBT.setVisibility(View.GONE);
                        selectBT.setVisibility(View.GONE);
                        Intent intent =new Intent(AddRestaurantActivity.this,MenuOfferActivity.class);
                        intent.putExtra("placeID",placeID);
                        startActivity(intent);
                        finish();
                        Toast.makeText(AddRestaurantActivity.this,"Reataurant Added",Toast.LENGTH_SHORT).show();
                    } else {
                        errorTV.setText(task.getException().getLocalizedMessage());
                        errorTV.setVisibility(View.VISIBLE);
                    }
                }
            });

        }
    }

    public void addMenu(View view) {
        UIUtil.hideKeyboard(AddRestaurantActivity.this);
        Intent intent=new Intent(AddRestaurantActivity.this, MenuOfferActivity.class);
        intent.putExtra("placeID",placeID);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        UIUtil.hideKeyboard(AddRestaurantActivity.this);
        return super.dispatchTouchEvent(ev);
    }

    public void addPhoto(View view) {
        UIUtil.hideKeyboard(AddRestaurantActivity.this);
        selectImage();
    }

    private void selectImage() {
        Intent intent =new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,GALLERY_INTENT);
    }

    public void selectRestaurantMap(View view) {
        Intent intent= new Intent(AddRestaurantActivity.this,MapActivity.class);
        startActivity(intent);
    }
}
