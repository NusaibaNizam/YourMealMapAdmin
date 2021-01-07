package com.example.restaurantlocationtrackeradmin;

import android.Manifest;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, PlacesAdapter.ClickListener {
    private static final int GEO_FENCE_CODE = 10;
    private static final int GEOFENCE_PENDIND_CODE = 111;
    GoogleMap map;
    Address address;
    GoogleMapOptions options;
    public static final int PERMISSION_REQUEST = 1;
    ArrayList<MyItem> clusterItems = new ArrayList<>();
    ArrayList<MyItem> nearByItems = new ArrayList<>();
    ClusterManager<MyItem> clusterManager;
    private FusedLocationProviderClient client;
    private Location lastLocation;
    double lat, lng;
    LatLng latLng;
    Place place;
    private String origin;
    private String destination;
    private final int AUTOCOMPLTE_REQUEST = 2;
    SearchBox search;
    FloatingActionButton direction;
    DrawerLayout drawer;
    ImageView Drawer;
    TypeFilter typeFilter;
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/";
    NearByPlacesService service;
    String urlString;
    double nearByLat;
    double nearByLng;
    double radius;
    String type;
    double desLng;
    double desLat;
    View bottomSheetHeaderColor;
    String key;
    private BottomSheetBehavior mBottomSheetBehavior;
    List<NearByPlacesResponse.Result> places;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    String nextPage;
    int responceCode;
    String nearByString;
    boolean remove;
    boolean dir;
    AlertDialog.Builder alert;
    MyItem item;
    GeofencingClient geofencingClient;
    PendingIntent pendingIntent;
    ArrayList<Geofence> geofences = new ArrayList<>();
    private PlacesClient placesClient;
    private String TAG="lll";

    FirebaseDatabase database;
    DatabaseReference restaurantDatabase;
    private String pID;
    private MyItem item1;
    private Dialog dialog1;
    private View bottomSheet;
    private Restaurant restaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        geofencingClient = LocationServices.getGeofencingClient(MapActivity.this);
        pendingIntent = null;
        places = new ArrayList<>();
       /* drawer = findViewById(R.id.drawer);*/
        direction = findViewById(R.id.direction);
        direction.hide();
        Places.initialize(this,getString(R.string.google_near_by_places_api));
        placesClient = Places.createClient(this);
        database=FirebaseDatabase.getInstance();
        restaurantDatabase=database.getReference().child("restaurants");
        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetHeaderColor = findViewById(R.id.color);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        nearByLat = 181;
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedpreferences.edit();
        editor.commit();
        //Create retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(NearByPlacesService.class);
        key = getString(R.string.google_near_by_places_api);


        /*//navigation drawer
        Drawer = findViewById(R.id.drawer_logo);
        Drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });

*/
        search = findViewById(R.id.searchbox);
        search.setLogoText("Search Places");
        search.setLogoTextColor(R.color.colorSearchText);
        //search view listener
        search.setSearchListener(new SearchBox.SearchListener() {
            @Override
            public void onSearchOpened() {
                search.toggleSearch();
                try {
                    address = getAddress(lat, lng);

                    List<Place.Field> fields = Arrays.asList(Place.Field.ID,
                            Place.Field.NAME, Place.Field.LAT_LNG,
                            Place.Field.ADDRESS);

                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields)
                            .build(MapActivity.this);
                    startActivityForResult(intent, AUTOCOMPLTE_REQUEST);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onSearchCleared() {

            }

            @Override
            public void onSearchClosed() {
                getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                );
            }

            @Override
            public void onSearchTermChanged(String s) {

            }

            @Override
            public void onSearch(String s) {

            }

            @Override
            public void onResultClick(SearchResult searchResult) {

            }
        });

        //map fragment
        options = new GoogleMapOptions();
        options.zoomControlsEnabled(true).compassEnabled(true).mapType(GoogleMap.MAP_TYPE_TERRAIN);
        SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction().replace(R.id.map, mapFragment);
        ft.commit();
        mapFragment.getMapAsync(this);
        client = LocationServices.getFusedLocationProviderClient(this);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_DRAGGING
                        || mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetHeaderColor.setBackgroundColor(getResources().getColor(R.color.darkSky));
                } else {
                    bottomSheetHeaderColor.setBackgroundColor(getResources().getColor(R.color.bluish_gray));
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.map = googleMap;
        clusterManager = new ClusterManager<MyItem>(MapActivity.this, googleMap);
        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
        clusterManager.clearItems();

        //set my location enabled
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST);
            return;
        }

        //get last location
        client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    lastLocation = task.getResult();
                    lat = lastLocation.getLatitude();
                    lng = lastLocation.getLongitude();
                    latLng = new LatLng(lat, lng);

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                    address = getAddress(lat, lng);
                    if (address != null) {
                        item1=new MyItem(lat, lng, address.getLocality(), address.getAddressLine(0));

                    } else item1=new MyItem(lat, lng);
                    item1.setNone(true);
                    clusterItems.add(item1);
                    clusterManager.addItems(clusterItems);
                }
            }
        });
        map.setMyLocationEnabled(true);
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                nearByLat = lat;
                nearByLng = lng;
                if (address != null) {
                    nearByString = address.getLocality();
                }
                dir = false;
                direction.hide();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 12));
                return false;
            }
        });

        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(MyItem myItem) {
                if (nearByItems.contains(myItem)) {
                    item = myItem;
                    getPhotos(myItem.getPlaceId(), null);

                }
                return false;
            }
        });

        //Disable Map Toolbar:
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Address geoAddress=getAddress(latLng.latitude,latLng.longitude);
                String address="Selected area";
                if(geoAddress!=null){
                    address=geoAddress.getAddressLine(0);
                }
                Geofence geofence = new Geofence.Builder().setRequestId(address)
                        .setCircularRegion(latLng.latitude, latLng.longitude, 200)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)
                        .setExpirationDuration(6 * 60 * 60 * 1000)
                        .build();
                geofences.add(geofence);
                registerGeofence();
                Toast.makeText(MapActivity.this, "Added to Geofenced Area", Toast.LENGTH_SHORT).show();
            }
        });

        googleMap.getUiSettings().setMapToolbarEnabled(false);
        this.map = googleMap;
    }

    private void registerGeofence() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},GEO_FENCE_CODE);
            return;
        }
        geofencingClient.addGeofences(getGeofencingRequest(),getPendingIntent()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MapActivity.this, "Geofence Added", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MapActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("error", task.getException().getMessage());
                }
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==PERMISSION_REQUEST&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
            onMapReady(map);
            //registerGeofence();
        }else {
            Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
        }
    }
    private GeofencingRequest getGeofencingRequest(){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofences);
        GeofencingRequest request=builder.build();
        return request;
    }
    private PendingIntent getPendingIntent(){
        if(pendingIntent != null){
            return pendingIntent;
        }else{
            Intent intent = new Intent(MapActivity.this,GeofencePendingIntentService.class);
            pendingIntent = PendingIntent.getService(this,GEOFENCE_PENDIND_CODE,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            return pendingIntent;
        }
    }

    public void getNearByPlaces(){
        Call<NearByPlacesResponse> nearByPlacesResponseCall;
        nearByPlacesResponseCall=service.getNearByPlaces(urlString);
        nearByPlacesResponseCall.enqueue(new Callback<NearByPlacesResponse>() {
            @Override
            public void onResponse(Call<NearByPlacesResponse> call, Response<NearByPlacesResponse> response) {
                editor.putInt("code",response.code());
                editor.apply();
                editor.commit();
                if(response.code()==200){
                    NearByPlacesResponse placesResponse;
                    placesResponse=response.body();
                    editor.putString("token",placesResponse.getNextPageToken());
                    editor.apply();
                    editor.commit();
                    places.addAll(placesResponse.getResults());
                    try {
                        if(places.size()!=0)
                            Toast.makeText(MapActivity.this,places.size()+" places found!",Toast.LENGTH_SHORT).show();
                    }catch (NullPointerException e){}

                    Log.e("Url",BASE_URL+urlString);
                    getNextPage();
             }else {
                    Toast.makeText(MapActivity.this,response.message(),Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<NearByPlacesResponse> call, Throwable t) {
                Toast.makeText(MapActivity.this,t.getMessage(),Toast.LENGTH_SHORT).show();
                editor.putInt("code",0);
                editor.apply();
                editor.commit();
            }
        });

    }

    //get Address
    public Address getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            return addresses.get(0);
        } catch (Exception e){}
        return null;
    }

    //get searched place
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLTE_REQUEST) {
            if (resultCode == RESULT_OK) {
                place = Autocomplete.getPlaceFromIntent(data);
                desLat = place.getLatLng().latitude;
                desLng = place.getLatLng().longitude;
                destination = "" + desLat + "," + desLng;
                nearByLng=desLng;
                nearByLat=desLat;
                nearByString=place.getAddress().toString();
                origin = "" + lastLocation.getLatitude() + "," + lastLocation.getLongitude();
                map.clear();
                clusterItems.clear();
                clusterManager.clearItems();
                if(address!=null) {
                    item1=new MyItem(lat, lng, address.getLocality(), address.getAdminArea());
                }
                else item1=new MyItem(lat,lng);
                item1.setNone(true);
                clusterItems.add(item1);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(desLat, desLng), 12));
                MyItem desItem=new MyItem(desLat, desLng, place.getName().toString(), place.getAddress().toString());
                desItem.setNone(true);
                clusterItems.add(desItem);
                clusterManager.addItems(clusterItems);
                clusterManager.cluster();
            }else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                // TODO: Handle the error.
                Log.i("",status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //go to direction activity and get direction
    public void direction(View view) {
        if(remove){
            clusterManager.clearItems();
            nearByItems.clear();
            clusterManager.addItems(clusterItems);
            clusterManager.cluster();
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(nearByLat,nearByLng),12));
            remove=false;

            direction.setImageResource(R.drawable.ic_directions_black_24dp);
            direction.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            if(!dir){
                direction.hide();
            }else direction.show();
        }
    }
    public void nearBy(View view) {
        clusterManager.clearItems();
        nearByItems.clear();
        clusterManager.addItems(clusterItems);
        clusterManager.cluster();
        if(nearByLat==181){
            nearByLat=lat;
            nearByLng=lng;
            if(address!=null) {
                nearByString = address.getAdminArea();
            }
        }
        places.clear();
        Toast.makeText(MapActivity.this,"Restaurants",Toast.LENGTH_SHORT).show();
        type="restaurant";
        radius=1000;
        urlString=String.format("json?location=%f,%f&radius=%f&type=%s&key=%s",nearByLat,nearByLng,radius,type,key);
        getNearByPlaces();

    }

    public void getNextPage() {
        responceCode = sharedpreferences.getInt("code", 0);
        if (responceCode == 200) {
            nextPage = sharedpreferences.getString("token", null);
            if (nextPage != null) {
                urlString = String.format("json?pagetoken=%s&key=%s", nextPage, key);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getNearByPlaces();
            }else {
                showPlaces();
            }
        }
    }
    public void showPlaces(){
        if(places.size()!=0) {
            dialog1 = new Dialog(MapActivity.this);
            dialog1.setCancelable(false);
            dialog1.setCanceledOnTouchOutside(false);
            View view = LayoutInflater.from(MapActivity.this).inflate(R.layout.places, null);
            dialog1.setContentView(view);
            TextView title = view.findViewById(R.id.typeTV);
            title.setText("Nearby " + type + " list");
            RecyclerView recyclerView = dialog1.findViewById(R.id.placeRV);
            PlacesAdapter adapter = new PlacesAdapter(MapActivity.this, places, this);
            RecyclerView.LayoutManager manager = new LinearLayoutManager(MapActivity.this, RecyclerView.VERTICAL, false);
            recyclerView.setLayoutManager(manager);
            recyclerView.setAdapter(adapter);
            adapter.Update(places);
            TextView cancel=dialog1.findViewById(R.id.cancel);
            TextView showAll=dialog1.findViewById(R.id.showAll);
            nearByItems.clear();
            final LatLngBounds.Builder builder=new LatLngBounds.Builder();
            for (final NearByPlacesResponse.Result place:places){
                builder.include(new LatLng(place.getGeometry().getLocation().getLat(),
                        place.getGeometry().getLocation().getLng()));
                final MyItem item=new MyItem(place.getGeometry().getLocation().getLat(),
                        place.getGeometry().getLocation().getLng(),place.getName());
                item.setPlaceId(place.getPlaceId());
                restaurantDatabase.child(place.getPlaceId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue()!=null){
                            item.setAccepted(true);
                            if(place.getRating()!=null)
                                item.setRating(place.getRating());
                            else item.setRating(0);
                        }
                        nearByItems.add(item);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog1.dismiss();
                    Toast.makeText(MapActivity.this,"No places selected",Toast.LENGTH_SHORT).show();
                }
            });
            showAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),30));
                    clusterManager.addItems(nearByItems);
                    clusterManager.setRenderer(new ClusterRenderer(MapActivity.this, map,
                            clusterManager));
                    clusterManager.cluster();
                    direction.setImageResource(R.drawable.ic_remove_black_24dp);
                    direction.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    direction.show();
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    remove=true;
                    dialog1.dismiss();
                }
            });
            dialog1.show();
        }else {
            Toast.makeText(MapActivity.this,"No places found",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(NearByPlacesResponse.Result place) {
        getPhotos(place.getPlaceId(),place);
    }
    private void getPhotos(final String placeId, final NearByPlacesResponse.Result place) {

        restaurantDatabase.child(placeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                restaurant= dataSnapshot.getValue(Restaurant.class);
                if(restaurant!=null){
                    getImage(null,place);

                }else {

                    List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);
                    FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeId, fields);
                    placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
                        Place place1 = response.getPlace();

                        // Get the photo metadata.
                        try {

                            PhotoMetadata photoMetadata = place1.getPhotoMetadatas().get(0);

                            // Get the attribution text.
                            String attributions = photoMetadata.getAttributions();

                            // Create a FetchPhotoRequest.
                            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                                    .build();
                            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                                getImage(bitmap,place);
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    if (exception instanceof ApiException) {
                                        ApiException apiException = (ApiException) exception;
                                        int statusCode = apiException.getStatusCode();
                                        // Handle error with given status code.
                                        getImage(null,place);
                                        Log.e(TAG, "Place not found: " + exception.getMessage());
                                    }
                                }
                            });
                        }catch (NullPointerException e){
                            getImage(null,place);
                        }

                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getImage(Bitmap bitmap, final NearByPlacesResponse.Result plac) {
        alert=new AlertDialog.Builder(MapActivity.this);
        if(bitmap!=null) {
            final ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(bitmap);
            alert.setView(imageView);
        } else if(restaurant!=null){
            final ImageView imageView = new ImageView(this);
            Glide.with(this /* context */)
                    .load(restaurant.getPhoto())
                    .optionalCenterCrop()
                    .apply(new RequestOptions().override(500, 300))
                    .placeholder(R.drawable.cover)
                    .into(imageView);
            alert.setView(imageView);
        }
        if(plac==null) {
            pID=item.getPlaceId();
            alert.setTitle(item.getTitle());
        }else {
            pID=plac.getPlaceId();
            alert.setTitle(plac.getName());
        }
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel",null);

        if(restaurant!=null){


            AlertDialog dialog = alert.create();
            dialog.show();
        } else {
            alert.setPositiveButton("Select Place", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(plac==null) {
                        Intent intent=new Intent(MapActivity.this,AddRestaurantActivity.class);
                        intent.putExtra("placeID",item.getPlaceId());
                        intent.putExtra("name",item.getTitle());
                        intent.putExtra("map",true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }else {
                        Intent intent=new Intent(MapActivity.this,AddRestaurantActivity.class);
                        intent.putExtra("placeID",plac.getPlaceId());
                        intent.putExtra("name",plac.getName());
                        intent.putExtra("map",true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
            });
            AlertDialog dialog = alert.create();
            dialog.show();
        }
    }
}