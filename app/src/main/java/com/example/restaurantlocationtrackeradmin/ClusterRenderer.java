package com.example.restaurantlocationtrackeradmin;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;

public class ClusterRenderer extends DefaultClusterRenderer<MyItem> {
    private final IconGenerator mClusterIconGenerator ;
    Context context;
    ArrayList<MyItem> items;
    public ClusterRenderer(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
        super(context, map, clusterManager);
        mClusterIconGenerator = new IconGenerator(context);
        this.context=context;
        items=new ArrayList<>();
    }
    @Override
    protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
        if(item.isAccepted()){
            BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.right);
            items.add(item);
            markerOptions.icon(markerDescriptor);
        } else if(!item.isNone()) {
            BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.wrong);

            markerOptions.icon(markerDescriptor);
        } else {
            BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.defaultMarker();

            markerOptions.icon(markerDescriptor);
        }


    }

    @Override
    protected void onClusterItemRendered(MyItem clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);
    }

}
