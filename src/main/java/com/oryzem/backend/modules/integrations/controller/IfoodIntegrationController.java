package com.oryzem.backend.modules.integrations.controller;

import com.oryzem.backend.modules.integrations.dto.IfoodIngestionResponse;
import com.oryzem.backend.modules.integrations.service.IfoodIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    @Operation(
            summary = "Receive iFood webhook events",
            description = "Public endpoint that receives webhook notifications sent by iFood.",
            security = {}
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Raw JSON payload sent by iFood",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "string"),
                    examples = @ExampleObject(
                            name = "order-created",
                            value = "{\"id\":\"evt-1\",\"fullCode\":\"ABC123\",\"eventType\":\"PLC\"}"
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Webhook accepted for processing"),
            @ApiResponse(responseCode = "400", description = "Invalid webhook payload")
    })
    public ResponseEntity<IfoodIngestionResponse> receiveWebhook(
            @RequestBody String payload,
            @Parameter(description = "Optional signature header used to validate the webhook", example = "sha256=abc123")
            @RequestHeader(name = "x-ifood-signature", required = false) String signature) {
        IfoodIngestionResponse response = ifoodIngestionService.ingestFromWebhook(payload, signature);
        return ResponseEntity.accepted().body(response);
    }

    @PostMapping("/reconcile")
    @Operation(
            summary = "Trigger iFood polling reconciliation",
            description = "Authenticated endpoint that pulls pending iFood orders using polling reconciliation."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reconciliation completed successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<IfoodIngestionResponse> reconcile() {
        return ResponseEntity.ok(ifoodIngestionService.ingestFromPolling());
    }
}
