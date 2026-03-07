package com.oryzem.backend.modules.system.controller;

import com.oryzem.backend.shared.dto.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "System", description = "Basic endpoints to confirm the API is reachable")
@SecurityRequirement(name = "bearerAuth")
public class HelloController {

    @GetMapping("/hello")
    @Operation(
            summary = "Return a welcome message",
            description = "Simple authenticated endpoint used to verify that the API is responding."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Message returned successfully"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public String hello() {
        return "Hello, Oryzem API is running!";
    }

    @GetMapping("/status")
    @Operation(
            summary = "Return a service status message",
            description = "Simple authenticated endpoint used as a lightweight availability check."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status returned successfully"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public String status() {
        return "API está operacional!";
    }
}
