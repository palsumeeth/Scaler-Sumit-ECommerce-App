package com.nxt.payment_service.client.razorpay;

import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Log4j2
@Component
public class RazorpayPaymentLinkRequestBuilder {
    public static final String INR = "INR";

    private Long amount;
    private String referenceId;
    private String description;
    private String phoneNumber;
    private String name;

    public RazorpayPaymentLinkRequestBuilder setAmount(Long amount) {
        this.amount = amount;
        return this;
    }

    public RazorpayPaymentLinkRequestBuilder setReferenceId(String referenceId) {
        this.referenceId = referenceId;
        return this;
    }

    public RazorpayPaymentLinkRequestBuilder setCustomerDetails(String phoneNumber, String name) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        return this;
    }

    public JSONObject build() {
        JSONObject paymentLinkRequest = new JSONObject();

        paymentLinkRequest.put("amount", amount);
        paymentLinkRequest.put("currency", INR);
        paymentLinkRequest.put("accept_partial", true);
        paymentLinkRequest.put("first_min_partial_amount", 100);
        long expireBy = Instant.now().getEpochSecond() + (17 * 60);
        paymentLinkRequest.put("expire_by", expireBy);
        paymentLinkRequest.put("reference_id", referenceId);

        if (description != null) {
            paymentLinkRequest.put("description", description);
        }

        JSONObject customer = new JSONObject();
        customer.put("name", name);
        customer.put("contact", phoneNumber);
        customer.put("email", "dummy@example.com"); // use valid dummy

        paymentLinkRequest.put("customer", customer);

        JSONObject notify = new JSONObject();
        notify.put("sms", true);
        notify.put("email", true);
        paymentLinkRequest.put("notify", notify);
        paymentLinkRequest.put("reminder_enable", true);

        JSONObject notes = new JSONObject();
        notes.put("policy_name", "N/A");
        paymentLinkRequest.put("notes", notes);

        paymentLinkRequest.put("callback_url", "https://example-callback-url.com");
        paymentLinkRequest.put("callback_method", "get");

        log.debug("Razorpay Request Payload: {}", paymentLinkRequest.toString(2));
        return paymentLinkRequest;
    }

    public JSONObject buildPaymentLinkRequest(Long amount, String orderId, String phoneNumber, String name) {
        return this.setAmount(amount).setCustomerDetails(phoneNumber, name).setReferenceId(orderId).build();
    }
}