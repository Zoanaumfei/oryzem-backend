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
    public ResponseEntity<?> createItem(@Valid @RequestBody ItemRequest request) {
        try {
            ItemResponse response = itemService.createItem(request);
            log.info("POST /api/v1/items - Sucesso: {}", response);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("POST /api/v1/items - Dados inválidos: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Dados inválidos: " + e.getMessage()));

        } catch (IllegalStateException e) {
            log.warn("POST /api/v1/items - Conflito: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("POST /api/v1/items - Erro interno: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro interno no servidor"));
        }
    }

    @GetMapping("/{partNumberID}/{supplierID}")
    public ResponseEntity<?> getItem(
            @PathVariable String partNumberID,
            @PathVariable String supplierID) {

        try {
            ItemResponse response = itemService.getItem(partNumberID, supplierID);
            log.info("GET /api/v1/items/{}/{} - Sucesso", partNumberID, supplierID);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.warn("GET /api/v1/items/{}/{} - Não encontrado: {}",
                    partNumberID, supplierID, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));

        } catch (Exception e) {
            log.error("GET /api/v1/items/{}/{} - Erro interno: ",
                    partNumberID, supplierID, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erro interno no servidor"));
        }
    }
}
