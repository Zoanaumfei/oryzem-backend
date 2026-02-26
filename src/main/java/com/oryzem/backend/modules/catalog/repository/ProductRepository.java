package com.oryzem.backend.modules.catalog.repository;

import com.oryzem.backend.modules.catalog.domain.Product;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class ProductRepository {

    private final ConcurrentMap<String, Product> productsById = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> productIdBySku = new ConcurrentHashMap<>();

    public synchronized Product save(Product product) {
        String skuKey = normalizeSku(product.getSku());
        String existingProductId = productIdBySku.get(skuKey);
        if (existingProductId != null && !existingProductId.equals(product.getId())) {
            throw new IllegalStateException("Product SKU already exists: " + product.getSku());
        }

        Product copy = copy(product);
        productsById.put(copy.getId(), copy);
        productIdBySku.put(skuKey, copy.getId());
        return copy(copy);
    }

    public Optional<Product> findById(String productId) {
        Product product = productsById.get(productId);
        return Optional.ofNullable(product).map(this::copy);
    }

    public Optional<Product> findBySku(String sku) {
        String skuKey = normalizeSku(sku);
        String productId = productIdBySku.get(skuKey);
        if (productId == null) {
            return Optional.empty();
        }
        return findById(productId);
    }

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        for (Product product : productsById.values()) {
            products.add(copy(product));
        }
        return products;
    }

    private String normalizeSku(String sku) {
        return sku == null ? "" : sku.trim().toUpperCase();
    }

    private Product copy(Product product) {
        return Product.builder()
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
}
