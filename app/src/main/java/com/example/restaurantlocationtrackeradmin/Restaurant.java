package com.example.restaurantlocationtrackeradmin;

import java.io.Serializable;

public class Restaurant implements Serializable {
    String placeID;
    String restaurantName;
    String restaurantNameLower;
    String phoneNumber;
    String photo;


    public Restaurant(String placeID, String restaurantName, String phoneNumber, String photo) {
        this.placeID = placeID;
        this.restaurantName = restaurantName;
        this.restaurantNameLower = restaurantName.toLowerCase();
        this.phoneNumber = phoneNumber;
        this.photo = photo;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Restaurant() {

    }

    public Restaurant(String placeID, String restaurantName, String phoneNumber) {
        this.placeID = placeID;
        this.restaurantName = restaurantName;
        this.restaurantNameLower = restaurantName.toLowerCase();
        this.phoneNumber = phoneNumber;
    }

    public Restaurant(String restaurantName, String phoneNumber) {
        this.restaurantName = restaurantName;
        this.restaurantNameLower = restaurantName.toLowerCase();
        this.phoneNumber = phoneNumber;
    }

    public Restaurant(String placeID) {
        this.placeID = placeID;
    }

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
