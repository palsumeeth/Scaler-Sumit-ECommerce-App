package com.nxt.order_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CheckoutRequest {
    @NotNull
    private Long userId;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String customerName;
    private String phoneNumber;
    @NotBlank
    private String paymentMethod;
    @NotBlank
    private String addressLine;
    @NotBlank
    private String city;
    @NotBlank
    private String postalCode;
    @NotBlank
    private String country;
    @NotEmpty
    @Valid
    private List<CheckoutItemRequest> items;
}
