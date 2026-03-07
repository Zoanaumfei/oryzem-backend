package com.oryzem.backend.modules.catalog.repository;

import com.oryzem.backend.modules.catalog.domain.Product;
import com.oryzem.backend.core.tenant.TenantScope;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class ProductRepository {

    private final ConcurrentMap<String, ConcurrentMap<String, Product>> productsByTenantAndId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ConcurrentMap<String, String>> productIdsByTenantAndSku = new ConcurrentHashMap<>();

    public synchronized Product save(Product product) {
        String tenantScope = TenantScope.current();
        ConcurrentMap<String, Product> productsById = tenantProducts(tenantScope);
        ConcurrentMap<String, String> productIdBySku = tenantProductIdsBySku(tenantScope);
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
        Product product = tenantProducts(TenantScope.current()).get(productId);
        return Optional.ofNullable(product).map(this::copy);
    }

    public Optional<Product> findBySku(String sku) {
        String skuKey = normalizeSku(sku);
        String productId = tenantProductIdsBySku(TenantScope.current()).get(skuKey);
        if (productId == null) {
            return Optional.empty();
        }
        return findById(productId);
    }

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        for (Product product : tenantProducts(TenantScope.current()).values()) {
            products.add(copy(product));
        }
        return products;
    }

    private ConcurrentMap<String, Product> tenantProducts(String tenantScope) {
        return productsByTenantAndId.computeIfAbsent(tenantScope, ignored -> new ConcurrentHashMap<>());
    }

    private ConcurrentMap<String, String> tenantProductIdsBySku(String tenantScope) {
        return productIdsByTenantAndSku.computeIfAbsent(tenantScope, ignored -> new ConcurrentHashMap<>());
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
