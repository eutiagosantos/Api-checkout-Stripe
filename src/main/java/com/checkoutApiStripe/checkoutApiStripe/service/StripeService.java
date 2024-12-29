package com.checkoutApiStripe.checkoutApiStripe.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.checkoutApiStripe.checkoutApiStripe.dto.StripeChargeDto;
import com.checkoutApiStripe.checkoutApiStripe.dto.StripeSubscriptionDto;
import com.checkoutApiStripe.checkoutApiStripe.dto.StripeSubscriptionResponse;
import com.checkoutApiStripe.checkoutApiStripe.dto.StripeTokenDto;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
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

    public StripeSubscriptionResponse createSubscription(StripeSubscriptionDto subscriptionDto){

        PaymentMethod paymentMethod = createPaymentMethod(subscriptionDto);
        Customer customer = createCustomer(paymentMethod, subscriptionDto);
        paymentMethod = attachCustomerToPaymentMethod(customer, paymentMethod);
        Subscription subscription = createSubscription(paymentMethod, subscriptionDto, customer);

        return createSubs(subscriptionDto,paymentMethod, customer, subscription);
                
    }
        
    private StripeSubscriptionResponse createSubs(StripeSubscriptionDto subscriptionDto,PaymentMethod paymentMethod, Customer customer,Subscription subscription) {
        
        return StripeSubscriptionResponse.builder()
                .username(subscriptionDto.getUsername())
                .stripeCustomerId(customer.getId())
                .stripeSubscriptionId(subscription.getId())
                .stripePaymentMethodId(paymentMethod.getId())
                .build();
    }
        
    private PaymentMethod createPaymentMethod(StripeSubscriptionDto subscription){

        try{
            Map<String, Object> card = new HashMap<>();
            card.put("cardNumber", subscription.getCardnumber());
            card.put("expMonth",Integer.parseInt(subscription.getExpMonth()));
            card.put("expYear", Integer.parseInt(subscription.getExpYear()));
            card.put("cvc", subscription.getCvc());

            Map<String, Object> params = new HashMap<>();
            params.put("type", "card");
            params.put("card", card);

            return PaymentMethod.create(params);
        } catch(StripeException e){
            log.error("StripeService (paymentMethod)", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private Customer createCustomer(PaymentMethod paymentMethod, StripeSubscriptionDto subscription){
    
        try {
            Map<String, Object> customerMap = new HashMap<>();
            customerMap.put("name", subscription.getUsername());
            customerMap.put("email", subscription.getEmail());
            customerMap.put("payment_method", paymentMethod.getId());
            
            return Customer.create(customerMap);

        } catch (StripeException e) {
            log.error("StripeService (createCustomer)", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private PaymentMethod attachCustomerToPaymentMethod(Customer customer, PaymentMethod paymentMethod){

        try{
            paymentMethod = PaymentMethod.retrieve(paymentMethod.getId());

            Map<String, Object> params = new HashMap<>();
            params.put("customer", customer.getId());
            paymentMethod = paymentMethod.attach(params);
            return paymentMethod;
        }catch(StripeException e ){
            log.error("StripeService (attachCustomerToPaymentMethod)", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private Subscription createSubscription(PaymentMethod paymentMethod, StripeSubscriptionDto subscriptionDto, Customer customer){

        try{
            List<Object> items = new ArrayList<>();
            Map<String, Object> item1 = new HashMap<>();
            item1.put(
                "price",
                subscriptionDto.getPriceId()
            );
            item1.put("quantity", subscriptionDto.getNumberOfLicense());
            items.add(item1);

            Map<String, Object> params = new HashMap<>();
            params.put("customer", customer.getId());
            params.put("default-payment-method", paymentMethod.getId());
            params.put("items", items);
            return Subscription.create(params);
        }catch(StripeException e){
            log.error("StripeService (createSubscription)", e);
            throw new RuntimeException(e.getMessage());
        }   
    }

}