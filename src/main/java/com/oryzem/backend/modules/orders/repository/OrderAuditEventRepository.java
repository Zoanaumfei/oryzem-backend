package com.oryzem.backend.modules.orders.repository;

import com.oryzem.backend.core.tenant.TenantScope;
import com.oryzem.backend.modules.orders.domain.OrderAuditEvent;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class OrderAuditEventRepository {

    private final ConcurrentMap<String, CopyOnWriteArrayList<OrderAuditEvent>> eventsByTenant = new ConcurrentHashMap<>();

    public OrderAuditEvent save(OrderAuditEvent event) {
        OrderAuditEvent copy = copy(event);
        tenantEvents(TenantScope.current()).add(copy);
        return copy(copy);
    }

    public List<OrderAuditEvent> findByOrderId(String orderId) {
        List<OrderAuditEvent> result = new ArrayList<>();
        for (OrderAuditEvent event : tenantEvents(TenantScope.current())) {
            if (orderId.equals(event.getOrderId())) {
                result.add(copy(event));
            }
        }
        return result;
    }

    private CopyOnWriteArrayList<OrderAuditEvent> tenantEvents(String tenantScope) {
        return eventsByTenant.computeIfAbsent(tenantScope, ignored -> new CopyOnWriteArrayList<>());
    }

    private OrderAuditEvent copy(OrderAuditEvent event) {
        return OrderAuditEvent.builder()
                .id(event.getId())
                .orderId(event.getOrderId())
                .eventType(event.getEventType())
                .message(event.getMessage())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
