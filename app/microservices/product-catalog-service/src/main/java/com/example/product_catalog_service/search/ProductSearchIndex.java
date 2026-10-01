package com.example.product_catalog_service.search;

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.example.product_catalog_service.entity.Product;
import com.example.product_catalog_service.exception.SearchUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ProductSearchIndex {

    private final ProductSearchRepository productSearchRepository;
    private final ElasticsearchOperations operations;

    public ProductSearchIndex(ProductSearchRepository productSearchRepository,
                              ElasticsearchOperations operations) {
        this.productSearchRepository = productSearchRepository;
        this.operations = operations;
    }

    public void index(Product product) {
        ProductDocument document = new ProductDocument();
        document.setId(product.getId());
        document.setName(product.getName());
        document.setDescription(product.getDescription());
        document.setCategoryName(product.getCategory() == null ? null : product.getCategory().getName());
        document.setState(product.getState().name());
        try {
            productSearchRepository.save(document);
        } catch (RuntimeException e) {
            log.warn("Could not index product {}: {}", product.getId(), e.getMessage());
        }
    }

    public List<Long> searchIds(String keyword) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .filter(f -> f.term(t -> t.field("state").value("ACTIVE")))
                        .must(m -> m.multiMatch(mm -> mm
                                .query(keyword)
                                .fields("name^3", "description", "categoryName^2")
                                .fuzziness("AUTO")
                                .type(TextQueryType.BestFields)))))
                .build();
        try {
            return operations.search(query, ProductDocument.class).stream()
                    .map(hit -> hit.getContent().getId())
                    .toList();
        } catch (RuntimeException e) {
            log.warn("Elasticsearch search failed: {}", e.getMessage());
            throw new SearchUnavailableException();
        }
    }
}
