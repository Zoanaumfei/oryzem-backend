package com.oryzem.backend.domain.aws.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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

    @GetMapping("/{partNumberID}/{supplierID}")
    public ResponseEntity<ItemResponse> getItem(
            @PathVariable String partNumberID,
            @PathVariable String supplierID) {

        ItemResponse response =
                itemService.getItem(partNumberID, supplierID);

        log.info("GET /api/v1/items/{}/{} - Sucesso",
                partNumberID, supplierID);

        return ResponseEntity.ok(response);
    }
}
