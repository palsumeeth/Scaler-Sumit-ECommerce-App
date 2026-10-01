package com.nxt.cart_service.repo;

import com.nxt.cart_service.entity.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {
    Optional<Cart> findByUserId(Long userId);

    @Query("{ 'items.id': ?0 }")
    Optional<Cart> findByItemId(Long itemId);
}
