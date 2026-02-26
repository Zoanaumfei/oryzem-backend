package com.oryzem.backend.modules.integrations.service;

import com.oryzem.backend.modules.catalog.domain.Product;
import com.oryzem.backend.modules.catalog.repository.ProductRepository;
import com.oryzem.backend.modules.integrations.domain.MarketplaceOrderItemPayload;
import com.oryzem.backend.modules.integrations.domain.MarketplaceOrderPayload;
import com.oryzem.backend.modules.orders.dto.CreateOrderRequest;
import com.oryzem.backend.modules.orders.dto.OrderItemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MarketplaceOrderMapper {

    private final ProductRepository productRepository;

    public CreateOrderRequest toCreateOrderRequest(MarketplaceOrderPayload payload) {
        if (payload.getSource() == null) {
            throw new IllegalArgumentException("Marketplace order source is required");
        }
        if (payload.getExternalOrderId() == null || payload.getExternalOrderId().isBlank()) {
            throw new IllegalArgumentException("Marketplace externalOrderId is required");
        }
        if (payload.getMerchantId() == null || payload.getMerchantId().isBlank()) {
            throw new IllegalArgumentException("Marketplace merchantId is required");
        }

        List<OrderItemRequest> items = payload.getItems().stream()
                .map(this::toItemRequest)
                .toList();

        return CreateOrderRequest.builder()
                .source(payload.getSource())
                .merchantId(payload.getMerchantId().trim())
                .externalId(payload.getExternalOrderId().trim())
                .customerName(defaultCustomerName(payload.getCustomerName()))
                .items(items)
                .build();
    }

    private OrderItemRequest toItemRequest(MarketplaceOrderItemPayload itemPayload) {
        Product product = resolveProduct(itemPayload);
        BigDecimal unitPrice = itemPayload.getUnitPrice() == null ? product.getUnitPrice() : itemPayload.getUnitPrice();

        return OrderItemRequest.builder()
                .productId(product.getId())
                .nameSnapshot(itemPayload.getName() == null || itemPayload.getName().isBlank()
                        ? product.getName()
                        : itemPayload.getName().trim())
                .quantity(itemPayload.getQuantity())
                .unitPrice(unitPrice)
                .build();
    }

    private Product resolveProduct(MarketplaceOrderItemPayload itemPayload) {
        if (itemPayload.getProductId() != null && !itemPayload.getProductId().isBlank()) {
            return productRepository.findById(itemPayload.getProductId().trim())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unknown marketplace productId: " + itemPayload.getProductId()
                    ));
        }

        if (itemPayload.getSku() != null && !itemPayload.getSku().isBlank()) {
            return productRepository.findBySku(itemPayload.getSku().trim())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unknown marketplace sku: " + itemPayload.getSku()
                    ));
        }

        throw new IllegalArgumentException("Marketplace item must provide productId or sku");
    }

    private String defaultCustomerName(String customerName) {
        if (customerName == null || customerName.isBlank()) {
            return "Marketplace Customer";
        }
        return customerName.trim();
    }
}
