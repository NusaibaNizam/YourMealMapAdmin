package com.example.restaurantlocationtrackeradmin;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by BITM Trainer 601 on 1/17/2018.
 */

public interface NearByPlacesService {

    @GET
    Call<NearByPlacesResponse>getNearByPlaces(@Url String urlString);
}
