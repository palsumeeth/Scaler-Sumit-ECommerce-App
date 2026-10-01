package com.nxt.payment_service.client.razorpay;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nxt.payment_service.client.PaymentGatewayClient;
import com.nxt.payment_service.exception.RazorpayPaymentCreationException;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Component
public class NxtRazorpayClient implements PaymentGatewayClient {

    private final RazorpayClient client;
    private final RazorpayPaymentLinkRequestBuilder requestBuilder;

    @Autowired
    public NxtRazorpayClient(RazorpayClient client,
                             RazorpayPaymentLinkRequestBuilder requestBuilder) {

        this.client = client;
        this.requestBuilder = requestBuilder;
    }

    @Override
    public String getPaymentLink(Long amount, String orderId, String phoneNumber, String name) {
        JSONObject request = requestBuilder.buildPaymentLinkRequest(amount, orderId, phoneNumber, name);

        try {
            PaymentLink paymentLink = client.paymentLink.create(request);
            return paymentLink.get("short_url").toString();
        } catch(RazorpayException e) {
            throw new RazorpayPaymentCreationException("Unable to create payment link : ", e);
        }
    }
}
