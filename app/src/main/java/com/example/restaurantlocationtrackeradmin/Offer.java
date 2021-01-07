package com.example.restaurantlocationtrackeradmin;

import java.io.Serializable;

public class Offer implements Serializable {
    String offerId;
    String offerText;

    public Offer() {
    }

    public Offer(String offerId, String offerText) {
        this.offerId = offerId;
        this.offerText = offerText;
    }

    public Offer(String offerId) {
        this.offerId = offerId;
    }

    public String getOfferText() {
        return offerText;
    }

    public void setOfferText(String offerText) {
        this.offerText = offerText;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }
}
