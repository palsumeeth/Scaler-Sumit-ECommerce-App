package com.example.product_catalog_service.repo;

import com.example.product_catalog_service.entity.Product;
import com.example.product_catalog_service.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, PagingAndSortingRepository<Product, Long> {

    @Query("""
        SELECT p FROM Product p
        JOIN FETCH p.category
        WHERE p.id = :id AND p.state = :state
    """)
    Optional<Product> findProductDetails(@Param("id") Long id, @Param("state") State state);

    @Query("""
        SELECT p FROM Product p
        JOIN FETCH p.category
        WHERE p.state = :state AND p.category.id = :categoryId
        """)
    List<Product> findActiveByCategory(@Param("categoryId") Long categoryId, @Param("state") State state);

    @Query("""
        SELECT p FROM Product p
        JOIN FETCH p.category
        WHERE p.state = :state AND p.id IN :ids
        """)
    List<Product> findActiveByIds(@Param("ids") List<Long> ids, @Param("state") State state);

    @Query("""
        SELECT p FROM Product p
        JOIN FETCH p.category
        WHERE p.state = :state
        """)
    List<Product> findAllActiveWithCategory(@Param("state") State state);
}