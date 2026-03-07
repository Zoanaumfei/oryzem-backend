package com.oryzem.backend.modules.files.controller;

import com.oryzem.backend.modules.files.dto.PresignDownloadResponse;
import com.oryzem.backend.modules.files.dto.PresignUploadRequest;
import com.oryzem.backend.modules.files.dto.PresignUploadResponse;
import com.oryzem.backend.modules.files.service.S3PresignService;
import com.oryzem.backend.shared.dto.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "Files", description = "Generate pre-signed URLs for file uploads and downloads")
@SecurityRequirement(name = "bearerAuth")
public class FilesController {

    private final S3PresignService presignService;

    public FilesController(S3PresignService presignService) {
        this.presignService = presignService;
    }

    @PostMapping("/presign-upload")
    @Operation(
            summary = "Generate a pre-signed upload URL",
            description = "Returns a temporary URL and metadata needed to upload a file directly to S3."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Upload request with file metadata"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upload URL generated successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid upload request",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<PresignUploadResponse> presignUpload(
            @Valid @RequestBody PresignUploadRequest request
    ) {
        PresignUploadResponse response = presignService.presignUpload(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/presign-download")
    @Operation(
            summary = "Generate a pre-signed download URL",
            description = "Returns a temporary URL that allows downloading an object from S3."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Download URL generated successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid object key",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<PresignDownloadResponse> presignDownload(
            @Parameter(description = "S3 object key", example = "initiatives/2026/report.pdf")
            @RequestParam("key") @NotBlank String key
    ) {
        PresignDownloadResponse response = presignService.presignDownload(key);
        return ResponseEntity.ok(response);
    }
}
