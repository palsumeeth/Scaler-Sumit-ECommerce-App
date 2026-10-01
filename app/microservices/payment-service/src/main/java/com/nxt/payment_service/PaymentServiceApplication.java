package com.nxt.payment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentServiceApplication {

	public static void main(String[] args) {
//		System.out.println(">>>Stripe Key = " + System.getenv("NXT_STRIPE_API_KEY"));
//		System.out.println("Stripe Key = " + System.getenv("NXT_RAZORPAY_KEY"));
//		System.out.println("Stripe Key = " + System.getenv("NXT_RAZORPAY_SECRET"));

		SpringApplication.run(PaymentServiceApplication.class, args);
	}

}