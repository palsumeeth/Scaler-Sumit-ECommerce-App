package com.nxt.payment_service.client.stripe;

import com.stripe.model.Price;
import com.stripe.param.PaymentLinkCreateParams.AfterCompletion;
import com.stripe.param.PaymentLinkCreateParams.AfterCompletion.Redirect;
import com.stripe.param.PaymentLinkCreateParams.AfterCompletion.Type;

public class StripeAfterCompletionFactory {

    private static final String REDIRECT_URL = "https://www.nxt.com/orders";

    public static AfterCompletion buildAfterCompletion(Price price) {
        return AfterCompletion.builder()
            .setType(Type.REDIRECT)
            .setRedirect(buildRedirect())
            .build();
    }

    private static Redirect buildRedirect() {
        return Redirect.builder().setUrl(REDIRECT_URL).build();
    }
}
