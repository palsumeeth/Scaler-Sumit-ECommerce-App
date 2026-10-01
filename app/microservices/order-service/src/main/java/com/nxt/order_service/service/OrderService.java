package com.nxt.order_service.service;

import com.nxt.order_service.dto.CheckoutRequest;
import com.nxt.order_service.dto.OrderItemResponse;
import com.nxt.order_service.dto.OrderResponse;
import com.nxt.order_service.entity.CustomerOrder;
import com.nxt.order_service.entity.OrderItem;
import com.nxt.order_service.entity.OrderStatus;
import com.nxt.order_service.entity.PaymentMethod;
import com.nxt.order_service.repo.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.NoSuchElementException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(OrderRepository orderRepository, OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        PaymentMethod paymentMethod;
        try {
            paymentMethod = PaymentMethod.valueOf(request.getPaymentMethod().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("paymentMethod must be CARD, NET_BANKING, UPI, or WALLET");
        }

        CustomerOrder order = new CustomerOrder();
        order.setUserId(request.getUserId());
        order.setEmail(request.getEmail());
        order.setCustomerName(request.getCustomerName());
        order.setPhoneNumber(request.getPhoneNumber());
        order.setPaymentMethod(paymentMethod);
        order.setAddressLine(request.getAddressLine());
        order.setCity(request.getCity());
        order.setPostalCode(request.getPostalCode());
        order.setCountry(request.getCountry());
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(Instant.now());

        BigDecimal total = BigDecimal.ZERO;
        for (var itemReq : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(itemReq.getProductId());
            item.setProductName(itemReq.getProductName());
            item.setUnitPrice(itemReq.getUnitPrice());
            item.setQuantity(itemReq.getQuantity());
            order.getItems().add(item);
            total = total.add(itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }
        order.setTotalAmount(total);

        CustomerOrder saved = orderRepository.save(order);
        orderEventPublisher.orderPlaced(saved);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        return toResponse(load(orderId));
    }

    @Transactional(readOnly = true)
    public java.util.List<OrderResponse> history(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, String status) {
        OrderStatus next;
        try {
            next = OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown order status: " + status);
        }
        CustomerOrder order = load(orderId);
        order.setStatus(next);
        return toResponse(order);
    }

    @Transactional
    public void markPaid(Long orderId, String receiptId) {
        CustomerOrder order = load(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            return;
        }
        order.setStatus(OrderStatus.PAID);
        order.setReceiptId(receiptId);
        orderEventPublisher.orderConfirmed(order);
    }

    private CustomerOrder load(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
    }

    private OrderResponse toResponse(CustomerOrder order) {
        var items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProductId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getEmail(),
                order.getStatus().name(),
                order.getPaymentMethod().name(),
                order.getAddressLine(),
                order.getCity(),
                order.getPostalCode(),
                order.getCountry(),
                order.getTotalAmount(),
                order.getReceiptId(),
                order.getCreatedAt(),
                items
        );
    }
}
