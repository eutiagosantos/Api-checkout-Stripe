package com.checkoutApiStripe.checkoutApiStripe.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.checkoutApiStripe.checkoutApiStripe.dto.StripeChargeDto;
import com.checkoutApiStripe.checkoutApiStripe.dto.StripeTokenDto;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Token;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StripeService {
    
    @Value("$api.stripe.key")
    private String stripeApiKey;


    @PostConstruct
    public void init(){
        Stripe.apiKey = stripeApiKey;
    }

    public StripeTokenDto createToken(StripeTokenDto model){

        try{
            Map<String, Object> card = new HashMap<>();
            card.put("cardNumber", model.getCardnumber());
            card.put("expMonth",Integer.parseInt(model.getExpMonth()));
            card.put("expYear", Integer.parseInt(model.getExpYear()));
            card.put("cvc", model.getCvc());
            
            Map<String, Object> params = new HashMap<>();
            params.put("card", card);

            Token token = Token.create(params);
            if(token != null && token.getId() != null){
                model.setSuccess(true);
                model.setToken(token.toString());
            }

            return model;
        }catch(StripeException e){
            log.error("Stripe service (creditCardToken)", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public StripeChargeDto charge(StripeChargeDto request){

        try{
            request.setSuccess(false);
            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount", request.getAmount());
            chargeParams.put("currency", "USD");
            chargeParams.put("description", "Payment for id: " + request.getAdditionalInfo().getOrDefault("ID_TAG", ""));
            chargeParams.put("source", request.getStripeToken());

            Map<String, Object> metaData = new HashMap<>();
            metaData.put("id", request.getChargeId());
            metaData.putAll(request.getAdditionalInfo());
            chargeParams.put("metadata", metaData);
            
            Charge charge = Charge.create(chargeParams);
            request.setMessage(charge.getOutcome().getSellerMessage());
            
            if(charge.getPaid()){
                request.setChargeId(charge.getId());
                request.setSuccess(true);
            }
            return request;
        }catch(StripeException e){
            log.error("StripeService (charge)", e);
            throw new RuntimeException(e.getMessage());
        }
    }

}