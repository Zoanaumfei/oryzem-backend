package com.oryzem.backend.modules.integrations.service;

import com.oryzem.backend.modules.integrations.config.IfoodProperties;
import com.oryzem.backend.modules.integrations.dto.IfoodIngestionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class IfoodReconciliationScheduler {

    private final IfoodIngestionService ifoodIngestionService;
    private final IfoodProperties ifoodProperties;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(fixedDelayString = "${app.integrations.ifood.reconciliation-interval-seconds:300}000")
    public void reconcile() {
        if (!ifoodProperties.enabled() || !ifoodProperties.reconciliationEnabled()) {
            return;
        }
        if (!running.compareAndSet(false, true)) {
            log.warn("Skipping iFood reconciliation because previous execution is still running");
            return;
        }

        try {
            IfoodIngestionResponse response = ifoodIngestionService.ingestFromPolling();
            if (response.getProcessedEvents() > 0 || response.getFailedEvents() > 0) {
                log.info("iFood reconciliation processed={} imported={} duplicates={} failed={}",
                        response.getProcessedEvents(),
                        response.getImportedOrders(),
                        response.getDuplicateOrders(),
                        response.getFailedEvents());
            }
        } finally {
            running.set(false);
        }
    }
}
