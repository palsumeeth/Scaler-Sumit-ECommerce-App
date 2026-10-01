package com.example.product_catalog_service.repo;

import com.example.product_catalog_service.entity.Category;
import com.example.product_catalog_service.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByState(State state);
}
