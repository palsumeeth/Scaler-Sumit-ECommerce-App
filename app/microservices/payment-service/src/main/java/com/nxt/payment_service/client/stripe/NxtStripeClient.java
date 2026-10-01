package com.nxt.payment_service.client.stripe;

import com.nxt.payment_service.exception.StripePriceCreationError;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nxt.payment_service.client.PaymentGatewayClient;
import com.nxt.payment_service.exception.StripePaymentCreationError;
import com.stripe.model.PaymentLink;
import com.stripe.model.Price;
import com.stripe.param.PaymentLinkCreateParams;

@Component
public class NxtStripeClient implements PaymentGatewayClient {

    private final StripeClient stripeClient;

    @Autowired
    public NxtStripeClient(StripeClient stripeClient) {
        this.stripeClient = stripeClient;
    }

    @Override
    public String getPaymentLink(Long amount, String orderId, String phoneNumber, String name) {
        Price price = null;
        try {
            price = stripeClient.prices().create(StripePriceFactory.buildPrice(amount));
        } catch (StripeException e) {
            throw new StripePriceCreationError("Error while building `Price` object for payment link" +
                    " request ", e);
        }

        PaymentLinkCreateParams params = PaymentLinkCreateParams.builder()
            .addLineItem(StripeLineItemFactory.buildLineItem(price))
            .setAfterCompletion(StripeAfterCompletionFactory.buildAfterCompletion(price))
            .build();
    
        try {
            PaymentLink paymentLink = stripeClient.paymentLinks().create(params);
            return paymentLink.getUrl();
        } catch (Exception e) {
            throw new StripePaymentCreationError("Unable to create payment link : ", e);
        }
    }
}
