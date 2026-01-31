package com.oryzem.backend.modules.files.controller;

import com.oryzem.backend.modules.files.dto.PresignDownloadResponse;
import com.oryzem.backend.modules.files.dto.PresignUploadRequest;
import com.oryzem.backend.modules.files.dto.PresignUploadResponse;
import com.oryzem.backend.modules.files.service.S3PresignService;
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
public class FilesController {

    private final S3PresignService presignService;

    public FilesController(S3PresignService presignService) {
        this.presignService = presignService;
    }

    @PostMapping("/presign-upload")
    public ResponseEntity<PresignUploadResponse> presignUpload(
            @Valid @RequestBody PresignUploadRequest request
    ) {
        PresignUploadResponse response = presignService.presignUpload(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/presign-download")
    public ResponseEntity<PresignDownloadResponse> presignDownload(
            @RequestParam("key") @NotBlank String key
    ) {
        PresignDownloadResponse response = presignService.presignDownload(key);
        return ResponseEntity.ok(response);
    }
}

