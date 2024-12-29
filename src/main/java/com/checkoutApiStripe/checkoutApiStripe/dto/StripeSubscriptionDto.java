package com.checkoutApiStripe.checkoutApiStripe.dto;

import lombok.Data;

@Data
public class StripeSubscriptionDto {

    public String cardnumber;
    public String expMonth;
    public String expYear;
    public String cvc;
    public String email;
    public String priceId;
    public long numberOfLicense;
    public String username;
    public boolean success;
}