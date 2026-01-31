package com.oryzem.backend.modules.items.controller;

import com.oryzem.backend.modules.items.domain.ItemStatus;
import com.oryzem.backend.modules.items.dto.ItemRequest;
import com.oryzem.backend.modules.items.dto.ItemResponse;
import com.oryzem.backend.modules.items.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponse> createItem(
            @Valid @RequestBody ItemRequest request) {

        ItemResponse response = itemService.createItem(request);

        log.info("POST /api/v1/items - Sucesso: {}", response);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        List<ItemResponse> response = itemService.getAllItems();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{supplierID}/{partNumberVersion}")
    public ResponseEntity<ItemResponse> getItem(
            @PathVariable String supplierID,
            @PathVariable String partNumberVersion) {

        ItemResponse response =
                itemService.getItem(supplierID, partNumberVersion);

        log.info("GET /api/v1/items/{}/{} - Sucesso",
                supplierID, partNumberVersion);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ItemResponse>> getItemsByStatus(
            @PathVariable ItemStatus status) {
        List<ItemResponse> response = itemService.getItemsByStatus(status);
        return ResponseEntity.ok(response);
    }
}








