package com.example.product_catalog_service.search;

import com.example.product_catalog_service.entity.State;
import com.example.product_catalog_service.repo.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductIndexInitializer implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final ProductSearchIndex productSearchIndex;

    public ProductIndexInitializer(ProductRepository productRepository,
                                   ProductSearchIndex productSearchIndex) {
        this.productRepository = productRepository;
        this.productSearchIndex = productSearchIndex;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            productRepository.findAllActiveWithCategory(State.ACTIVE)
                    .forEach(productSearchIndex::index);
        } catch (RuntimeException e) {
            log.warn("Could not rebuild the product search index: {}", e.getMessage());
        }
    }
}
