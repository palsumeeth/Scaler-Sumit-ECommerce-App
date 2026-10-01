package com.nxt.cart_service.service;

import com.nxt.cart_service.dto.AddCartItemRequest;
import com.nxt.cart_service.dto.CartLineResponse;
import com.nxt.cart_service.dto.CartResponse;
import com.nxt.cart_service.entity.Cart;
import com.nxt.cart_service.entity.CartItem;
import com.nxt.cart_service.repo.CartRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartCache cartCache;
    private final CartEventPublisher cartEventPublisher;

    public CartService(CartRepository cartRepository,
                       CartCache cartCache,
                       CartEventPublisher cartEventPublisher) {
        this.cartRepository = cartRepository;
        this.cartCache = cartCache;
        this.cartEventPublisher = cartEventPublisher;
    }

    public CartResponse addItem(AddCartItemRequest request) {
        Cart cart = cartRepository.findByUserId(request.getUserId()).orElseGet(() -> {
            Cart created = new Cart();
            created.setUserId(request.getUserId());
            return created;
        });

        CartItem item = cart.getItems().stream()
                .filter(existing -> existing.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElseGet(() -> {
                    CartItem created = new CartItem();
                    created.setId(nextItemId(cart));
                    created.setProductId(request.getProductId());
                    cart.getItems().add(created);
                    return created;
                });

        item.setProductName(request.getProductName());
        item.setUnitPrice(request.getUnitPrice());
        item.setQuantity(item.getQuantity() + request.getQuantity());
        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            item.setAttributes(new LinkedHashMap<>(request.getAttributes()));
        }

        Cart saved = cartRepository.save(cart);
        cartEventPublisher.cartUpdated(saved);
        CartResponse response = toResponse(saved);
        cartCache.put(response);
        return response;
    }

    public CartResponse getCart(Long userId) {
        return cartCache.get(userId).orElseGet(() -> loadAndCache(userId));
    }

    public CartResponse updateQuantity(Long itemId, int quantity) {
        Cart cart = findCartByItemId(itemId);
        CartItem item = findItem(cart, itemId);
        item.setQuantity(quantity);
        return persist(cart);
    }

    public CartResponse removeItem(Long itemId) {
        Cart cart = findCartByItemId(itemId);
        cart.getItems().removeIf(item -> itemId.equals(item.getId()));
        return persist(cart);
    }

    private CartResponse loadAndCache(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Cart not found for user " + userId));
        CartResponse response = toResponse(cart);
        cartCache.put(response);
        return response;
    }

    private CartResponse persist(Cart cart) {
        Cart saved = cartRepository.save(cart);
        cartEventPublisher.cartUpdated(saved);
        CartResponse response = toResponse(saved);
        cartCache.put(response);
        return response;
    }

    private Cart findCartByItemId(Long itemId) {
        return cartRepository.findByItemId(itemId)
                .orElseThrow(() -> new NoSuchElementException("Cart item not found: " + itemId));
    }

    private CartItem findItem(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(item -> itemId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Cart item not found: " + itemId));
    }

    private long nextItemId(Cart cart) {
        return cart.getItems().stream()
                .map(CartItem::getId)
                .filter(id -> id != null)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1;
    }

    private CartResponse toResponse(Cart cart) {
        var lines = cart.getItems().stream()
                .map(item -> {
                    BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    Map<String, Object> attributes = item.getAttributes() == null
                            ? Map.of()
                            : item.getAttributes();
                    return new CartLineResponse(
                            item.getId(),
                            item.getProductId(),
                            item.getProductName(),
                            item.getUnitPrice(),
                            item.getQuantity(),
                            lineTotal,
                            attributes
                    );
                })
                .toList();
        BigDecimal total = lines.stream()
                .map(CartLineResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getId(), cart.getUserId(), lines, total);
    }
}
