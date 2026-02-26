package com.oryzem.backend.modules.integrations.controller;

import com.oryzem.backend.modules.integrations.dto.IfoodIngestionResponse;
import com.oryzem.backend.modules.integrations.service.IfoodIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/integrations/ifood")
@RequiredArgsConstructor
@Tag(name = "iFood Integrations", description = "iFood webhook and reconciliation endpoints")
public class IfoodIntegrationController {

    private final IfoodIngestionService ifoodIngestionService;

    @PostMapping("/webhook")
    @Operation(summary = "Receive iFood webhook events")
    public ResponseEntity<IfoodIngestionResponse> receiveWebhook(
            @RequestBody String payload,
            @RequestHeader(name = "x-ifood-signature", required = false) String signature) {
        IfoodIngestionResponse response = ifoodIngestionService.ingestFromWebhook(payload, signature);
        return ResponseEntity.accepted().body(response);
    }

    @PostMapping("/reconcile")
    @Operation(summary = "Trigger iFood polling reconciliation")
    public ResponseEntity<IfoodIngestionResponse> reconcile() {
        return ResponseEntity.ok(ifoodIngestionService.ingestFromPolling());
    }
}
