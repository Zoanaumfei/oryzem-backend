package com.oryzem.backend.core.audit;

import java.time.Instant;

public record AuditMetadata(
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy
) {
}
