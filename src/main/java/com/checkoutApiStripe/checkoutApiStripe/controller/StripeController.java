package com.checkoutApiStripe.checkoutApiStripe.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.checkoutApiStripe.checkoutApiStripe.dto.StripeChargeDto;
import com.checkoutApiStripe.checkoutApiStripe.dto.StripeSubscriptionDto;
import com.checkoutApiStripe.checkoutApiStripe.dto.StripeSubscriptionResponse;
import com.checkoutApiStripe.checkoutApiStripe.dto.StripeTokenDto;
import com.checkoutApiStripe.checkoutApiStripe.service.StripeService;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping(value = "/stripe")
@AllArgsConstructor
public class StripeController {

    private final StripeService stripeService;


    @PostMapping("/createToken")
    @ResponseBody
    public StripeTokenDto createToken(@RequestBody StripeTokenDto model ) {

        return stripeService.createToken(model);
    }
    
    @PostMapping("/charge")
    @ResponseBody
    public StripeChargeDto charge(@RequestBody StripeChargeDto model ) {

        return stripeService.charge(model);
    }

    @PostMapping("/createSubscription")
    @ResponseBody
    public StripeSubscriptionResponse subscription(@RequestBody StripeSubscriptionDto entity) {
        
        return stripeService.createSubscription(entity);
    }
    

}