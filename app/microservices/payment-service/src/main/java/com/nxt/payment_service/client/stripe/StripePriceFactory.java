package com.nxt.payment_service.client.stripe;

import com.nxt.payment_service.exception.StripePriceCreationError;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.PriceCreateParams.ProductData;
import com.stripe.param.PriceCreateParams.Recurring;
import com.stripe.param.PriceCreateParams.Recurring.Interval;

public class StripePriceFactory {
    
    private static final String USD = "usd";
    private static final String GOLD_PLAN = "Gold Plan";

    public static PriceCreateParams buildPrice(Long amount) {
        return PriceCreateParams.builder()
            .setCurrency(USD)
            .setUnitAmount(amount)
            .setRecurring(buildMonthlyRecurring())
            .setProductData(buildGoldPlanProductData())
            .build();
    } 
   
    private static Recurring buildMonthlyRecurring() {
        return Recurring.builder().setInterval(Interval.MONTH).build();
    }

    private static ProductData buildGoldPlanProductData() {
        return ProductData.builder().setName(GOLD_PLAN).build();
    }
}
