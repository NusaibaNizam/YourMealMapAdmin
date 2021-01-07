package com.example.restaurantlocationtrackeradmin;

import java.io.Serializable;

public class Menu implements Serializable {
    String menuID;
    String photo;
    String name;
    String portion;
    String price;

    public Menu() {
    }

    public Menu(String menuID) {
        this.menuID = menuID;
    }

    public Menu(String menuID, String name, String portion, String price) {
        this.menuID = menuID;
        this.name = name;
        this.portion = portion;
        this.price = price;
    }

    public Menu(String menuID, String photo, String name, String portion, String price) {
        this.menuID = menuID;
        this.photo = photo;
        this.name = name;
        this.portion = portion;
        this.price = price;
    }

    public String getMenuID() {
        return menuID;
    }

    public void setMenuID(String menuID) {
        this.menuID = menuID;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPortion() {
        return portion;
    }

    public void setPortion(String portion) {
        this.portion = portion;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
