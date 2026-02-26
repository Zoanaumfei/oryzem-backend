package com.oryzem.backend.modules.integrations.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.integrations.ifood")
public record IfoodProperties(
        boolean enabled,
        String baseUrl,
        String clientId,
        String clientSecret,
        boolean webhookEnabled,
        String webhookSecret,
        boolean eventLedgerEnabled,
        int eventLedgerTtlDays,
        boolean reconciliationEnabled,
        int reconciliationIntervalSeconds,
        List<String> merchantIds,
        List<String> eventTypes,
        String preferredCancellationCode,
        String cancellationReason,
        int requestTimeoutSeconds,
        int tokenRefreshSkewSeconds
) {
    private static final String DEFAULT_BASE_URL = "https://merchant-api.ifood.com.br";
    private static final String DEFAULT_CANCELLATION_REASON = "Cancelamento solicitado pela loja";
    private static final int DEFAULT_EVENT_LEDGER_TTL_DAYS = 7;
    private static final int DEFAULT_RECONCILIATION_INTERVAL_SECONDS = 300;
    private static final int DEFAULT_REQUEST_TIMEOUT_SECONDS = 20;
    private static final int DEFAULT_TOKEN_REFRESH_SKEW_SECONDS = 60;

    public IfoodProperties {
        baseUrl = trimToNull(baseUrl);
        baseUrl = baseUrl == null ? DEFAULT_BASE_URL : baseUrl;
        clientId = trimToNull(clientId);
        clientSecret = trimToNull(clientSecret);
        webhookSecret = trimToNull(webhookSecret);
        eventLedgerTtlDays = eventLedgerTtlDays > 0
                ? eventLedgerTtlDays
                : DEFAULT_EVENT_LEDGER_TTL_DAYS;
        reconciliationIntervalSeconds = reconciliationIntervalSeconds > 0
                ? reconciliationIntervalSeconds
                : DEFAULT_RECONCILIATION_INTERVAL_SECONDS;
        merchantIds = sanitizeList(merchantIds);
        eventTypes = sanitizeList(eventTypes);
        preferredCancellationCode = trimToNull(preferredCancellationCode);
        cancellationReason = trimToNull(cancellationReason);
        cancellationReason = cancellationReason == null ? DEFAULT_CANCELLATION_REASON : cancellationReason;

        requestTimeoutSeconds = requestTimeoutSeconds > 0
                ? requestTimeoutSeconds
                : DEFAULT_REQUEST_TIMEOUT_SECONDS;
        tokenRefreshSkewSeconds = tokenRefreshSkewSeconds >= 0
                ? tokenRefreshSkewSeconds
                : DEFAULT_TOKEN_REFRESH_SKEW_SECONDS;
    }

    public boolean hasRequiredCredentials() {
        return clientId != null && clientSecret != null;
    }

    private static List<String> sanitizeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .map(IfoodProperties::trimToNull)
                .filter(value -> value != null)
                .toList();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
