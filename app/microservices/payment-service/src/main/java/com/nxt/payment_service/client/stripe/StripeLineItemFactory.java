package com.nxt.payment_service.client.stripe;

import com.stripe.model.Price;
import com.stripe.param.PaymentLinkCreateParams.LineItem;

public class StripeLineItemFactory {

    public static LineItem buildLineItem(Price price) {
        return LineItem.builder()
            .setPrice(price.getId())
            .setQuantity(1L)
            .build();
    }
}
