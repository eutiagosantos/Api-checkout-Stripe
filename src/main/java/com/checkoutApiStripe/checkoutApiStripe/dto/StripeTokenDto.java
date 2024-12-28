package com.checkoutApiStripe.checkoutApiStripe.dto;

import lombok.Data;

@Data
public class StripeTokenDto {
    
    public String cardnumber;
    public String expMonth;
    public String expYear;
    public String cvc;
    public String token;
    public String username;
    public boolean success;
}