package com.oryzem.backend.modules.catalog.service;

import com.oryzem.backend.modules.catalog.domain.Product;
import com.oryzem.backend.modules.catalog.domain.ProductNotFoundException;
import com.oryzem.backend.modules.catalog.dto.ProductCreateRequest;
import com.oryzem.backend.modules.catalog.dto.ProductResponse;
import com.oryzem.backend.modules.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(ProductCreateRequest request) {
        String normalizedSku = normalizeRequired(request.getSku(), "sku");
        productRepository.findBySku(normalizedSku).ifPresent(existing -> {
            throw new IllegalStateException("Product SKU already exists: " + normalizedSku);
        });

        Instant now = Instant.now();
        Product product = Product.builder()
                .id(UUID.randomUUID().toString())
                .sku(normalizedSku)
                .name(normalizeRequired(request.getName(), "name"))
                .category(normalizeRequired(request.getCategory(), "category"))
                .unitPrice(request.getUnitPrice())
                .active(request.getActive() == null || request.getActive())
                .createdAt(now)
                .updatedAt(now)
                .build();

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    public List<ProductResponse> listProducts() {
        return productRepository.findAll().stream()
                .sorted(Comparator.comparing(Product::getCreatedAt))
                .map(this::toResponse)
                .toList();
    }

    public Product getProductById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .category(product.getCategory())
                .unitPrice(product.getUnitPrice())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}
