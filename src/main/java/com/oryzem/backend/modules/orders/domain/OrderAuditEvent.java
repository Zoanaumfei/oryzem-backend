package com.oryzem.backend.modules.orders.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAuditEvent {

    private String id;
    private String orderId;
    private String eventType;
    private String message;
    private Instant createdAt;
}
