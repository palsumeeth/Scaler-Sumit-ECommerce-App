package com.nxt.payment_service.strategy;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nxt.payment_service.client.stripe.NxtStripeClient;
import com.nxt.payment_service.client.PaymentGatewayClient;
import com.nxt.payment_service.client.razorpay.NxtRazorpayClient;

@Component
public class PaymentGatewayStrategy {

    private final NxtRazorpayClient razorpayClient;
    private final NxtStripeClient stripeClient;

    @Autowired
    public PaymentGatewayStrategy(NxtRazorpayClient razorpayClient, 
                                  NxtStripeClient stripeClient) {

        this.razorpayClient = razorpayClient;
        this.stripeClient = stripeClient;
    }

    public PaymentGatewayClient getOptimalPaymentGateway() {
        if (generateGatewayDecisionFactor() < 4)
            return razorpayClient;
        else
            return stripeClient;
    }

    private int generateGatewayDecisionFactor() {
        return ThreadLocalRandom.current().nextInt(0, 10);
    }
}
